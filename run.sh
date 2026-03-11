#!/bin/bash

set -e

echo "🔌 Creando network si no existe..."
docker network create app-network >/dev/null 2>&1 || true

echo "🗄️ Levantando PostgreSQL..."
docker rm -f postgres-db >/dev/null 2>&1 || true

docker run -d \
  --name postgres-db \
  --network app-network \
  -e POSTGRES_DB=appdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  postgres:15

echo "⏳ Esperando a que Postgres inicie..."
sleep 8

echo "🏗️ Build de la app Java..."
docker build -t sportflow-app .

echo "🚀 Levantando app..."
docker rm -f sportflow-app >/dev/null 2>&1 || true

docker run -d \
  --name sportflow-app \
  --network app-network \
  -p 8080:8080 \
  -e DB_HOST=postgres-db \
  -e DB_NAME=appdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  sportflow-app

echo "✅ App corriendo en http://localhost:8080"
