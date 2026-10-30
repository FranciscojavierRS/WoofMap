-- Datos de ejemplo para la demostración de WoofMap.
-- Aplicar sobre una base de datos ya migrada por Flyway:
--   docker compose exec -T db psql -U woofmap -d woofmap -f - < Docs/demo/datos-demo.sql   (bash)
--   Get-Content Docs/demo/datos-demo.sql | docker compose exec -T db psql -U woofmap -d woofmap   (PowerShell)
--
-- Usuarios de demo (contraseña: woofmap123):
--   ana@woofmap.app
--   luis@woofmap.app
-- El script es idempotente: se puede ejecutar varias veces sin duplicar datos.

INSERT INTO usuario (nombre, email, password_hash) VALUES
    ('Ana',  'ana@woofmap.app',  '$2b$10$5sXv5t7W.Gs4msqFR1uh8.ha0T/jBYIaTU1bjBdHDLLuEkp7jTzSe'),
    ('Luis', 'luis@woofmap.app', '$2b$10$5sXv5t7W.Gs4msqFR1uh8.ha0T/jBYIaTU1bjBdHDLLuEkp7jTzSe')
ON CONFLICT (email) DO NOTHING;

INSERT INTO lugar (nombre, descripcion, categoria, ubicacion, usuario_id)
SELECT v.nombre, v.descripcion, v.categoria,
       ST_SetSRID(ST_MakePoint(v.lon, v.lat), 4326)::geography,
       u.id
FROM (VALUES
    ('Parque del Retiro', 'El pulmón de Madrid. Zonas amplias y sombra para pasear con el perro.', 'PARQUE', -3.6844, 40.4153, 'ana@woofmap.app'),
    ('Zona canina del Retiro', 'Área vallada junto a la Puerta de Alcalá donde los perros pueden ir sueltos.', 'ZONA DE JUEGOS', -3.6820, 40.4140, 'luis@woofmap.app'),
    ('Casa de Campo', 'Parque enorme con senderos, pinos y zonas de descanso. Ideal para paseos largos.', 'PARQUE', -3.7480, 40.4190, 'ana@woofmap.app'),
    ('Parque del Oeste', 'Rosaleda y praderas con vistas al Palacio Real. Muy tranquilo entre semana.', 'PARQUE', -3.7270, 40.4283, 'luis@woofmap.app'),
    ('Madrid Río', 'Paseo junto al Manzanares con fuentes y terrazas dog-friendly.', 'PARQUE', -3.7170, 40.4000, 'ana@woofmap.app'),
    ('Terraza El Perro Verde', 'Bar con terraza donde los perros son bienvenidos y siempre tienen agua.', 'RESTAURANTE DOG-FRIENDLY', -3.7050, 40.4230, 'luis@woofmap.app'),
    ('Clínica Veterinaria Retiro', 'Urgencias veterinarias y consulta general. Abierto también los sábados.', 'VETERINARIO', -3.6890, 40.4210, 'ana@woofmap.app'),
    ('Fuente del Pequeño Recinto', 'Fuente con agua potable en pleno Retiro, junto a la zona canina.', 'FUENTE DE AGUA', -3.6835, 40.4165, 'luis@woofmap.app'),
    ('Mascotas Salamanca', 'Tienda de alimentación y accesorios. Permiten entrar con el perro.', 'TIENDA DE MASCOTAS', -3.6800, 40.4300, 'ana@woofmap.app'),
    ('Parque Juan Carlos I', 'Amplias praderas y laguna. Hay una zona de agility para perros.', 'PARQUE', -3.6100, 40.4620, 'luis@woofmap.app')
) AS v(nombre, descripcion, categoria, lon, lat, email)
JOIN usuario u ON u.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM lugar l WHERE l.nombre = v.nombre);

INSERT INTO comentario (texto, valoracion, lugar_id, usuario_id)
SELECT v.texto, v.valoracion, l.id, u.id
FROM (VALUES
    ('Mi perro disfruta muchísimo aquí. Hay sombra y fuentes cerca.', 5, 'Parque del Retiro', 'luis@woofmap.app'),
    ('Muy amplio, pero los fines de semana se llena de gente.', 4, 'Parque del Retiro', 'ana@woofmap.app'),
    ('La zona vallada está genial para soltar al perro con seguridad.', 5, 'Zona canina del Retiro', 'ana@woofmap.app'),
    ('Nos trataron muy bien y trajeron un cuenco de agua sin pedirlo.', 5, 'Terraza El Perro Verde', 'ana@woofmap.app'),
    ('Buena atención en urgencias, aunque la espera fue larga.', 4, 'Clínica Veterinaria Retiro', 'luis@woofmap.app'),
    ('Perfecto para pasear al atardecer junto al río.', 5, 'Madrid Río', 'luis@woofmap.app'),
    ('Hay una zona de agility que a mi perro le encanta.', 4, 'Parque Juan Carlos I', 'ana@woofmap.app')
) AS v(texto, valoracion, lugar_nombre, email)
JOIN lugar l ON l.nombre = v.lugar_nombre
JOIN usuario u ON u.email = v.email
WHERE NOT EXISTS (
    SELECT 1 FROM comentario c
    WHERE c.lugar_id = l.id AND c.usuario_id = u.id AND c.texto = v.texto
);
