package com.uade.TrabajoPracticoProcesoDesarrollo.config;

import com.uade.TrabajoPracticoProcesoDesarrollo.service.UserSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Seeder opcional para crear usuarios masivos con emails "reales".
 * Activación: definir en application-console.properties o variables de entorno:
 *  - seed.users.emails=correo1@dom.com,correo2@dom.com
 *  - seed.users.generate.count=50 y seed.users.generate.domain=midominio.com
 *  - seed.users.defaultPassword=123 (opcional)
 *  - seed.users.region=LATAM (opcional)
 */
@Component
@Profile("console")
public class BulkUserSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BulkUserSeeder.class);

    private final UserSeedService userSeedService;
    private final Environment env;

    public BulkUserSeeder(UserSeedService userSeedService, Environment env) {
        this.userSeedService = userSeedService;
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> emails = parseEmails(env.getProperty("seed.users.emails"));
            if (emails.isEmpty()) {
                int genCount = env.getProperty("seed.users.generate.count", Integer.class, 0);
                String genDomain = env.getProperty("seed.users.generate.domain");
                if (genCount > 0 && genDomain != null && !genDomain.isBlank()) {
                    emails = generateEmails(genCount, genDomain);
                }
            }

            if (emails.isEmpty()) return; // nada para sembrar

            String defaultPass = env.getProperty("seed.users.defaultPassword", "123");
            String fixedRegion = env.getProperty("seed.users.region");

            int created = userSeedService.seedFromEmails(emails, defaultPass, fixedRegion);
            if (created > 0) log.info("BulkUserSeeder: creados {} usuarios", created);
        } catch (Exception ex) {
            log.warn("BulkUserSeeder falló: {}", ex.getMessage());
        }
    }

    private List<String> parseEmails(String csv){
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private List<String> generateEmails(int count, String domain){
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) list.add("user+" + i + "@" + domain);
        return list;
    }
}
