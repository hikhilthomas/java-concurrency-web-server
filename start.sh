#!/bin/sh
set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
  mvn -B package
)

exec java \
     -XX:StartFlightRecording=filename=recordings/server.jfr,settings=profile,dumponexit=true,delay=1s \
     -jar target/http-server.jar "$@"