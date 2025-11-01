-- seed-moderator.sql
-- Usage: edit the WHERE clause to match the user you want to promote.
-- Example: to set role by email:
--   UPDATE usuario SET rol = 'MODERADOR' WHERE email = 'mod@example.com';
-- Or by id:
--   UPDATE usuario SET rol = 'MODERADOR' WHERE id = 42;
-- Notes:
-- - Table/column names are based on the JPA entity `Usuario` (class name -> table may be `usuario`).
-- - If your database uses a different schema or table names, adapt accordingly.
-- - Run this SQL against your application's database (H2, Postgres, MySQL) using your preferred DB client or CLI.

-- Example safe flow (replace placeholders):
-- BEGIN TRANSACTION;
-- UPDATE usuario SET rol = 'MODERADOR' WHERE email = 'REPLACE_WITH_EMAIL';
-- COMMIT;

-- Fallback: if your JPA mapping stores roles in a separate table or column name differs, run a SELECT first:
-- SELECT id, username, email, rol FROM usuario WHERE email = 'REPLACE_WITH_EMAIL';

-- End of file
