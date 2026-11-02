# Memoria de desarrollo — WoofMap

**Proyecto:** WoofMap  
**Tipo:** Aplicación Android + API REST  
**Repositorio:** monorepo con backend (`WoofMapApi/`), app Android (`android/`) y documentación (`Docs/`)  
**Metodología:** desarrollo incremental por fases, con tests en backend y en la app, y commits al cerrar cada bloque funcional.

---

## 1. Propósito del proyecto

WoofMap es una guía colaborativa de lugares dog-friendly. Permite a los dueños de perros descubrir parques, playas, terrazas, veterinarios, tiendas y otros puntos de interés sobre un mapa, y compartir su experiencia mediante fotos, comentarios y valoraciones.

### Objetivo general

Construir una aplicación móvil Android con backend propio que permita gestionar de forma colaborativa lugares aptos para perros sobre un mapa.

### Objetivos específicos

- Diseñar e implementar una API REST para usuarios, lugares, fotos, comentarios y valoraciones.
- Implementar registro e inicio de sesión con contraseñas cifradas y tokens JWT.
- Desarrollar una app Android con mapa interactivo para consultar y crear lugares.
- Permitir subir fotos y publicar comentarios con valoración.
- Añadir funciones de comunidad: favoritos, reportes de contenido y moderación por parte de administradores.
- Desplegar el sistema de forma reproducible con Docker Compose.

### Alcance

Incluido en el proyecto:

- Registro, login y cierre de sesión.
- Mapa con lugares cercanos y filtro por categoría.
- Creación, edición y borrado de lugares propios.
- Fotos de lugares y comentarios con valoración.
- Perfil de usuario, lugares creados y fotos de sus perros.
- Lugares favoritos con salto a su ubicación en el mapa.
- Reportes de lugares, fotos y comentarios.
- Rol de administrador con revisión de reportes y borrado de contenido.
- Concurso "Perro del Mes/Semana" y ranking de zonas.

Fuera del alcance actual: app iOS, notificaciones push, recuperación de contraseña por email, inicio de sesión con Google y almacenamiento de fotos en un servicio externo tipo MinIO.

El email se trata como identificador de la cuenta y no se puede modificar desde el perfil; el nombre sí es editable.

---

## 2. Tecnologías usadas y por qué

### 2.1 Backend

| Tecnología | Uso | Motivo |
| --- | --- | --- |
| Java 25 (LTS) | Lenguaje del backend | Versión LTS moderna, con `records`, `switch` con pattern matching e hilos virtuales. |
| Spring Boot 4.1.1 | Framework REST | Configuración rápida, ecosistema maduro e integración con seguridad, JPA y validación. |
| Spring Web | API REST | Controladores HTTP y serialización JSON. |
| Spring Data JPA + Hibernate | Persistencia | Reduce el código de acceso a datos y permite trabajar con entidades y repositorios. |
| Hibernate Spatial | Geometrías | Mapeo de `geography(Point, 4326)` de PostGIS. |
| Spring Security + JJWT 0.12.6 | Autenticación y autorización | API stateless con token `Bearer`; el rol se resuelve en cada petición. |
| BCrypt | Contraseñas | Hash con salt, estándar para no almacenar contraseñas en claro. |
| Bean Validation | Validación de DTOs | Rechazo temprano de datos inválidos con mensajes claros. |
| PostgreSQL 17 + PostGIS 3.5 | Base de datos | Consultas geográficas eficientes mediante índices GIST; PostGIS resuelve "lugares a menos de X km". |
| Flyway | Migraciones | Esquema versionado y reproducible (`V1`, `V2`, `V3`). |
| springdoc OpenAPI 2.8.5 | Documentación | Swagger UI interactivo para probar la API. |
| Maven Wrapper | Build | No exige Maven instalado en la máquina. |
| JUnit 5 + Mockito + AssertJ | Tests | Tests unitarios de servicios y controladores. |
| Testcontainers | Tests de integración | PostgreSQL/PostGIS real en contenedor; H2 no soporta PostGIS. |
| JaCoCo | Cobertura | Informe de cobertura en `target/site/jacoco/`. |
| Docker multi-stage | Despliegue | Compila con JDK 25 y ejecuta con JRE 25 en una imagen ligera. |

### 2.2 Aplicación Android

| Tecnología | Uso | Motivo |
| --- | --- | --- |
| Java | Lenguaje de la app | Coherente con el ciclo formativo y con el backend. |
| Android Views + XML | Interfaz | Estructura estable y sencilla para pantallas y `RecyclerView`. |
| ViewBinding | Acceso a vistas | Evita `findViewById` y errores de tipos. |
| MVVM + ViewModel + LiveData | Arquitectura | Separa lógica de negocio, estado y UI; sobrevive a cambios de configuración. |
| Retrofit 2 + Gson + OkHttp | Red | Cliente REST declarativo, serialización JSON e interceptor del token. |
| MapLibre 13.7.1 + OpenFreeMap | Mapa | Mapas vectoriales sin API key ni cuenta; estilo Liberty servido por `tiles.openfreemap.org`. |
| Glide | Imágenes | Carga y caché de fotos de lugares y perros. |
| FusedLocationProviderClient | Ubicación | Localización precisa con bajo consumo. |
| EncryptedSharedPreferences | Token | El JWT se guarda cifrado en el dispositivo. |
| Photo Picker + cámara del sistema | Fotos | APIs modernas, sin permisos de almacenamiento amplios. |
| Material 3 / AppCompat | Componentes | Botones, tarjetas, hojas inferiores y estados visuales coherentes. |
| JUnit 4 + Mockito + MockWebServer | Tests Android | Tests de ViewModels y repositorios sin depender de la red real. |

### 2.3 Infraestructura y calidad

- **Docker Compose** levanta `db` y `api` con un solo comando y usa volúmenes para base de datos y fotos.
- **GitHub Actions** ejecuta `./mvnw -B verify` en backend y `./gradlew testDebugUnitTest` en Android.
- **Postman** se usó para pruebas manuales de los flujos de la API (colecciones de las fases 1 y 2).
- **Datos de demostración** (`Docs/demo/datos-demo.sql`) permiten probar la app sin crear contenido a mano.

### Decisiones de diseño destacadas

- **Un único microservicio:** el tamaño del proyecto no justifica separar usuarios, lugares y fotos en servicios independientes; internamente el código se separa por capas y responsabilidades.
- **DTOs en lugar de entidades:** los controladores nunca exponen entidades JPA; esto evita acoplar la API al esquema y permite validar la entrada.
- **El usuario se obtiene del contexto de seguridad:** nunca del cuerpo de la petición, para que un usuario no pueda actuar como otro.
- **Autorización en servicio además de en controlador:** las reglas de propiedad y el rol de administrador se comprueban en la capa de servicio, donde está la lógica de negocio.
- **MapLibre en vez de Google Maps:** no requiere clave de API ni cuenta de facturación y encaja con un proyecto autoalojado.
- **Fotos en volumen, no en base de datos:** la base solo guarda la URL; el binario se sirve desde `/uploads/**`.
- **Respuestas de error homogéneas:** `GlobalExceptionHandler` traduce excepciones a códigos HTTP y mensajes consistentes.

---

## 3. Guía de preparación del entorno de desarrollo

### 3.1 Requisitos previos

| Herramienta | Versión / nota |
| --- | --- |
| Git | Cualquier versión reciente. |
| JDK | 25 para el backend. Para Android vale el JDK embebido de Android Studio o un JDK 17/21/25. |
| Docker + Docker Compose | Necesario para la base de datos, la API y los tests de integración. También sirve Podman con Compose. |
| Android Studio | Con Android SDK 37 instalado. |
| Postman | Opcional, para probar la API manualmente. |

Comprobaciones útiles:

```bash
java -version
docker --version
docker compose version
```

> Importante: Maven y Gradle necesitan un **JDK completo**, no solo un JRE. Si `javac` no está disponible, hay que apuntar `JAVA_HOME` al directorio del JDK.

### 3.2 Obtener el repositorio

```bash
git clone <URL-del-repositorio>
cd WoofMap
```

### 3.3 Configurar variables de entorno

```bash
cp .env.example .env
```

Editar `.env` y rellenar como mínimo:

```dotenv
POSTGRES_DB=woofmap
POSTGRES_USER=woofmap
POSTGRES_PASSWORD=<contraseña-segura>
POSTGRES_PORT=5432
JWT_SECRET=<secreto-de-al-menos-32-bytes>
UPLOADS_DIR=/app/uploads
```

Generar el secreto JWT:

```bash
openssl rand -base64 48
```

`.env` está excluido de Git; no se deben subir credenciales ni secretos.

### 3.4 Preparar el backend

Dos opciones válidas:

**Opción A — todo con Docker (la habitual):**

```bash
docker compose up --build
```

**Opción B — base de datos en Docker y API en local:**

```bash
docker compose up -d db

export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/woofmap"
export SPRING_DATASOURCE_USERNAME="woofmap"
export SPRING_DATASOURCE_PASSWORD="<contraseña-de-.env>"
export JWT_SECRET="<secreto-de-.env>"

cd WoofMapApi
./mvnw spring-boot:run
```

En Windows, definir las variables con `set` o desde el panel de entorno, o usar la Opción A.

### 3.5 Preparar la app Android

1. Abrir la carpeta `android/` con Android Studio.
2. Revisar `android/app/build.gradle.kts`:
   - Emulador: `API_BASE_URL = "http://10.0.2.2:8080/"`.
   - Dispositivo físico: usar la IP local del equipo, por ejemplo `http://192.168.0.51:8080/`.
3. Instalar el SDK 37 si Android Studio lo solicita.
4. Sincronizar Gradle.
5. Ejecutar en un emulador o dispositivo.

El build `debug` permite tráfico HTTP local mediante `android/app/src/debug/res/xml/network_security_config.xml`. Una build `release` exige HTTPS.

### 3.6 Verificar que todo funciona

```bash
# Backend: tests (requiere Docker para Testcontainers)
cd WoofMapApi
./mvnw test

# Android: tests unitarios y compilación
cd ../android
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

### 3.7 Problemas frecuentes

| Síntoma | Causa probable | Solución |
| --- | --- | --- |
| `release version 25 not supported` | Maven está usando un JRE o un JDK antiguo. | Apuntar `JAVA_HOME` a un JDK 25 y añadir sus `bin` al `PATH`. |
| La app no conecta con la API | URL incorrecta, firewall o IP cambiada. | Revisar `API_BASE_URL`, usar `10.0.2.2` en emulador y comprobar que el puerto 8080 es accesible. |
| Error de conexión a PostgreSQL | `.env` incompleto o base aún arrancando. | Revisar credenciales y esperar al `healthcheck` de Compose. |
| No se ejecutan los tests de integración | Docker no está en marcha. | Arrancar Docker y repetir `./mvnw test`. |
| `cleartext HTTP not permitted` en release | El tráfico HTTP solo se permite en debug. | Usar HTTPS o probar con build debug. |

---

## 4. Fases del desarrollo

El repositorio tiene 13 commits. El desarrollo se organizó en fases funcionales: primero el backend, después la app Android y finalmente las ampliaciones de comunidad y la documentación.

| Fase | Commit | Entregable |
| --- | --- | --- |
| 0. Preparación | `d9311da` | Repositorio, README y `.gitignore`. |
| 1. Backend base | `bca7b8d` | API de lugares, PostGIS, Docker y Swagger. |
| 2. Autenticación | `b8eb47e` | Registro, login y endpoints protegidos con JWT. |
| 3. Fotos y comentarios | `f9610c2` | Subida de fotos, comentarios y valoraciones. |
| 4. Entorno y corrección | `efc8444`, `7df43a5` | `.env`, README, limpieza y estabilización del backend. |
| 5. Arranque Android | `8d10694` | Proyecto Android inicial. |
| 6. Android: auth y mapa | `4a4756d` | Login, registro, token cifrado y mapa. |
| 7. Android: contenido | `f4cc75b` | Crear lugares, detalle, fotos, comentarios y filtros. |
| 8. Pulido e infraestructura | `7156c93` | CI, datos demo, endurecimiento y refactor de factorías. |
| 9. Pulido de interfaz | `15c703f` | Hoja de detalle, filtros, iconos y correcciones. |
| 10. Concurso de perros | `914ee3d` | Perro del Mes/Semana, votos y ranking de zonas. |
| 11. Perfil y moderación | `75f1d32` | Perfil, favoritos, reportes, admin y email inmutable. |
| 12. Documentación | Actual | Memoria de desarrollo. |

### Fase 0 · Preparación del repositorio (`d9311da`)

**Cambios:** creación del monorepo, `.gitignore` y `README.md` inicial.

**Por qué:** dejar desde el principio una estructura única para backend, app y documentación, y evitar subir artefactos de build, credenciales o ficheros locales.

### Fase 1 · Backend base y base de datos (`bca7b8d`)

**Cambios principales:**

- Proyecto Spring Boot con Web, JPA, Validation, Flyway, Security y SpringDoc.
- `docker-compose.yml` con `db` (PostGIS) y `api`, más el Dockerfile multi-stage.
- Migración `V1__crear_esquema_inicial.sql`: usuarios, lugares, fotos, comentarios, índices y restricciones.
- Entidades JPA, repositorios y DTOs de lugares.
- CRUD de lugares y consulta de lugares cercanos con PostGIS.
- `GlobalExceptionHandler` y Swagger UI.
- Tests de servicio y controlador, y colección de Postman.

**Por qué:**

- Se empezó por el backend porque define el contrato que consumirá la app.
- PostGIS se eligió para resolver proximidad con índices espaciales, algo que una base relacional sin extensión geográfica no cubre igual de bien.
- Flyway permite reconstruir el esquema desde cero y mantener los cambios versionados.
- Los tests desde el inicio sirven de red de seguridad para las fases siguientes.
- Swagger y Postman permiten validar la API sin depender todavía de la app.

### Fase 2 · Autenticación y usuarios (`b8eb47e`)

**Cambios principales:**

- Entidad `Usuario`, `UsuarioRepository` y DTOs de registro/login.
- `AuthService` con BCrypt, `JwtService` y `JwtAuthFilter`.
- Configuración de seguridad stateless y protección de los endpoints de escritura.
- Asociación de cada lugar a su creador y comprobación de propiedad en edición/borrado.
- Tests de autenticación, JWT, seguridad e integración.

**Por qué:**

- Sin autenticación no se puede saber quién crea un lugar ni limitar quién lo edita.
- Se eligió JWT stateless para que la API no dependa de sesiones en servidor y funcione bien con un cliente móvil.
- BCrypt evita almacenar contraseñas en claro.
- La autorización se resolvió en la capa de servicio para que no dependiera solo del controlador.

### Fase 3 · Fotos y comentarios (`f9610c2`)

**Cambios principales:**

- `AlmacenamientoFotos` como interfaz y `AlmacenamientoFotosDisco` como implementación.
- Subida multipart de fotos, validación y servido público desde `/uploads/**`.
- Endpoints de comentarios y cálculo de la valoración media del lugar.
- Ampliación de tests y de la respuesta de lugar con valoración y total de comentarios.

**Por qué:**

- Las fotos no deben guardarse como binarios en PostgreSQL; se guardan en disco y en la base solo queda la URL.
- La interfaz de almacenamiento permite sustituir disco por MinIO en el futuro sin cambiar el servicio.
- La valoración media se calcula en el backend para que todos los clientes reciban el mismo dato.
- Se validan tipo y tamaño de archivo para evitar subidas peligrosas o excesivas.

### Fase 4 · Preparación del entorno y corrección de bugs (`efc8444`, `7df43a5`)

**Cambios principales:**

- `.env.example`, ampliación del README y endurecimiento del `.gitignore`.
- Limpieza de código y DTOs duplicados.
- Revisión del manejo global de errores, JWT, filtros y almacenamiento.
- Test de integración de repositorio con Testcontainers.
- Ajustes de `docker-compose.yml` y de la configuración de la aplicación.

**Por qué:**

- Al probar el flujo completo aparecieron problemas de configuración y código sobrante; se corrigieron antes de empezar la app.
- Tener un `.env.example` evita que cada desarrollador tenga que adivinar las variables necesarias.
- Los tests de integración con PostgreSQL real detectan fallos que los mocks no ven, especialmente en consultas espaciales.
- La limpieza reduce deuda técnica antes de añadir la interfaz Android.

### Fase 5 · Arranque del proyecto Android (`8d10694`)

**Cambios:** generación del proyecto Android con el asistente de Android Studio (plantilla inicial en Kotlin/Compose).

**Por qué:** partir de una base generada por la herramienta oficial asegura que Gradle, el SDK y la configuración del proyecto son correctos. Esa plantilla se sustituyó después por la arquitectura real de la app (Java + Views + MVVM), porque el resto del proyecto y los conocimientos del ciclo formativo encajaban mejor con Java y XML.

### Fase 6 · Android: autenticación y mapa (`4a4756d`)

**Cambios principales:**

- Eliminación de la plantilla Compose y paso a Activities/XML con ViewBinding.
- Capa de red con Retrofit, OkHttp, Gson, `AuthInterceptor` y `ApiResult`.
- `TokenStore` con EncryptedSharedPreferences.
- Pantallas de login y registro con validación.
- `MapActivity` con MapLibre, permiso de ubicación y carga de lugares cercanos.
- Redirección al login ante un 401.
- Tests de repositorios y ViewModels.

**Por qué:**

- MVVM separa la lógica de red y estado de la interfaz, lo que facilita probar los ViewModels.
- EncryptedSharedPreferences protege el token frente a accesos al almacenamiento de la app.
- El interceptor evita repetir la cabecera `Authorization` en cada llamada.
- MapLibre permite usar mapas sin clave de API, importante para un proyecto autoalojado y para las pruebas.
- Centralizar las respuestas en `ApiResult` simplifica el tratamiento de errores y del 401.

### Fase 7 · Android: crear lugares, fotos y comentarios (`f4cc75b`)

**Cambios principales:**

- Pantalla de creación de lugar con selección de punto, nombre, descripción y categoría.
- Pantalla de detalle con fotos, valoración media y comentarios.
- Galería con Glide, Photo Picker y cámara del sistema mediante `FileProvider`.
- Subida de fotos multipart y estados de carga.
- Creación de comentarios con valoración, edición y borrado de lugares propios.
- Filtro por categoría en el mapa.
- Tests de los nuevos ViewModels.

**Por qué:**

- Con esto se completó el MVP: el usuario ya podía aportar contenido, no solo consultarlo.
- La cámara del sistema y Photo Picker evitan pedir permisos amplios de almacenamiento y reducen código propio.
- El detalle se separó en Activity en esta fase; más adelante se convirtió en hoja inferior para mejorar la experiencia sobre el mapa.
- El filtro por categoría responde a la necesidad de encontrar lugares concretos sin recorrer todos los marcadores.

### Fase 8 · Pulido, CI y datos de demostración (`7156c93`)

**Cambios principales:**

- Workflow de GitHub Actions para backend y Android.
- Datos de demostración idempotentes con usuarios, lugares y comentarios.
- Mejoras de seguridad y validación en subida de archivos.
- Sustitución de varias factorías de ViewModel por `SimpleViewModelFactory`.
- Configuración de red de desarrollo separada en `src/debug`.
- Correcciones en login, registro, detalle y mapa.

**Por qué:**

- La integración continua detecta regresiones en cada push.
- Los datos demo permiten enseñar la app sin preparar contenido manualmente.
- `SimpleViewModelFactory` elimina clases repetidas para cada ViewModel.
- Separar la configuración de red por build type evita permitir HTTP en release.
- Las correcciones se hicieron antes de añadir más funcionalidad para no arrastrar fallos.

### Fase 9 · Pulido de interfaz (`15c703f`)

**Cambios principales:**

- El detalle del lugar pasó de Activity a `LugarDetailSheet` (hoja inferior sobre el mapa).
- Nuevos iconos y estilos: estrella, filtro, perfil, salir, badge y asa de la hoja.
- Mejoras en el mapa, el buscador y la barra superior.
- Filtro de tamaño máximo de petición en backend.
- Correcciones de tests y de carga de fotos.

**Por qué:**

- Una hoja inferior mantiene el contexto del mapa y reduce la sensación de navegación profunda.
- Los iconos propios y el estilo visual dan coherencia a la app.
- El límite de tamaño de petición protege al servidor frente a subidas abusivas.
- Los ajustes de tests mantienen la suite alineada con los cambios de UI.

### Fase 10 · Concurso de perros y rankings (`914ee3d`)

**Cambios principales:**

- Migración `V2`: tablas `perro` y `voto_perro`.
- Entidades, repositorios, servicio y endpoints para subir perros y votarlos.
- Ranking de perros por periodo y "mazo" de votación.
- Pantallas `PerroMesActivity` y `ValorarPerrosActivity`.
- `TopZonasActivity` con ranking de lugares.
- Tarjetas y adaptadores para rankings.

**Por qué:**

- Se añadió un componente lúdico y de retención: los usuarios pueden participar y votar.
- El voto se limita a un voto por usuario y perro mediante restricción única en base de datos.
- El ranking se calcula en backend para que el resultado sea consistente.
- Las pantallas reutilizan adaptadores y estilos, evitando duplicar UI.

### Fase 11 · Perfil, favoritos, reportes y administración (`75f1d32`)

**Cambios principales:**

- Migración `V3`: columna `usuario.administrador` y tablas `favorito` y `reporte`.
- Perfil: `GET/PUT /usuarios/me`, mis lugares y fotos de mis perros.
- Favoritos: listar, añadir y quitar; botón de estrella en el detalle y pantalla de favoritos que mueve el mapa al lugar elegido.
- Comentarios: edición y borrado por parte del autor o de un administrador.
- Fotos: borrado por autor o administrador.
- Reportes de lugar, foto y comentario, con motivo obligatorio.
- Rol de administrador: revisión de reportes y borrado de cualquier contenido reportado.
- El email queda como identificador inmutable; solo se puede editar el nombre.

**Por qué:**

- El perfil permite al usuario gestionar su identidad y ver su aportación a la comunidad.
- Los favoritos son una función de uso cotidiano: guardar sitios y volver a ellos desde el mapa.
- La edición/borrado de comentarios y fotos da control sobre el propio contenido.
- Los reportes permiten a la comunidad señalar contenido inadecuado sin necesidad de un moderador permanente.
- El rol de administrador se implementó con una autoridad `ROLE_ADMIN` y una comprobación adicional en servicio, sin duplicar la lógica de negocio.
- El email se mantiene inmutable porque se usa como identificador de la cuenta; cambiarlo invalidaría la sesión y complicaría la coherencia con el token.

### Iteración posterior de interfaz (cambio local pendiente de commit)

En el momento de redactar esta memoria hay un ajuste local sin commitear:

- El botón flotante de añadir lugar usa una huella de perro (`ic_pata.xml`) en lugar del icono genérico `+` (`ic_add.xml`, eliminado).
- Se refuerza así la identidad visual de la app sin cambiar la lógica de creación de lugares.

**Por qué:** el icono `+` era funcional pero genérico; la huella comunica mejor el propósito de la app y la temática canina.

### Fase 12 · Documentación y entrega (actual)

**Cambios:** esta memoria, el README y las instrucciones de ejecución.

**Por qué:** dejar por escrito las decisiones, el proceso seguido y los pasos para que otra persona pueda levantar y evaluar el proyecto sin depender del desarrollador.

---

## 5. Guía de ejecución del proyecto

### 5.1 Arranque completo con Docker

```bash
# 1. Configurar entorno
cp .env.example .env
# Editar .env: POSTGRES_PASSWORD y JWT_SECRET

# 2. Construir y arrancar
docker compose up --build
```

Servicios:

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>

Para ejecutar en segundo plano:

```bash
docker compose up -d --build
docker compose logs -f api
```

Para detener:

```bash
docker compose down
```

Para borrar también los datos y empezar de cero:

```bash
docker compose down -v
```

### 5.2 Cargar datos de demostración

Con los servicios levantados:

```bash
docker compose exec -T db psql -U woofmap -d woofmap -f - < Docs/demo/datos-demo.sql
```

En PowerShell:

```powershell
Get-Content Docs/demo/datos-demo.sql | docker compose exec -T db psql -U woofmap -d woofmap
```

Usuarios de demo (contraseña `woofmap123`):

- `ana@woofmap.app`
- `luis@woofmap.app`

El script es idempotente: se puede ejecutar varias veces sin duplicar datos.

### 5.3 Crear un administrador

El primer administrador se promociona manualmente, ya que no hay una pantalla de gestión de roles:

```sql
UPDATE usuario SET administrador = true WHERE email = 'tu-correo@ejemplo.com';
```

Sustituye el email por el de una cuenta ya registrada.

Tras iniciar sesión con esa cuenta aparecerán la opción "Administración" en el menú del mapa y el botón correspondiente en el perfil.

### 5.4 Ejecutar la app Android

1. Abrir `android/` en Android Studio.
2. Ajustar `API_BASE_URL` en `android/app/build.gradle.kts`:
   - Emulador: `http://10.0.2.2:8080/`.
   - Dispositivo físico: IP local del ordenador, por ejemplo `http://192.168.0.51:8080/`.
3. Sincronizar Gradle.
4. Ejecutar en emulador o dispositivo.

Compilación por línea de comandos:

```bash
cd android
./gradlew :app:assembleDebug
```

El APK se genera en `android/app/build/outputs/apk/debug/`.

En un dispositivo físico, el móvil y el ordenador deben estar en la misma red y el puerto 8080 debe ser accesible. En build `debug` se permite HTTP local; en `release` se exige HTTPS.

### 5.5 Ejecutar los tests

Backend:

```bash
cd WoofMapApi
./mvnw test
```

- Los tests de integración usan Testcontainers y necesitan Docker.
- El informe de cobertura queda en `WoofMapApi/target/site/jacoco/index.html`.

Android:

```bash
cd android
./gradlew :app:testDebugUnitTest
```

### 5.6 Copia de seguridad y restauración

```bash
# Copia
docker compose exec -T db pg_dump -U woofmap -d woofmap -Fc > woofmap.dump

# Restauración sobre una base vacía
docker compose exec -T db pg_restore -U woofmap -d woofmap --clean --if-exists < woofmap.dump
```

### 5.7 Integración continua

El workflow `.github/workflows/ci.yml` se ejecuta en cada push y pull request:

- Backend: Java 25 + `./mvnw -B verify`.
- Android: Java 21 + `./gradlew testDebugUnitTest`.

---

## 6. Estado actual y pendientes

### Completado

- Backend con autenticación JWT, lugares, fotos, comentarios, perros, favoritos, reportes y administración.
- App Android con login, registro, mapa, creación y detalle de lugares, fotos, comentarios, perfil, favoritos, rankings y panel de administración.
- Migraciones Flyway `V1`, `V2` y `V3`.
- Datos de demostración y workflow de CI.
- Tests de backend y de ViewModels/repositorios Android.

### Pendiente o mejorable

- Probar en un dispositivo físico además del emulador.
- Configurar un túnel HTTPS (Cloudflare Tunnel, ngrok o Tailscale) para demostraciones fuera de la red local.
- Verificar el arranque desde cero con `docker compose up` en una máquina limpia.
- Probar una restauración real con `pg_restore`.
- Exportar la documentación OpenAPI y preparar diagramas finales.
- Añadir más pruebas de interfaz (Espresso) si se quiere automatizar la validación visual.

---

## 7. Conclusiones

El desarrollo siguió un enfoque incremental: primero se construyó una API sólida y probada, después se conectó la app Android y finalmente se añadieron funciones de comunidad y moderación. Esta separación permitió que los cambios de la app no obligaran a rediseñar el backend y que cada fase terminara con un entregable funcional.

Las decisiones técnicas priorizaron la simplicidad y la reproducibilidad: un único servicio, PostgreSQL con PostGIS, migraciones versionadas, Docker Compose, JWT stateless y una app Android con MVVM. Las ampliaciones posteriores (perfil, favoritos, reportes, administración y concurso de perros) reutilizaron las entidades, servicios y patrones existentes en lugar de introducir nuevas arquitecturas.

Como líneas futuras destacan el despliegue en la nube, las notificaciones push, la recuperación de contraseña, el almacenamiento de fotos en MinIO y una moderación más avanzada con historial de sanciones.
