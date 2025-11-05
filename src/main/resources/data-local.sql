-- Juegos base (H2 local)
INSERT INTO juego (id, nombre) VALUES (1, 'Valorant');
INSERT INTO juego (id, nombre) VALUES (2, 'League of Legends');
INSERT INTO juego (id, nombre) VALUES (3, 'Counter-Strike 2');

-- Minimal user seed without violating NOT NULL/unique constraints
-- Usuarios de ejemplo (no colisionan con el flujo, que registra usuarios nuevos con timestamp)
INSERT INTO usuario (id, username, nombre, email, password_hash, region, verificacion_estado, notify_push, notify_email, notify_discord, mmr)
VALUES (1, 'alice', 'alice', 'alice@example.com', '$2a$10$K8K0K8K0K8K0K8K0K8K0KuJQe3H9fE2xF1cJQ6WcZr5vYw5mV9aS', 'LATAM', 'PENDIENTE', TRUE, FALSE, TRUE, 1200);
INSERT INTO usuario (id, username, nombre, email, password_hash, region, verificacion_estado, notify_push, notify_email, notify_discord, mmr)
VALUES (2, 'bob', 'bob', 'bob@example.com', '$2a$10$K8K0K8K0K8K0K8K0K8K0KuJQe3H9fE2xF1cJQ6WcZr5vYw5mV9aS', 'LATAM', 'PENDIENTE', TRUE, FALSE, TRUE, 1150);
