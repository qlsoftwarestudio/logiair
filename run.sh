#!/bin/bash

set -e

echo "🔌 Creando network si no existe..."
docker network create app-network >/dev/null 2>&1 || true

echo "🗄️ Levantando PostgreSQL con volumen persistente..."
docker rm -f postgres-db >/dev/null 2>&1 || true

# Crear volumen persistente con nombre específico si no existe
docker volume create postgres-logiair-data >/dev/null 2>&1 || true
echo "   📦 Volumen 'postgres-logiair-data' listo para persistencia"

docker run -d \
  --name postgres-db \
  --network app-network \
  -e POSTGRES_DB=logiairdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v postgres-logiair-data:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:15

echo "⏳ Esperando a que Postgres inicie..."
sleep 8

echo "🏗️ Build de la app Java..."
docker build -t logiair-os-app .

echo "🚀 Levantando app..."
docker rm -f logiair-os-app >/dev/null 2>&1 || true

docker run -d \
  --name logiair-os-app \
  --network app-network \
  -p 8080:8080 \
  -e DB_HOST=postgres-db \
  -e DB_NAME=logiairdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  logiair-os-app

echo "✅ App corriendo en http://localhost:8080"
echo "📊 Health Check: http://localhost:8080/actuator/health"
echo "📚 API Docs: http://localhost:8080/swagger-ui.html"
