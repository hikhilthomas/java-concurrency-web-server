# java-concurrency-web-server

A Java HTTP server that can run in three different concurrency models:  
- **ThreadPool** (traditional blocking with executor service)  
- **VirtualThread** (Java 21 virtual threads)  
- **Reactive** (using Vert.x event loop)  

This project is designed to benchmark and compare how each model handles concurrency under different workloads (simple, IO-bound, compute-heavy).

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/java-concurrency-web-server.git
cd java-concurrency-web-server
```
### 🐳 Run with Docker

### Requirements
- [Docker](https://docs.docker.com/get-docker/)  
- [Docker Compose](https://docs.docker.com/compose/)

### Build and start the server
docker-compose up --build

## 🔧 Configuration

The server mode and port are configured in [`compose.yaml`](./compose.yaml).  
For example:

```yaml
command: ["ThreadPool", "4221"]
```

Replace `ThreadPool` with one of:

- `ThreadPool`
- `VirtualThread`
- `Reactive`

Port defaults to `4221`.

## 🧪 Test the server

Once running, you can hit the server endpoints with `curl`:

```bash
curl http://localhost:4221/
curl http://localhost:4221/io
curl http://localhost:4221/compute
```

Or load test with k6:

```bash
k6 run script.js
```
# or run with a web dashboard
```bash
K6_WEB_DASHBOARD=true k6 run script.js
```
## 🛑 Stop the server

To stop the server gracefully and ensure that **Java Flight Recorder (JFR)** captures all recordings:

```bash
docker-compose stop
```
After stopping, you can remove all containers and images if needed:
```bash
docker-compose down -rmi all
```
## 📂 Logs and Recordings

- **Java Flight Recorder (JFR)** is enabled by default.  
  Recordings are saved to `/recordings`.  
- Error logs are stored in `/logs`.

## 🖥️ Run Locally (without Docker)

### Requirements
- Azul JDK 21  
- Maven  
- k6  

### Start the server

Use the provided shell script:

```bash
sh start.sh <ServerType> <Port>
```
`<ServerType>` is mandatory: one of:

- `ThreadPool`
- `VirtualThread`
- `Reactive`

`<Port>` is optional (default: `4221`).

### Example

```bash
sh start.sh VirtualThread 4221
```

### This script will

1. Build the project with Maven (`mvn package`)  
2. Start the server with **Java Flight Recorder** enabled  

#### The `start.sh` script

```sh
#!/bin/sh
set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")"
  mvn -B package
)

exec java \
     -XX:StartFlightRecording=filename=recordings/server.jfr,settings=profile,dumponexit=true,delay=1s \
     -jar target/http-server.jar "$@"
```

## Test locally

Same as Docker:

```bash
curl http://localhost:4221/
curl http://localhost:4221/io
curl http://localhost:4221/compute
```
Or run load tests with k6:

```bash
k6 run script.js
```
## 📂 Project Structure

- `BlockingServer` – Base implementation for `ThreadPool` and `VirtualThread` servers  
- `ReactiveServer` – Vert.x-based reactive server  
- `HTTPHandler` – Interface for request handlers  
- `BlockingHTTPHandler` / `ReactiveHTTPHandler` – Implementations for different concurrency models  
- `start.sh` – Helper script to build and run locally with JFR enabled  
- `compose.yaml` – Docker Compose configuration  

## 📊 Observability

- JFR recordings → `recordings/`  
- Application logs → `logs/`  

These can be analyzed with **Java Mission Control** for profiling and performance insights.

## 🧪 Benchmarking

You can benchmark with `k6` by running:

```bash
k6 run script.js
```
For a live web dashboard:

```bash
K6_WEB_DASHBOARD=true k6 run script.js
```
## ⚖️ Comparison Goal

This project lets you explore:

- How many concurrent requests each model can handle
- Trade-offs in throughput, latency, and CPU utilization between:
  - Traditional thread pool
  - Virtual threads (structured concurrency)
  - Reactive (event loop with async IO)

