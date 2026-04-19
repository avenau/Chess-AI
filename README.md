# Chess AI

## Prerequisites

Install these before running the project:

- Java 11
- Maven
- Node.js
- npm

This repo includes a root [start-chess](/Users/avenau/Desktop/Projects/Chess%20Ai/start-chess:1) launcher that starts both apps, so the main requirement is having those tools available on your machine.

## Recommended Versions

- Java: `21`
- Node.js: a current version that includes `npm`

## macOS Install Options

Using Homebrew:

```bash
brew install openjdk@11 maven node
```

After installing Java with Homebrew, make sure your shell can find it. One common setup is:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Frontend Setup

Install frontend dependencies once:

```bash
cd chess_ai_frontend
npm install
cd ..
```

## Run The App

From the repo root:

```bash
./start-chess
```

This will:

- start the Spring Boot backend on `http://localhost:8080`
- start the Vite frontend on `http://localhost:5173`
- open the frontend in your browser

Press `Ctrl+C` to stop both servers.

## Troubleshooting

- If `./start-chess` says `JAVA_HOME` is not set correctly, install Java 11 and set `JAVA_HOME`.
- If port `8080` or `5173` is already in use, stop the existing process before starting the app.
- If the frontend dependencies are missing, run `npm install` inside `chess_ai_frontend`.
- Runtime logs are written to `.run/backend.log` and `.run/frontend.log`.
