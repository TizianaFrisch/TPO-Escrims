-- convert-pk-to-bigint.sql (automated)
-- Purpose: Safely convert INT primary keys (usuario, juego, scrim) to BIGINT to match JPA Long ids.
-- The script will:
--  1) Discover existing single-column foreign key constraints that reference usuario/juego/scrim
--  2) Capture ADD CONSTRAINT statements for later re-creation
--  3) DROP those foreign keys
--  4) ALTER the PK columns to BIGINT (preserving UNSIGNED if present)
--  5) Re-create the foreign keys with the original names
-- IMPORTANT: BACKUP your database before running this script. DDL operations are not transactional in MySQL.

-- USAGE (PowerShell example):
-- mysqldump -u root -p scrims_db > c:\backups\scrims_db_backup.sql
-- mysql -u root -p scrims_db < scripts/convert-pk-to-bigint.sql

-- WARNING & LIMITATIONS:
-- - This script assumes foreign keys are single-column foreign keys. If any FK is multi-column,
--   manual intervention will be required.
-- - If your PKs were UNSIGNED, the script will preserve UNSIGNED.
-- - Large tables: ALTER TABLE may be long-running and lock tables.

-- Step 0: Show current constraints referencing targets (user-visible)
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
  AND REFERENCED_COLUMN_NAME = 'id'
  AND REFERENCED_TABLE_NAME IN ('usuario','juego','scrim')
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- Step 1: Build ADD CONSTRAINT statements (we store them before dropping the FKs)
SET @add_sql = (
  SELECT GROUP_CONCAT(
    CONCAT('ALTER TABLE `', TABLE_NAME, '` ADD CONSTRAINT `', CONSTRAINT_NAME,
           '` FOREIGN KEY (`', COLUMN_NAME, '`) REFERENCES `', REFERENCED_TABLE_NAME, '`(`', REFERENCED_COLUMN_NAME, '`)')
    SEPARATOR '; ')
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
    AND REFERENCED_COLUMN_NAME = 'id'
    AND REFERENCED_TABLE_NAME IN ('usuario','juego','scrim')
  GROUP BY REFERENCED_TABLE_NAME
);

SELECT @add_sql AS add_statements_preview;

-- Step 2: Build DROP statements for those FKs
SET @drop_sql = (
  SELECT GROUP_CONCAT(CONCAT('ALTER TABLE `', TABLE_NAME, '` DROP FOREIGN KEY `', CONSTRAINT_NAME, '`') SEPARATOR '; ')
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
    AND REFERENCED_COLUMN_NAME = 'id'
    AND REFERENCED_TABLE_NAME IN ('usuario','juego','scrim')
);

SELECT @drop_sql AS drop_statements_preview;

-- Execute DROP statements if any
SET @tmp = IFNULL(@drop_sql, 'SELECT "-- no foreign keys to drop --"');
PREPARE s FROM @tmp;
EXECUTE s;
DEALLOCATE PREPARE s;

-- Step 3: Modify PK columns to BIGINT, preserving UNSIGNED if present
-- We'll generate MODIFY statements per table based on COLUMN_TYPE
SELECT COLUMN_NAME, COLUMN_TYPE, TABLE_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('usuario','juego','scrim')
  AND COLUMN_KEY = 'PRI'
  AND COLUMN_NAME = 'id';

-- Construct and execute ALTER TABLE MODIFY statements for each affected table
SET @mod_sql = (
  SELECT GROUP_CONCAT(
    CONCAT('ALTER TABLE `', TABLE_NAME, '` MODIFY COLUMN `id` ',
           (CASE WHEN LOCATE('unsigned', LOWER(COLUMN_TYPE))>0 THEN 'BIGINT UNSIGNED' ELSE 'BIGINT' END),
           ' NOT NULL AUTO_INCREMENT') SEPARATOR '; ')
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('usuario','juego','scrim')
    AND COLUMN_KEY = 'PRI'
    AND COLUMN_NAME = 'id'
);

SELECT @mod_sql AS modify_statements_preview;

SET @tmp2 = IFNULL(@mod_sql, 'SELECT "-- no PK columns to modify --"');
PREPARE t FROM @tmp2;
EXECUTE t;
DEALLOCATE PREPARE t;

-- Step 4: Recreate the captured foreign keys
SET @tmp3 = IFNULL(@add_sql, 'SELECT "-- no foreign keys to add --"');
PREPARE u FROM @tmp3;
EXECUTE u;
DEALLOCATE PREPARE u;

-- Step 5: Verify resulting key usage and column types
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
  AND REFERENCED_COLUMN_NAME = 'id'
  AND REFERENCED_TABLE_NAME IN ('usuario','juego','scrim')
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('usuario','juego','scrim','matches','miembros_equipo','reporte_conducta','waitlist_entry','usuario_roles_preferidos')
  AND COLUMN_NAME IN ('id','usuario_id','scrim_id','juego_id')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- End of script
