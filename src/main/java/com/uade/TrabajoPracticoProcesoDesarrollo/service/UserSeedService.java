package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserSeedService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public UserSeedService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Environment env) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    public int seedFromEmails(List<String> emails, String defaultPass, String fixedRegion){
        if (emails == null || emails.isEmpty()) return 0;
        String pass = (defaultPass == null || defaultPass.isBlank()) ? "123" : defaultPass;
        int created = 0;
        for (String email : emails) {
            if (email == null || email.isBlank() || !email.contains("@")) continue;
            if (usuarioRepository.findByEmail(email).isPresent()) continue;

            String baseUser = email.substring(0, email.indexOf('@'));
            String username = uniqueUsername(baseUser);
            String nombre = uniqueNombre(baseUser);
            String region = resolveRegion(fixedRegion, created);

            Usuario u = new Usuario();
            u.setUsername(username);
            u.setNombre(nombre);
            u.setEmail(email);
            u.setPasswordHash(passwordEncoder.encode(pass));
            u.setRegion(region);
            u.setVerificacionEstado(VerificacionEstado.VERIFICADO);
            u.setNotifyEmail(true);
            u.setMmr(randomMMR());
            u.setLatencia(randomLatencyForRegion(region));
            usuarioRepository.save(u);
            created++;
        }
        return created;
    }

    public int seedGenerated(int count, String domain, String defaultPass, String fixedRegion){
        if (count <= 0 || domain == null || domain.isBlank()) return 0;
        List<String> emails = new ArrayList<>();
        for (int i = 1; i <= count; i++) emails.add("user+" + i + "@" + domain);
        return seedFromEmails(emails, defaultPass, fixedRegion);
    }

    private String uniqueUsername(String base){
        String username = base;
        int i = 1;
        while (usuarioRepository.findByUsername(username).isPresent()) {
            username = base + i++;
        }
        return username;
    }

    private String uniqueNombre(String base){
        String nombre = base;
        int i = 1;
        while (true) {
            boolean taken = false;
            for (Usuario u : usuarioRepository.findAll()) {
                if (nombre.equals(u.getNombre())) { taken = true; break; }
            }
            if (!taken) return nombre;
            nombre = base + i++;
        }
    }

    private final List<String> rrRegions = Arrays.asList("LATAM", "BR", "NA", "EU", "AP");
    private String resolveRegion(String fixedRegion, int idx){
        if (fixedRegion != null && !fixedRegion.isBlank()) return fixedRegion.trim().toUpperCase();
        return rrRegions.get(idx % rrRegions.size());
    }

    private int randomMMR(){
        return 100 + new Random().nextInt(1901); // 100-2000
    }

    private Integer randomLatencyForRegion(String region){
        String r = region != null ? region.trim().toUpperCase() : "";
        Random rnd = new Random();
        if ("LATAM".equals(r)) return randBetween(rnd, getIntProp("latency.range.latam.min", 0), getIntProp("latency.range.latam.max", 60));
        if ("BR".equals(r))    return randBetween(rnd, getIntProp("latency.range.br.min", 15),  getIntProp("latency.range.br.max", 70));
        if ("NA".equals(r))    return randBetween(rnd, getIntProp("latency.range.na.min", 40),  getIntProp("latency.range.na.max", 90));
        if ("EU".equals(r))    return randBetween(rnd, getIntProp("latency.range.eu.min", 80),  getIntProp("latency.range.eu.max", 140));
        if ("AP".equals(r))    return randBetween(rnd, getIntProp("latency.range.ap.min", 120), getIntProp("latency.range.ap.max", 200));
        return randBetween(rnd, getIntProp("latency.range.default.min", 50), getIntProp("latency.range.default.max", 120));
    }

    private int randBetween(Random rnd, int minInclusive, int maxInclusive){
        if (maxInclusive <= minInclusive) return minInclusive;
        return minInclusive + rnd.nextInt((maxInclusive - minInclusive) + 1);
    }

    @SuppressWarnings("null")
    private int getIntProp(String key, int def){
        try { return env != null ? env.getProperty(key, Integer.class, def) : def; }
        catch (Exception ignore){ return def; }
    }
}
