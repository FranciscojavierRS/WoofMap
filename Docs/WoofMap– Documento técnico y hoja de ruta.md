# WoofMap – Documento técnico y hoja de ruta

*Proyecto de Desarrollo de Aplicaciones Multiplataforma · Nombre provisional: WoofMap*

## 1. Introducción

### 1.1 Descripción del proyecto

WoofMap es una aplicación Android que permite a los dueños de perros descubrir y compartir los mejores lugares para visitar con sus mascotas (parques, playas, terrazas y locales dog-friendly, rutas de paseo, etc.). Los usuarios marcan lugares en un mapa y los enriquecen con fotos, valoraciones y comentarios, de modo que se construye una guía colaborativa.

### 1.2 Objetivos

**Objetivo general:** desarrollar una aplicación móvil Android con un backend propio que permita la gestión colaborativa de lugares aptos para perros sobre un mapa.

**Objetivos específicos:**

- Diseñar e implementar una API REST que gestione usuarios, lugares, fotos y comentarios.
- Implementar registro y autenticación segura de usuarios.
- Desarrollar una app Android con mapa interactivo para visualizar y crear lugares.
- Permitir la subida de fotografías y la publicación de comentarios y valoraciones.
- Desplegar todo el sistema de forma reproducible mediante contenedores Docker.

### 1.3 Alcance

Incluido: registro/login, mapa con lugares, creación de lugares, fotos, comentarios y valoraciones, búsqueda por proximidad y despliegue local con Docker Compose.

Fuera de alcance (posibles ampliaciones): app iOS, notificaciones push, recuperación de contraseña por email, moderación avanzada, inicio de sesión con Google.

## 2. Requisitos

### 2.1 Requisitos funcionales

| ID | Requisito |
| --- | --- |
| RF01 | El usuario puede registrarse con nombre, email y contraseña. |
| RF02 | El usuario puede iniciar y cerrar sesión. |
| RF03 | El usuario puede ver en un mapa los lugares cercanos a su posición. |
| RF04 | El usuario autenticado puede crear un lugar marcando un punto en el mapa, con nombre, descripción y categoría. |
| RF05 | El usuario autenticado puede subir una o varias fotos a un lugar. |
| RF06 | El usuario autenticado puede comentar y valorar un lugar. |
| RF07 | El usuario puede consultar el detalle de un lugar (fotos, comentarios, valoración media). |
| RF08 | El creador de un lugar puede editarlo o eliminarlo. |
| RF09 | El usuario puede filtrar lugares por categoría. |

### 2.2 Requisitos no funcionales

| ID | Requisito |
| --- | --- |
| RNF01 | Las contraseñas se almacenan cifradas con BCrypt. |
| RNF02 | La API es stateless y se protege con tokens JWT. |
| RNF03 | Todo el sistema se levanta con un único comando (`docker compose up`). |
| RNF04 | Las consultas geográficas deben responder de forma eficiente mediante índices espaciales. |
| RNF05 | Las fotos se validan por tipo y tamaño máximo. |
| RNF06 | La API está documentada con OpenAPI/Swagger. |
| RNF07 | Las credenciales y secretos no se almacenan en el repositorio. |
| RNF08 | El backend se desarrolla en Java 25 (LTS) y compila sin funciones en vista previa. |
| RNF09 | El backend se desarrolla con TDD y cumple los principios SOLID y KISS, con cobertura orientativa de al menos 80 % en servicios y dominio. |

## 3. Arquitectura del sistema

### 3.1 Visión general

El sistema sigue una arquitectura cliente-servidor de tres capas: cliente móvil, API REST (microservicio) y persistencia. El cliente nunca accede directamente a la base de datos; toda la comunicación se realiza a través de la API mediante HTTPS y JSON.

```
[App Android] ⇄ HTTPS/JSON (REST) ⇄ [Microservicio Spring Boot] ⇄ [PostgreSQL + PostGIS]
                                              ⇩
                                   [Almacenamiento de fotos]
```

### 3.2 Justificación de un único microservicio

Para el tamaño y los plazos del proyecto, un único servicio REST es la opción más adecuada. Dividirlo en varios servicios (usuarios, lugares, fotos) aumentaría la complejidad operativa (comunicación entre servicios, despliegue, trazabilidad) sin aportar un beneficio real. El código se organiza internamente en módulos (auth, lugares, fotos, comentarios) para facilitar una futura separación.

### 3.3 Despliegue

El sistema se autoaloja en el equipo del desarrollador mediante Docker Compose, con los siguientes servicios:

- **api:** microservicio Spring Boot construido con un Dockerfile multi-stage.
- **db:** imagen `postgis/postgis` (PostgreSQL con extensión PostGIS).
- **Volúmenes:** `db_data` para la base de datos y `uploads` para las fotografías.

Para demostraciones fuera de la red local se utilizará un túnel (Cloudflare Tunnel, ngrok o Tailscale), que además proporciona HTTPS.

## 4. Stack tecnológico y metodología

### 4.1 Cliente Android

| Función | Tecnología | Justificación |
| --- | --- | --- |
| Lenguaje | Java | Conocimiento previo del ciclo formativo. |
| IDE | Android Studio | Herramienta oficial. |
| Arquitectura | MVVM (ViewModel + LiveData) | Separación de responsabilidades y recomendada por Google. |
| Mapa | MapLibre / OSMDroid o Google Maps SDK | Las alternativas abiertas no requieren cuenta de facturación. |
| Red | Retrofit + OkHttp + Gson/Moshi | Estándar para consumo de APIs REST. |
| Imágenes | Glide | Carga y caché eficiente de imágenes. |
| Cámara/galería | Photo Picker + CameraX | APIs modernas de Android. |
| Ubicación | FusedLocationProviderClient | Localización precisa y de bajo consumo. |
| Almacenamiento seguro | EncryptedSharedPreferences | Guardado cifrado del token JWT. |
| Caché local (opcional) | Room | Funcionamiento básico sin conexión. |

### 4.2 Backend

| Función | Tecnología |
| --- | --- |
| Framework | Java 25 (LTS) + Spring Boot con soporte oficial para Java 25 |
| Acceso a datos | Spring Data JPA (Hibernate) |
| Seguridad | Spring Security + JWT (jjwt 0.12.x) |
| Validación | Bean Validation |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Migraciones | Flyway |
| Build | Maven |
| Tests | JUnit 5 + Mockito + AssertJ + Testcontainers + JaCoCo (desarrollo TDD) |

### 4.3 Datos y almacenamiento

- **PostgreSQL + PostGIS:** permite consultas espaciales como "lugares a menos de X km" con índices geográficos, algo que MySQL no resuelve con la misma solvencia.
- **Fotografías:** no se almacenan en la base de datos. Se guardan en un volumen Docker servido por la API (alternativa: MinIO) y en la BD solo se registra la ruta o URL.

### 4.4 Herramientas de apoyo

Git y GitHub (monorepo), Docker y Docker Compose, Postman para pruebas de la API, Swagger UI para documentación interactiva.

### 4.5 Java 25 en el backend

El backend usa Java 25, la última versión LTS (septiembre de 2025), con Eclipse Temurin 25 en las imágenes Docker (`maven:3.9-eclipse-temurin-25` para compilar y `eclipse-temurin:25-jre` para ejecutar). La versión de Spring Boot se elegirá comprobando su soporte oficial de Java 25 al generar el proyecto.

| Característica | Uso en el proyecto |
| --- | --- |
| Records | DTOs de entrada y salida inmutables (`RegisterRequest`, `AuthResponse`, `LugarRequest`). |
| Interfaces selladas y pattern matching en `switch` | Resultados de dominio explícitos (por ejemplo, un registro es `Creado` o `EmailDuplicado`) en lugar de excepciones como control de flujo; el compilador avisa si falta un caso. |
| Hilos virtuales | Con `spring.threads.virtual.enabled=true`, las peticiones que esperan a la BD o al disco escalan sin código reactivo; desde Java 24 `synchronized` ya no los bloquea (JEP 491). |
| Scoped Values (definitivos en Java 25) | Alternativa inmutable a `ThreadLocal` para propagar datos de la petición; solo si hace falta (KISS). |
| Cuerpos de constructor flexibles (definitivos en Java 25) | Validar argumentos antes de llamar a `super(...)` o `this(...)`. |
| Bloques de texto, `var` y expresiones `switch` | Consultas SQL nativas de PostGIS legibles y código más conciso. |
| Cabeceras de objeto compactas (opcional) | `-XX:+UseCompactObjectHeaders` reduce la memoria del contenedor; se activa solo si se mide una mejora. |

Las funciones en vista previa de Java 25 (por ejemplo, Structured Concurrency) no se usarán, para no depender de `--enable-preview`.

### 4.6 Metodología y principios de diseño

El backend se desarrolla con TDD y debe cumplir SOLID y KISS; el detalle de las pruebas está en el apartado 9. Cada funcionalidad empieza con un test que falla, se implementa lo mínimo para que pase y se refactoriza con los tests como red de seguridad.

| Principio | Aplicación en WoofMap |
| --- | --- |
| S: responsabilidad única | Controlador (HTTP), servicio (reglas de negocio) y repositorio (persistencia) separados; `AuthService` y `JwtService` son clases distintas. |
| O: abierto/cerrado | `AlmacenamientoFotos` es una interfaz con implementación en disco; pasar a MinIO añade una clase sin tocar el servicio. |
| L: sustitución de Liskov | Las implementaciones respetan el contrato de su interfaz; en los tests se sustituyen por dobles sin cambiar el comportamiento esperado. |
| I: segregación de interfaces | Interfaces pequeñas y DTOs distintos para crear, editar y consultar, para no depender de lo que no se usa. |
| D: inversión de dependencias | Los servicios dependen de abstracciones inyectadas por constructor, lo que permite probarlos con Mockito. |

**KISS en la práctica:**

- Un único microservicio y un único módulo Maven.
- No se crea una abstracción hasta que hay dos usos reales que la justifiquen.
- Se prefiere lo que ya ofrecen Java y Spring antes que código propio o librerías nuevas.
- Métodos cortos, un solo nivel de abstracción por método y nombres que expresan la intención.
- Si SOLID y KISS chocan, se elige la solución más simple que mantenga la responsabilidad única y la inversión de dependencias.

**Definición de "hecho" para cada tarea de backend:**

- [ ] Hubo un test que falló antes de escribir el código.
- [ ] Todos los tests pasan en local y en GitHub Actions.
- [ ] La cobertura de servicios y dominio no baja del objetivo.
- [ ] El código se ha revisado contra SOLID y KISS (sin duplicación ni abstracciones innecesarias).
- [ ] El endpoint está documentado en Swagger y probado en Postman.

## 5. Diseño de la base de datos

### 5.1 Entidades

| Entidad | Campos principales |
| --- | --- |
| **Usuario** | id, nombre, email (único), password\_hash, fecha\_alta |
| **Lugar** | id, nombre, descripción, categoría, ubicación (geography Point), usuario\_id, fecha\_creación |
| **Foto** | id, url, lugar\_id, usuario\_id, fecha\_subida |
| **Comentario** | id, texto, valoración (1-5), lugar\_id, usuario\_id, fecha |

### 5.2 Relaciones

- Un usuario crea muchos lugares, fotos y comentarios (1:N).
- Un lugar tiene muchas fotos y muchos comentarios (1:N).

### 5.3 Consideraciones

- Índice espacial GIST sobre la columna de ubicación del lugar.
- Restricción UNIQUE sobre el email del usuario.
- Esquema versionado con migraciones Flyway.

## 6. Diseño de la API REST

| Método | Endpoint | Descripción | Auth |
| --- | --- | --- | --- |
| POST | /auth/register | Registro de usuario | No |
| POST | /auth/login | Inicio de sesión | No |
| GET | /lugares?lat=&lon=&radio= | Lugares cercanos | No |
| GET | /lugares/{id} | Detalle de un lugar | No |
| POST | /lugares | Crear lugar | Sí |
| PUT | /lugares/{id} | Editar lugar (solo creador) | Sí |
| DELETE | /lugares/{id} | Eliminar lugar (solo creador) | Sí |
| POST | /lugares/{id}/fotos | Subir foto (multipart) | Sí |
| GET | /lugares/{id}/comentarios | Listar comentarios | No |
| POST | /lugares/{id}/comentarios | Comentar y valorar | Sí |

La API devuelve DTOs (nunca entidades directamente) y utiliza códigos HTTP estándar (200, 201, 400, 401, 403, 404, 409).

## 7. Seguridad

- **Autenticación stateless con JWT:** el login devuelve un token firmado (HMAC) con caducidad que el cliente envía en la cabecera `Authorization: Bearer <token>`.
- **Contraseñas:** almacenadas con BCrypt; nunca se devuelven en las respuestas.
- **Autorización:** el identificador del usuario se obtiene siempre del contexto de seguridad, nunca del cuerpo de la petición. La edición y el borrado se limitan al creador del recurso.
- **Cliente Android:** el token se guarda en EncryptedSharedPreferences y se añade automáticamente mediante un interceptor de OkHttp. Ante un 401 se elimina el token y se redirige al login.
- **Subida de archivos:** validación de tipo MIME, extensión y tamaño máximo; renombrado del archivo con identificador aleatorio.
- **Secretos:** `JWT_SECRET` y credenciales de BD en un fichero `.env` excluido de Git.
- **Transporte:** HTTPS mediante túnel en demostraciones; HTTP solo en desarrollo local, con `network_security_config.xml` restringido al host de desarrollo.

## 8. Estructura del repositorio

```
WoofMap/
├── docker-compose.yml
├── .env                (no versionado)
├── .gitignore
├── README.md
├── docs/               (memoria, diagramas, capturas)
├── backend/            (Spring Boot + Dockerfile)
└── android/            (proyecto Android Studio)
```

## 9. Estrategia de pruebas (TDD)

- **Ciclo TDD:** rojo (test que falla), verde (código mínimo que lo hace pasar) y refactor (limpiar sin romper los tests), con commits pequeños por ciclo. No se escribe código de producción del backend sin un test que falle antes.
- **Tests unitarios (la base):** servicios y lógica de dominio con JUnit 5, Mockito y AssertJ, sin Spring ni base de datos.
- **Tests de capa:** `@WebMvcTest` para controladores y seguridad; `@DataJpaTest` para repositorios sobre PostgreSQL con PostGIS real mediante Testcontainers (H2 no soporta PostGIS).
- **Tests de integración:** `@SpringBootTest` con Testcontainers para los flujos completos (registro, login, crear lugar, subir foto); pocos y bien elegidos.
- **Seguridad:** acceso sin token, token caducado y recursos de otros usuarios, cada caso como test automatizado.
- **API:** colección de Postman como prueba de humo manual.
- **Android:** TDD en ViewModels y repositorios con JUnit y Mockito; la interfaz se valida con pruebas manuales en emulador y dispositivo físico (Espresso opcional).
- **Cobertura:** JaCoCo con objetivo orientativo de al menos 80 % en servicios y dominio; es una señal de calidad, no un fin.
- **Integración continua:** GitHub Actions ejecuta todos los tests en cada push.

## 10. Riesgos y mitigación

| Riesgo | Mitigación |
| --- | --- |
| El móvil no alcanza el servidor en la red local | Usar IP fija, abrir el puerto en el firewall o emplear un túnel. |
| Android bloquea tráfico HTTP | Configuración de red de desarrollo o uso de HTTPS vía túnel. |
| Curva de aprendizaje de Spring Boot | Avanzar por hitos pequeños y probar cada endpoint en Postman. |
| Complejidad del mapa y la geolocalización | Empezar con marcadores simples y ampliar progresivamente. |
| Pérdida de datos al estar autoalojado | Copias periódicas con `pg_dump`. |
| Retrasos en el calendario | Priorizar el MVP y dejar las mejoras como ampliaciones. |
| Alguna librería no soporta aún Java 25 | Comprobar la compatibilidad al añadir cada dependencia; usar versiones recientes y fijar versiones en el pom.xml. |
| TDD ralentiza el avance al principio | Ciclos cortos, tests simples y tests de integración solo para los flujos clave. |

## 11. Hoja de ruta

La planificación se divide en fases con tareas verificables. Cada fase termina con un entregable funcional. Marca cada tarea al completarla. Todas las tareas de backend siguen TDD: primero el test que falla, después el código mínimo que lo hace pasar y por último el refactor.

### Fase 0 · Preparación y entorno

*Entregable: repositorio y herramientas listas.*

- [X] Instalar JDK 25 (LTS), Maven, Docker Desktop, Android Studio y Postman.
- [X] Crear el repositorio en GitHub (monorepo) con `.gitignore` y `README.md`.
- [X] Crear la estructura de carpetas (`backend`, `android`, `docs`).
- [X] Definir el nombre definitivo de la app y las categorías de lugares.
- [ ] Elaborar bocetos (wireframes) de las pantallas principales.
- [X] Decidir la librería de mapas (MapLibre/OSMDroid o Google Maps).
- [X] Configurar Maven para compilar con Java 25 y confirmar la versión de Spring Boot compatible.
- [X] Preparar la base de tests: JUnit 5, Mockito, AssertJ, Testcontainers (PostGIS) y JaCoCo.
- [ ] Configurar GitHub Actions para ejecutar los tests en cada push.
- [ ] Acordar la definición de "hecho" (TDD, SOLID y KISS) del apartado 4.6.

### Fase 1 · Backend base y base de datos

*Entregable: API con CRUD de lugares funcionando en Docker.*

- [ ] Generar el proyecto Spring Boot (Web, JPA, PostgreSQL, Validation, Flyway, Security, SpringDoc).
- [ ] Escribir el `docker-compose.yml` con los servicios `db` y `api`, y el fichero `.env`.
- [ ] Escribir el Dockerfile multi-stage del backend.
- [ ] Crear las migraciones Flyway: extensión PostGIS, tablas de usuarios, lugares, fotos y comentarios.
- [ ] Crear entidades JPA y repositorios.
- [ ] Implementar el CRUD de lugares con DTOs (records) y validación, escribiendo antes los tests de cada caso de uso.
- [ ] Implementar la consulta de lugares cercanos (`lat`, `lon`, `radio`) con PostGIS.
- [ ] Añadir manejo global de errores (`@ControllerAdvice`).
- [ ] Activar Swagger UI y probar los endpoints en Postman.

### Fase 2 · Autenticación y usuarios

*Entregable: registro y login con JWT, endpoints protegidos.*

- [ ] Crear la entidad `Usuario` y su repositorio.
- [ ] Implementar `JwtService` (generación y validación de tokens).
- [ ] Implementar `AuthService`, `POST /auth/register` y `POST /auth/login` con BCrypt.
- [ ] Implementar `JwtAuthFilter` y la configuración de `SecurityFilterChain`.
- [ ] Asociar cada lugar a su creador usando el usuario autenticado.
- [ ] Restringir edición y borrado al creador (403 en caso contrario).
- [ ] Escribir primero los tests de AuthService y JwtService (TDD) y aplicar después su implementación.
- [ ] Probar los flujos de seguridad en Postman (sin token, token inválido, recurso ajeno).

### Fase 3 · Fotos y comentarios (backend)

*Entregable: API completa.*

- [ ] Configurar el volumen `uploads` y el servicio de almacenamiento de ficheros.
- [ ] Implementar `POST /lugares/{id}/fotos` (multipart) con validación de tipo y tamaño.
- [ ] Servir las imágenes de forma pública desde `/uploads/**`.
- [ ] Implementar los endpoints de comentarios y valoración.
- [ ] Calcular la valoración media de cada lugar.
- [ ] Ampliar la colección de Postman y los tests de integración.

### Fase 4 · Android: base, autenticación y mapa

*Entregable: app que permite iniciar sesión y ver lugares en el mapa.*

- [ ] Crear el proyecto Android (Java, minSdk adecuado) con estructura MVVM.
- [ ] Configurar Retrofit, OkHttp y el `AuthInterceptor`.
- [ ] Configurar `network_security_config.xml` para desarrollo (host `10.0.2.2` o IP local).
- [ ] Implementar `TokenStore` con EncryptedSharedPreferences.
- [ ] Pantallas de Login y Registro con validación en cliente.
- [ ] Lógica de arranque: si hay token válido, ir directamente al mapa.
- [ ] Integrar el mapa y solicitar el permiso de ubicación.
- [ ] Centrar el mapa en la posición del usuario y cargar lugares cercanos desde la API.
- [ ] Gestionar el 401 (token caducado) redirigiendo al login.

### Fase 5 · Android: crear lugares, fotos y comentarios

*Entregable: funcionalidad completa del MVP.*

- [ ] Pantalla de creación de lugar: marcar punto en el mapa, nombre, descripción y categoría.
- [ ] Pantalla de detalle del lugar (información, fotos, valoración media).
- [ ] Selección de fotos con Photo Picker o captura con CameraX.
- [ ] Subida de fotos (multipart) con indicador de progreso.
- [ ] Galería de fotos en el detalle con Glide.
- [ ] Listado y creación de comentarios con valoración.
- [ ] Edición y borrado de los lugares propios.
- [ ] Filtro por categoría en el mapa.

### Fase 6 · Pulido, pruebas y despliegue

*Entregable: sistema estable y demostrable.*

- [ ] Revisión de calidad del backend: cobertura, código muerto y cumplimiento de SOLID y KISS en cada módulo.
- [ ] Revisar manejo de errores y mensajes al usuario (sin conexión, errores del servidor).
- [ ] Añadir estados de carga y vistas vacías.
- [ ] Probar en emulador y en al menos un dispositivo físico.
- [ ] Configurar el túnel (Cloudflare Tunnel, ngrok o Tailscale) y probar la demo fuera de la red local.
- [ ] Verificar que `docker compose up` levanta todo desde cero.
- [ ] Realizar una copia de seguridad con `pg_dump` y probar la restauración.
- [ ] Añadir datos de ejemplo para la demostración.

### Fase 7 · Documentación y entrega

*Entregable: memoria y presentación finalizadas.*

- [ ] Completar el `README.md` con instrucciones de instalación y ejecución.
- [ ] Exportar la documentación OpenAPI de la API.
- [ ] Preparar diagramas finales (arquitectura, entidad-relación, casos de uso).
- [ ] Capturas de pantalla de la aplicación.
- [ ] Redactar la memoria final (basada en este documento) con conclusiones y líneas futuras.
- [ ] Preparar la presentación y ensayar la demostración.

### Ampliaciones opcionales

- [ ] Refresh token y caducidad corta del token de acceso.
- [ ] Caché local con Room.
- [ ] Inicio de sesión con Google.
- [ ] Favoritos y lugares guardados por usuario.
- [ ] Rol de moderador y sistema de reportes.
- [ ] Migrar el almacenamiento de fotos a MinIO.

## 12. Hitos principales

| Hito | Resultado | Fase |
| --- | --- | --- |
| H1 | Backend dockerizado con CRUD de lugares | 1 |
| H2 | Registro, login y API protegida | 2 |
| H3 | API completa con fotos y comentarios | 3 |
| H4 | App con login y mapa mostrando lugares | 4 |
| H5 | MVP completo (crear lugares, fotos, comentarios) | 5 |
| H6 | Sistema estable y desplegado en local | 6 |
| H7 | Memoria y presentación entregadas | 7 |

## 13. Conclusiones y líneas futuras

*(Apartado a completar al finalizar el proyecto.)* Posibles líneas futuras: versión iOS, notificaciones push, recomendaciones personalizadas, moderación comunitaria y despliegue en la nube.
