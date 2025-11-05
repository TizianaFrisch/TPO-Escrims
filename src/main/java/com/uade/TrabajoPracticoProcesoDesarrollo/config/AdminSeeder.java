package com.uade.TrabajoPracticoProcesoDesarrollo.config;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Seed de usuario administrador: si no existe un admin, crea uno con
 * email admin@local y contraseña "123". Se ejecuta en todos los perfiles.
 */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Asegurar que exista el usuario admin@local con rol ADMINISTRADOR
            Optional<Usuario> existing = usuarioRepository.findByEmail("admin@local");
            if (existing.isPresent()) {
                Usuario u = existing.get();
                boolean changed = false;
                if (u.getRol() != Usuario.Rol.ADMINISTRADOR) {
                    u.setRol(Usuario.Rol.ADMINISTRADOR);
                    changed = true;
                }
                // Forzar credenciales conocidas para entorno de consola: admin@local / 123
                try {
                    if (u.getPasswordHash() == null || u.getPasswordHash().isBlank() ||
                            !passwordEncoder.matches("123", u.getPasswordHash())) {
                        u.setPasswordHash(passwordEncoder.encode("123"));
                        changed = true;
                    }
                } catch (Exception ignore) {
                    // En caso de encoder no disponible, al menos setear el hash
                    u.setPasswordHash(passwordEncoder.encode("123"));
                    changed = true;
                }
                if (changed) {
                    usuarioRepository.save(u);
                    log.info("Usuario '{}' asegurado como ADMINISTRADOR (pass=123 si faltaba)", u.getEmail());
                }
                return;
            }

            // Si no existe, crear admin@local con pass=123
            String base = "admin";
            String username = base;
            int i = 1;
            while (usuarioRepository.findByUsername(username).isPresent()) {
                username = base + i++;
            }
            String nombre = base;
            i = 1;
            while (true) {
                boolean taken = false;
                for (Usuario u : usuarioRepository.findAll()) {
                    if (nombre.equals(u.getNombre())) { taken = true; break; }
                }
                if (!taken) break;
                nombre = base + i++;
            }

            Usuario admin = new Usuario();
            admin.setUsername(username);
            admin.setNombre(nombre);
            admin.setEmail("admin@local");
            admin.setPasswordHash(passwordEncoder.encode("123"));
            admin.setRegion("LATAM");
            admin.setVerificacionEstado(VerificacionEstado.VERIFICADO);
            admin.setNotifyEmail(true);
            admin.setRol(Usuario.Rol.ADMINISTRADOR);
            usuarioRepository.save(admin);
            log.info("Usuario admin creado: {} / {} (pass=123)", admin.getUsername(), admin.getEmail());
        } catch (Exception ex) {
            log.warn("AdminSeeder no pudo crear/promover admin: {}", ex.getMessage());
        }
    }
}
