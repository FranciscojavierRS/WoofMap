# WoofMap

Aplicación Android + API REST para descubrir y compartir lugares dog-friendly sobre un mapa.

## Requisitos

- JDK 25
- Docker (o Podman) con Compose
- Android Studio para la app

## Puesta en marcha

```bash
cp .env.example .env
# Edita .env: define POSTGRES_PASSWORD y JWT_SECRET (openssl rand -base64 48)
docker compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Datos de ejemplo (demo)

Con los servicios levantados, carga lugares, usuarios y comentarios de prueba:

```bash
docker compose exec -T db psql -U woofmap -d woofmap -f - < Docs/demo/datos-demo.sql   # bash
```

```powershell
Get-Content Docs/demo/datos-demo.sql | docker compose exec -T db psql -U woofmap -d woofmap   # PowerShell
```

Usuarios creados (contraseña `woofmap123`): `ana@woofmap.app` y `luis@woofmap.app`.
El script es idempotente y se puede repetir.

## App Android

`API_BASE_URL` se define en `android/app/build.gradle.kts`:

- Emulador: `http://10.0.2.2:8080/` (valor por defecto).
- Dispositivo físico: cambia `10.0.2.2` por la IP local del equipo y asegúrate de que el puerto 8080 es accesible.
- Demo fuera de la red local: apunta a la URL HTTPS del túnel (Cloudflare Tunnel, ngrok o Tailscale).

El tráfico HTTP en claro solo está permitido en builds `debug`; una build `release` exige HTTPS.

Tras cambiar la URL, sincroniza Gradle y vuelve a instalar la app.

## Tests backend

```bash
cd WoofMapApi
./mvnw test
```

Los tests de integración con Testcontainers se omiten si Docker no está en marcha.
El informe de cobertura queda en `WoofMapApi/target/site/jacoco/index.html`.

## Copia de seguridad y restauración

```bash
# Copia
docker compose exec -T db pg_dump -U woofmap -d woofmap -Fc > woofmap.dump

# Restauración (sobre una base vacía)
docker compose exec -T db pg_restore -U woofmap -d woofmap --clean --if-exists < woofmap.dump
```

Para empezar de cero: `docker compose down -v` (borra también las fotos y la base de datos).

## Estado

Ver la hoja de ruta en `Docs/WoofMap– Documento técnico y hoja de ruta.md`.
