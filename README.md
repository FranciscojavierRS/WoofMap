# WoofMap
## Requisitos
- JDK 25
- Docker/Podman
- Android Studio (opcional para app)

## Desarrollo rápido
```bash
# Copiar variables de entorno
cp .env.example .env  # si existe, sino crear con valores

# Levantar servicios
docker compose up --build
# o con podman: podman compose up --build
```

La API estará en http://localhost:8080. Swagger UI: http://localhost:8080/swagger-ui.html

## Tests backend
```bash
cd WoofMapApi
./mvnw test
```

## Estado
Ver hoja de ruta en `Docs/WoofMap– Documento técnico y hoja de ruta.md`
