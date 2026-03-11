# api-gestor-reservas
Backend para gestion del sistema de reserva de clases

### Sportflow – Backend Java + PostgreSQL + Docker

Aplicación backend básica desarrollada en Java + Spring Boot, conectada a PostgreSQL y ejecutada mediante Docker.

El objetivo del proyecto es proveer una base simple, portable y reproducible para desarrollo de servicios backend.

## 🧱 Stack tecnológico

Java 21

Spring Boot

Gradle

PostgreSQL 15

Docker

Docker network

# 📦 Arquitectura
Controller → Service → Repository → PostgreSQL


Controller: expone endpoints REST

Service: lógica de negocio

Repository: acceso a datos (JPA)

DB: PostgreSQL en container Docker

## 🚀 Cómo levantar el proyecto localmente
Requisitos

Tener instalado:

Docker

Git

No se necesita Java ni PostgreSQL local.

1) Clonar repo
   git clone <repo-url>
   cd sportflow

2) Ejecutar script de arranque
   sh run.sh


Esto hace automáticamente:

Crea network Docker

Levanta PostgreSQL en container

Build del jar con Gradle dentro de Docker

Levanta backend Java

Conecta app con DB

3) Verificar que la app funciona
   curl http://localhost:8080/users

# 🧪 Probar endpoints
### Crear usuario
    curl -X POST http://localhost:8080/users \
    -H "Content-Type: application/json" \
    -d '{"name":"Emilio","lastname":"Quilodran","email":"emiq@mail.com","role":"COACH","isActive":true}'

### Listar usuarios
    curl http://localhost:8080/users

### 🐳 Comandos útiles de Docker
Ver contenedores activos
    
    docker ps

Ver logs del backend
    
    docker logs sportflow-app

Ver logs de postgres
    
    docker logs postgres-db

### 🛑 Detener toda la aplicación

    docker rm -f sportflow-app postgres-db && docker network rm app-network


Esto:

mata backend

mata postgres

elimina network docker

### 🧹 Limpieza completa Docker

    docker system prune -a


Elimina contenedores, imágenes y cache.

### ⚙️ Variables de entorno usadas

El backend se conecta a PostgreSQL usando:

DB_HOST
DB_NAME
DB_USER
DB_PASSWORD


Estas variables se inyectan desde Docker en runtime.

## 📁 Estructura del proyecto
    src/
    ├── controller
    ├── service
    ├── repository
    ├── model
    └── resources
    └── application.properties
    
    Dockerfile
    run.sh
    build.gradle
    README.md
