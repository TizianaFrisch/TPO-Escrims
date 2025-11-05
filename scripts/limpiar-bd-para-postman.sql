-- ========================================
-- Script de limpieza de BD para testing
-- ========================================
-- Ejecutar antes de correr Postman Collection
-- Para empezar con BD limpia sin conflictos
-- ========================================

SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar todas las tablas en orden correcto
DELETE FROM estadisticas_jugador_match;
DELETE FROM eventos_match;
DELETE FROM miembros_equipo;
DELETE FROM equipos;
DELETE FROM matches;
DELETE FROM historial_usuario;
DELETE FROM feedback;
DELETE FROM reporte_conducta;
DELETE FROM confirmacion;
DELETE FROM postulacion;
DELETE FROM waitlist_entry;
DELETE FROM notificacion;
DELETE FROM confirmation_token;
DELETE FROM busqueda_favorita;
DELETE FROM usuario_roles_preferidos;
DELETE FROM scrim;
DELETE FROM usuario;
DELETE FROM juego;
DELETE FROM log_auditoria;

SET FOREIGN_KEY_CHECKS = 1;

-- Reinsertar datos básicos de juegos
INSERT INTO juego (id, nombre, genero) VALUES 
(1, 'League of Legends', 'MOBA'),
(2, 'Valorant', 'FPS'),
(3, 'Counter-Strike 2', 'FPS');

-- Verificar limpieza
SELECT 'BD limpiada exitosamente! ✅' AS resultado;
SELECT COUNT(*) AS usuarios_restantes FROM usuario;
SELECT COUNT(*) AS scrims_restantes FROM scrim;
SELECT COUNT(*) AS juegos_disponibles FROM juego;

-- Reinsertar datos básicos de juegos
INSERT INTO juego (id, nombre, genero) VALUES 
(1, 'League of Legends', 'MOBA'),
(2, 'Valorant', 'FPS'),
(3, 'Counter-Strike 2', 'FPS');

-- Verificar limpieza
SELECT 'BD limpiada exitosamente! ✅' AS resultado;
SELECT COUNT(*) AS usuarios_restantes FROM usuario;
SELECT COUNT(*) AS scrims_restantes FROM scrim;
SELECT COUNT(*) AS juegos_disponibles FROM juego;
