-- Juegos base (MySQL) con metadatos
INSERT INTO juego (id, nombre, version, descripcion, desarrollador, genero, max_jugadores, activo, fecha_lanzamiento, roles_disponibles, regiones_soportadas, mmr_minimo, mmr_maximo)
VALUES (1, 'Valorant', '8.03', 'Shooter táctico 5v5', 'Riot Games', 'FPS', 10, TRUE, '2020-06-02 00:00:00', '["DUELIST","INITIATOR","CONTROLLER","SENTINEL","FLEX"]', '["NA","EU","LATAM","APAC"]', 0, 5000)
ON DUPLICATE KEY UPDATE
	nombre=VALUES(nombre), version=VALUES(version), descripcion=VALUES(descripcion), desarrollador=VALUES(desarrollador), genero=VALUES(genero), max_jugadores=VALUES(max_jugadores), activo=VALUES(activo), fecha_lanzamiento=VALUES(fecha_lanzamiento), roles_disponibles=VALUES(roles_disponibles), regiones_soportadas=VALUES(regiones_soportadas), mmr_minimo=VALUES(mmr_minimo), mmr_maximo=VALUES(mmr_maximo);

INSERT INTO juego (id, nombre, version, descripcion, desarrollador, genero, max_jugadores, activo, fecha_lanzamiento, roles_disponibles, regiones_soportadas, mmr_minimo, mmr_maximo)
VALUES (2, 'League of Legends', '14.19', 'MOBA 5v5', 'Riot Games', 'MOBA', 10, TRUE, '2009-10-27 00:00:00', '["TOP","JUNGLE","MID","ADC","SUPPORT"]', '["NA","EU","LATAM","KR","CN"]', 0, 5000)
ON DUPLICATE KEY UPDATE
	nombre=VALUES(nombre), version=VALUES(version), descripcion=VALUES(descripcion), desarrollador=VALUES(desarrollador), genero=VALUES(genero), max_jugadores=VALUES(max_jugadores), activo=VALUES(activo), fecha_lanzamiento=VALUES(fecha_lanzamiento), roles_disponibles=VALUES(roles_disponibles), regiones_soportadas=VALUES(regiones_soportadas), mmr_minimo=VALUES(mmr_minimo), mmr_maximo=VALUES(mmr_maximo);

INSERT INTO juego (id, nombre, version, descripcion, desarrollador, genero, max_jugadores, activo, fecha_lanzamiento, roles_disponibles, regiones_soportadas, mmr_minimo, mmr_maximo)
VALUES (3, 'Counter-Strike 2', 'Release', 'FPS competitivo 5v5', 'Valve', 'FPS', 10, TRUE, '2023-09-27 00:00:00', '["ENTRY","AWPER","RIFLER","IGL","SUPPORT"]', '["NA","EU","LATAM","APAC"]', 0, 5000)
ON DUPLICATE KEY UPDATE
	nombre=VALUES(nombre), version=VALUES(version), descripcion=VALUES(descripcion), desarrollador=VALUES(desarrollador), genero=VALUES(genero), max_jugadores=VALUES(max_jugadores), activo=VALUES(activo), fecha_lanzamiento=VALUES(fecha_lanzamiento), roles_disponibles=VALUES(roles_disponibles), regiones_soportadas=VALUES(regiones_soportadas), mmr_minimo=VALUES(mmr_minimo), mmr_maximo=VALUES(mmr_maximo);

-- Usuarios de ejemplo (más completos). No se usan en el flujo de Postman (se registran usuarios nuevos)
INSERT INTO usuario (id, username, email, password_hash, region, verificacion_estado, notify_push, notify_email, notify_discord, mmr, latencia, rol, rol_preferido, activo)
VALUES (1, 'alice', 'alice@example.com', '$2a$10$K8K0K8K0K8K0K8K0K8K0KuJQe3H9fE2xF1cJQ6WcZr5vYw5mV9aS', 'LATAM', 'VERIFICADO', TRUE, TRUE, TRUE, 1200, 45, 'USUARIO', 'MID', TRUE)
ON DUPLICATE KEY UPDATE
	username=VALUES(username), email=VALUES(email), region=VALUES(region), verificacion_estado=VALUES(verificacion_estado), notify_push=VALUES(notify_push), notify_email=VALUES(notify_email), notify_discord=VALUES(notify_discord), mmr=VALUES(mmr), latencia=VALUES(latencia), rol=VALUES(rol), rol_preferido=VALUES(rol_preferido), activo=VALUES(activo);

INSERT INTO usuario (id, username, email, password_hash, region, verificacion_estado, notify_push, notify_email, notify_discord, mmr, latencia, rol, rol_preferido, activo)
VALUES (2, 'bob', 'bob@example.com', '$2a$10$K8K0K8K0K8K0K8K0K8K0KuJQe3H9fE2xF1cJQ6WcZr5vYw5mV9aS', 'LATAM', 'VERIFICADO', TRUE, TRUE, TRUE, 1150, 52, 'USUARIO', 'TOP', TRUE)
ON DUPLICATE KEY UPDATE
	username=VALUES(username), email=VALUES(email), region=VALUES(region), verificacion_estado=VALUES(verificacion_estado), notify_push=VALUES(notify_push), notify_email=VALUES(notify_email), notify_discord=VALUES(notify_discord), mmr=VALUES(mmr), latencia=VALUES(latencia), rol=VALUES(rol), rol_preferido=VALUES(rol_preferido), activo=VALUES(activo);
