# logiair-os
Backend para sistema de gestión logística de guías aéreas (AWB)

### Logiair OS – Backend Java + PostgreSQL + Docker

Sistema operativo para despachantes de aduana desarrollado en Java + Spring Boot, conectado a PostgreSQL y ejecutado mediante Docker.

El objetivo del proyecto es proveer una plataforma completa para la digitalización y automatización del seguimiento operativo y administrativo de guías aéreas utilizadas en procesos de importación y exportación.

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
   cd logiair-os

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
    
    docker logs logiair-app

Ver logs de postgres
    
    docker logs postgres-db

### 🛑 Detener toda la aplicación

    docker rm -f logiair-app postgres-db && docker network rm app-network


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
