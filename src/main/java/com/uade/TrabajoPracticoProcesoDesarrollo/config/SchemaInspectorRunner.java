package com.uade.TrabajoPracticoProcesoDesarrollo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("!local")  // No ejecutar en perfil local (H2)
public class SchemaInspectorRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaInspectorRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String sql = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME IN ('usuario','miembros_equipo','scrim','juego','matches','reporte_conducta','waitlist_entry','usuario_roles_preferidos') " +
                "ORDER BY TABLE_NAME, ORDINAL_POSITION;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        System.out.println("--- SCHEMA INSPECTION: columns for implicated tables ---");
        for (Map<String, Object> row : rows) {
            System.out.printf("%s | %s | %s | %s | %s\n",
                    row.get("TABLE_NAME"), row.get("COLUMN_NAME"), row.get("COLUMN_TYPE"), row.get("IS_NULLABLE"), row.get("COLUMN_KEY"));
        }
        System.out.println("--- END SCHEMA INSPECTION ---");

        // Check if audit table exists and print its CREATE TABLE if present
        Integer auditTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'log_auditoria'",
                Integer.class);
        if (auditTableCount != null && auditTableCount > 0) {
            System.out.println("log_auditoria table exists; SHOW CREATE TABLE:");
            List<Map<String, Object>> create = jdbcTemplate.queryForList("SHOW CREATE TABLE log_auditoria");
            if (!create.isEmpty()) {
                Map<String, Object> c = create.get(0);
                // Column name may be 'Create Table'
                Object createStmt = c.get("Create Table");
                if (createStmt == null) createStmt = c.values().stream().skip(1).findFirst().orElse(c.values().iterator().next());
                System.out.println(createStmt);
            }
        } else {
            System.out.println("log_auditoria table does NOT exist in the database.");
        }

        // Print SHOW CREATE TABLE for a set of implicated tables to help craft ALTER statements
        String[] implicated = new String[]{"usuario","juego","scrim","matches","miembros_equipo","reporte_conducta","waitlist_entry","usuario_roles_preferidos"};
        System.out.println("--- SHOW CREATE TABLE for implicated tables ---");
        for (String t : implicated) {
            try {
                Integer exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                        Integer.class, t);
                if (exists == null || exists == 0) {
                    System.out.println("TABLE NOT FOUND: " + t);
                    continue;
                }
                List<Map<String, Object>> createT = jdbcTemplate.queryForList("SHOW CREATE TABLE " + t);
                if (!createT.isEmpty()) {
                    Map<String, Object> row = createT.get(0);
                    Object stmt = row.get("Create Table");
                    if (stmt == null) stmt = row.values().stream().skip(1).findFirst().orElse(row.values().iterator().next());
                    System.out.println("-- " + t + " --");
                    System.out.println(stmt);
                }
            } catch (Exception ex) {
                System.out.println("Error retrieving CREATE TABLE for " + t + ": " + ex.getMessage());
            }
        }
        System.out.println("--- END SHOW CREATE TABLE ---");

        // Check which entity classes under domain/entities have corresponding tables
        System.out.println("--- ENTITY vs DB TABLE CHECK ---");
        File entitiesDir = new File("src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/domain/entities");
        if (entitiesDir.exists() && entitiesDir.isDirectory()) {
            File[] files = entitiesDir.listFiles((d, name) -> name.endsWith(".java"));
            if (files != null) {
                for (File f : files) {
                        String className = f.getName().replaceAll("\\.java$", "");
                        String expectedTable = camelToSnake(className);
                        // Try to parse @Table(name = "...") from the source file and prefer it if present
                        try {
                            String content = java.nio.file.Files.readString(f.toPath());
                            String tableName = parseTableNameFromSource(content);
                            if (tableName != null && !tableName.isBlank()) {
                                expectedTable = tableName;
                            }
                        } catch (Exception ex) {
                            // ignore and fall back to camel->snake
                        }
                        Integer cnt = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                                Integer.class, expectedTable);
                        if (cnt == null || cnt == 0) {
                            System.out.println("MISSING TABLE for entity: " + className + " -> expected table '" + expectedTable + "'");
                        } else {
                            System.out.println("OK: entity " + className + " -> table '" + expectedTable + "' exists");
                        }
                }
            }
        } else {
            System.out.println("Entities directory not found: " + entitiesDir.getAbsolutePath());
        }
        System.out.println("--- END ENTITY vs DB TABLE CHECK ---");
    }

    private String camelToSnake(String name) {
        // Convert CamelCase to snake_case (e.g., LogAuditoria -> log_auditoria)
        String s = name.replaceAll("([a-z])([A-Z]+)", "$1_$2");
        return s.toLowerCase();
    }

    private String parseTableNameFromSource(String src) {
        // Very small parser: find @Table(name = "...") or @Table("...")
        // Pattern handles optional whitespace and optional fully qualified annotation
        Pattern p = Pattern.compile("@Table\\s*\\(\\s*name\\s*=\\s*\"([^\"]+)\"\\s*\\)");
        Matcher m = p.matcher(src);
        if (m.find()) return m.group(1);
        // also support @Table(name="...") without spaces (already covered) and @Table("...")
        Pattern p2 = Pattern.compile("@Table\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
        Matcher m2 = p2.matcher(src);
        if (m2.find()) return m2.group(1);
        return null;
    }
}
