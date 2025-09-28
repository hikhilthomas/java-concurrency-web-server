#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: sh load_test.sh <base-name>"
    exit 1
fi

BASE_NAME=$1
FOLDER_NAME="${BASE_NAME%_*}"
K6_FOLDER="./k6_data/$FOLDER_NAME"
JFR_FOLDER="./recordings/$FOLDER_NAME"
mkdir -p "$K6_FOLDER"
mkdir -p "$JFR_FOLDER"


for i in $(seq -w 01 10); do
  JFR_FILE="${BASE_NAME}_${i}.jfr"
  JSON_FILE="${BASE_NAME}_${i}.json"
  echo "Iteration number: ${i}"

  echo "🚀 Starting docker-compose stack..."
  docker-compose up --build -d

  # Wait until server responds with 200 OK
  echo "⏳ Waiting for server to be ready..."
  until curl -s -o /dev/null -w "%{http_code}" http://localhost:4221/ | grep -q "200"; do
    echo "Server not ready yet, retrying in 2s..."
    sleep 2
  done
  echo "✅ Server is up!"

  echo "🔥 Running warmup test..."
  k6 run warmup.js &
  K6_WARM_UP_ID=$!

  echo "⏳ Waiting 30s before main test (for JFR delay)..."
  sleep 30

  echo "🛑 Stopping warmup k6 test..."
  kill -INT $K6_WARM_UP_ID || true

  echo "📊 Running main load test (with summary export)..."
  k6 run script.js --summary-export="$K6_FOLDER/$JSON_FILE" &

  K6_PID=$!

  echo "⏳ Waiting 125s before main test (for JFR delay)..."
  sleep 125

  echo "🛑 Stopping main k6 test..."
  kill -INT $K6_PID || true

  echo "📦 Tearing down docker-compose stack..."
  docker-compose down

  echo "🧹 Removing all images built by docker-compose..."
  docker-compose down --rmi all

  echo "✅ Test run complete!"

  # Rename the server.jfr to the iteration-specific filename
  if [ -f ./recordings/server.jfr ]; then
      mv ./recordings/server.jfr "$JFR_FOLDER/$JFR_FILE"
      echo "Renamed server.jfr → $JFR_FOLDER/$JFR_FILE"
  else
      echo "Warning: server.jfr not found in ./recordings"
  fi

  echo "=== Finished iteration $i ==="
done