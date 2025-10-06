-- ===================================================================
-- DATOS INICIALES PARA LA PLATAFORMA DE eSPORTS
-- ===================================================================

-- Insertar usuarios de prueba
INSERT INTO usuarios (username, email, password, discord_id, summoner_name, region, mmr, rol_preferido, rol, activo, fecha_registro, ultima_conexion) VALUES
('admin', 'admin@esports.com', 'admin123', 'admin_discord_123', 'AdminSummoner', 'LAS', 2000, 'MID', 'ADMINISTRADOR', true, NOW(), NOW()),
('moderador1', 'mod1@esports.com', 'mod123', 'mod1_discord_456', 'ModSummoner', 'LAS', 1800, 'SUPPORT', 'MODERADOR', true, NOW(), NOW()),
('player1', 'player1@esports.com', 'pass123', 'player1_discord_789', 'ProPlayer1', 'LAS', 2500, 'ADC', 'USUARIO', true, NOW(), NOW()),
('player2', 'player2@esports.com', 'pass123', 'player2_discord_101', 'ProPlayer2', 'LAS', 2300, 'TOP', 'USUARIO', true, NOW(), NOW()),
('player3', 'player3@esports.com', 'pass123', 'player3_discord_202', 'ProPlayer3', 'LAS', 2100, 'JUNGLE', 'USUARIO', true, NOW(), NOW()),
('player4', 'player4@esports.com', 'pass123', 'player4_discord_303', 'ProPlayer4', 'LAS', 1900, 'MID', 'USUARIO', true, NOW(), NOW()),
('player5', 'player5@esports.com', 'pass123', 'player5_discord_404', 'ProPlayer5', 'LAS', 1700, 'SUPPORT', 'USUARIO', true, NOW(), NOW()),
('player6', 'player6@esports.com', 'pass123', 'player6_discord_505', 'ProPlayer6', 'LAS', 2400, 'ADC', 'USUARIO', true, NOW(), NOW()),
('player7', 'player7@esports.com', 'pass123', 'player7_discord_606', 'ProPlayer7', 'LAS', 2200, 'TOP', 'USUARIO', true, NOW(), NOW()),
('player8', 'player8@esports.com', 'pass123', 'player8_discord_707', 'ProPlayer8', 'LAS', 2000, 'JUNGLE', 'USUARIO', true, NOW(), NOW()),
('player9', 'player9@esports.com', 'pass123', 'player9_discord_808', 'ProPlayer9', 'LAS', 1800, 'MID', 'USUARIO', true, NOW(), NOW()),
('player10', 'player10@esports.com', 'pass123', 'player10_discord_909', 'ProPlayer10', 'LAS', 1600, 'SUPPORT', 'USUARIO', true, NOW(), NOW());

-- Insertar juegos
INSERT INTO juegos (nombre, descripcion, version, desarrollador, genero, max_jugadores, mmr_minimo, mmr_maximo, activo, fecha_lanzamiento, fecha_creacion, regiones_soportadas, roles_disponibles) VALUES
('League of Legends', 'MOBA 5v5 más popular del mundo', '14.19', 'Riot Games', 'MOBA', 10, 0, 5000, true, '2009-10-27', NOW(), 'LAS,LAN,NA,EUW,EUNE,BR,JP,KR,OCE,TR,RU', 'TOP,JUNGLE,MID,ADC,SUPPORT'),
('Valorant', 'Shooter táctico 5v5', '8.12', 'Riot Games', 'FPS', 10, 0, 4000, true, '2020-06-02', NOW(), 'LAS,LAN,NA,EU,BR,AP,KR', 'DUELIST,INITIATOR,CONTROLLER,SENTINEL'),
('CS2', 'Counter-Strike 2', '1.0', 'Valve', 'FPS', 10, 0, 3000, true, '2023-09-27', NOW(), 'SA,NA,EU,ASIA,OCE', 'ENTRY,SUPPORT,AWP,IGL,LURKER');

-- Insertar scrims de ejemplo (ahora con juego_id)
INSERT INTO scrims (nombre, descripcion, mmr_minimo, mmr_maximo, region, fecha_hora, estado, creador_id, juego_id, activo, fecha_creacion, formato) VALUES
('Scrim Pro LAS 2K+', 'Scrim para jugadores de alto nivel en LAS', 2000, 3000, 'LAS', DATEADD('HOUR', 2, NOW()), 'BUSCANDO_JUGADORES', 3, 1, true, NOW(), '5v5'),
('Scrim Intermedio LAS', 'Scrim para jugadores intermedios', 1500, 2200, 'LAS', DATEADD('HOUR', 4, NOW()), 'BUSCANDO_JUGADORES', 4, 1, true, NOW(), '5v5'),
('Scrim Nocturno', 'Scrim para la noche', 1800, 2500, 'LAS', DATEADD('HOUR', 8, NOW()), 'BUSCANDO_JUGADORES', 5, 1, true, NOW(), '5v5');

-- Insertar estadísticas de usuario (requerido por relación OneToOne)
INSERT INTO estadisticas_usuario (usuario_id, mmr_actual, mmr_maximo, partidas_jugadas, partidas_ganadas, partidas_perdidas, winrate, fecha_creacion, ultima_actualizacion) VALUES
(1, 2000, 2100, 150, 85, 65, 0.567, NOW(), NOW()),
(2, 1800, 1950, 120, 72, 48, 0.600, NOW(), NOW()),
(3, 2500, 2600, 200, 130, 70, 0.650, NOW(), NOW()),
(4, 2300, 2400, 180, 108, 72, 0.600, NOW(), NOW()),
(5, 2100, 2200, 160, 88, 72, 0.550, NOW(), NOW()),
(6, 1900, 2000, 140, 77, 63, 0.550, NOW(), NOW()),
(7, 1700, 1800, 100, 52, 48, 0.520, NOW(), NOW()),
(8, 2400, 2500, 220, 154, 66, 0.700, NOW(), NOW()),
(9, 2200, 2300, 190, 114, 76, 0.600, NOW(), NOW()),
(10, 2000, 2100, 170, 102, 68, 0.600, NOW(), NOW()),
(11, 1800, 1900, 130, 71, 59, 0.546, NOW(), NOW()),
(12, 1600, 1700, 110, 55, 55, 0.500, NOW(), NOW());

-- Insertar algunas postulaciones
INSERT INTO postulaciones (usuario_id, scrim_id, estado, rol_solicitado, fecha_postulacion, comentario) VALUES
(6, 1, 'PENDIENTE', 'ADC', NOW(), 'Tengo experiencia en ranked'),
(7, 1, 'PENDIENTE', 'TOP', NOW(), 'Main top desde season 8'),
(8, 1, 'ACEPTADA', 'JUNGLE', NOW(), 'Especialista en control de mapa'),
(9, 2, 'PENDIENTE', 'MID', NOW(), 'Prefiero champions de control'),
(10, 2, 'PENDIENTE', 'SUPPORT', NOW(), 'Main support, buen wardeo'),
(11, 3, 'ACEPTADA', 'MID', NOW(), 'Disponible para horario nocturno'),
(12, 3, 'PENDIENTE', 'SUPPORT', NOW(), 'Flexible con horarios');

-- Insertar notificaciones de ejemplo
INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo, leida, fecha_creacion) VALUES
(3, '¡Bienvenido!', 'Tu cuenta ha sido creada exitosamente. ¡Comienza a buscar scrims!', 'NUEVA_POSTULACION', false, NOW()),
(6, 'Nueva Postulación', 'Te has postulado exitosamente al scrim ''Scrim Pro LAS 2K+''', 'NUEVA_POSTULACION', false, NOW()),
(8, 'Postulación Aceptada', 'Tu postulación para el scrim ''Scrim Pro LAS 2K+'' ha sido aceptada', 'POSTULACION_ACEPTADA', false, NOW()),
(11, 'Postulación Aceptada', 'Tu postulación para el scrim ''Scrim Nocturno'' ha sido aceptada', 'POSTULACION_ACEPTADA', false, NOW());

-- Insertar historial de usuarios
INSERT INTO historial_usuario (usuario_id, tipo_evento, descripcion, fecha_evento, mmr_antes, mmr_despues) VALUES
(3, 'SCRIM_COMPLETADO', 'Completó scrim exitosamente', DATEADD('DAY', -1, NOW()), 2480, 2500),
(4, 'SCRIM_COMPLETADO', 'Completó scrim exitosamente', DATEADD('DAY', -1, NOW()), 2280, 2300),
(8, 'POSTULACION_ACEPTADA', 'Postulación aceptada en Scrim Pro LAS 2K+', NOW(), NULL, NULL);

-- Insertar un reporte de ejemplo
INSERT INTO reportes (reportador_id, reportado_id, motivo, descripcion, estado, fecha_reporte) VALUES
(3, 12, 'Comportamiento tóxico', 'El jugador fue tóxico durante el scrim, insultando a compañeros', 'PENDIENTE', NOW());

-- Crear usuario bot del sistema
INSERT INTO usuarios (username, email, password, discord_id, region, mmr, rol_preferido, rol, activo, fecha_registro, ultima_conexion) VALUES
('SYSTEM_BOT', 'bot@sistema.com', 'N/A', 'system_bot', 'GLOBAL', 0, 'FLEX', 'MODERADOR', true, NOW(), NOW());