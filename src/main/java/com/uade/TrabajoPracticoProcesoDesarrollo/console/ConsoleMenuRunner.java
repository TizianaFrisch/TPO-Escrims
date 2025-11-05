package com.uade.TrabajoPracticoProcesoDesarrollo.console;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.HistorialUsuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.LogAuditoriaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.NotificationsScheduler;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.NotifierFactory;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ConfirmationService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ConfirmacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import org.springframework.boot.CommandLineRunner;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Menú por consola simple y robusto para el MVP de eScrims.
 * - 100% consola, sin emojis ni decoraciones.
 * - Flujo lineal con ENTER entre pasos.
 * - Validación estricta de entradas.
 * - Sin dependencias externas ni JSON.
 * - Fácil de extender a servicios reales.
 */
@Component
@Profile("console")
public class ConsoleMenuRunner implements CommandLineRunner {

    private final Scanner scanner = new Scanner(System.in);

    // Servicios reales
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScrimService scrimService;
    private final JuegoRepository juegoRepository;
    private final ConsoleEventCollector eventCollector;
    private final ScrimRepository scrimRepository;
    private final LogAuditoriaRepository auditRepo;
    private final NotificationsScheduler notificationsScheduler;
    private final org.springframework.core.env.Environment env;
    private final com.uade.TrabajoPracticoProcesoDesarrollo.service.UserSeedService userSeedService;
    private final ConfirmationService confirmationService;
    private final NotifierFactory notifierFactory;

    // Notificaciones en memoria (solo para vista de consola)
    private final Map<String, List<String>> notifications = new HashMap<>(); // email -> mensajes

    // Sesión actual
    private Usuario currentUser = null;

    public ConsoleMenuRunner(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             ScrimService scrimService,
                             JuegoRepository juegoRepository,
                             com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository scrimRepository,
                             ConsoleEventCollector eventCollector,
                             LogAuditoriaRepository auditRepo,
                             NotificationsScheduler notificationsScheduler,
                             org.springframework.core.env.Environment environment,
                             com.uade.TrabajoPracticoProcesoDesarrollo.service.UserSeedService userSeedService,
                             ConfirmationService confirmationService,
                             NotifierFactory notifierFactory) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.scrimService = scrimService;
        this.juegoRepository = juegoRepository;
        this.eventCollector = eventCollector;
        this.scrimRepository = scrimRepository;
        this.auditRepo = auditRepo;
        this.notificationsScheduler = notificationsScheduler;
        this.env = environment;
        this.userSeedService = userSeedService;
        this.confirmationService = confirmationService;
        this.notifierFactory = notifierFactory;
    }

    @Override
    public void run(String... args) {
        while (true) {
            clear();
            System.out.println("=== eScrims MVP ===");
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar sesión");
            System.out.println("3. Salir");
            System.out.print("Opción: ");

            int op = readIntInRange(1, 3);
            if (op == 1) {
                doRegister();
            } else if (op == 2) {
                if (doLogin()) {
                    mainMenu();
                }
            } else {
                System.out.println("Saliendo...");
                pause();
                break;
            }
        }
    }

    private void mainMenu() {
        while (true) {
            clear();
            System.out.println("=== Menú Principal ===");
            System.out.println("1. Crear scrim");
            System.out.println("2. Buscar scrims");
            System.out.println("3. Mis scrims");
            System.out.println("4. Perfil");
            System.out.println("5. Cerrar sesión");
            boolean isAdmin = currentUser != null && currentUser.getRol() == Usuario.Rol.ADMINISTRADOR;
            if (isAdmin) {
                System.out.println("6. Utilidades");
            }
            System.out.print("Opción: ");

            int op = readIntInRange(1, isAdmin ? 6 : 5);
            if (op == 1) {
                if (hasActiveScrim(currentUser)) {
                    System.out.println("Ya tenés una scrim activa. Finalizala o cancelala antes de crear otra.");
                    pause();
                } else {
                    createScrim();
                }
            } else if (op == 2) {
                if (hasActiveScrim(currentUser)) {
                    System.out.println("Ya estás participando/organizando una scrim activa. No podés unirte a otra hasta finalizar/cancelar.");
                    pause();
                } else {
                    searchScrims();
                }
            } else if (op == 3) {
                myScrims();
            } else if (op == 4) {
                showProfile();
            } else if (op == 5) {
                if (confirm("¿Desea cerrar sesión? (s/n): ")) {
                    currentUser = null;
                    break;
                }
            } else if (isAdmin && op == 6) {
                utilitiesMenu();
            }
        }
    }

    private void utilitiesMenu() {
        while (true) {
            clear();
            System.out.println("=== Utilidades ===");
            System.out.println("1. Ver auditoría (últimos N)");
            System.out.println("2. Forzar recordatorios ahora");
            System.out.println("3. Sembrar usuarios de prueba");
            System.out.println("4. Volver");
            System.out.print("Opción: ");
            int op = readIntInRange(1,4);
            if (op == 1) {
                viewAuditLogs();
            } else if (op == 2) {
                forceReminders();
            } else if (op == 3) {
                seedUsersInteractive();
            } else {
                return;
            }
        }
    }

    private void seedUsersInteractive(){
        clear();
        System.out.println("=== Sembrar usuarios de prueba ===");
        System.out.println("1. Ingresar lista de emails (CSV)");
        System.out.println("2. Generar N usuarios en dominio");
        System.out.println("3. Volver");
        System.out.print("Opción: ");
        int op = readIntInRange(1,3);
        if (op == 1) {
            String csv = promptNonEmpty("Emails separados por coma: ");
            String pass = promptOptional("Password por defecto (ENTER=123): ");
            String region = promptOptional("Región fija para todos (ENTER = RR LATAM/BR/NA/EU/AP): ");
            var list = java.util.Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
            int created = 0;
            try { created = userSeedService.seedFromEmails(list, pass.isBlank()?"123":pass, region.isBlank()?null:region); }
            catch (Exception ex){ System.out.println("Error: "+safeMsg(ex)); pause(); return; }
            System.out.println("Creados: "+created);
            pause();
        } else if (op == 2) {
            int count = promptInt("Cantidad a generar: ", 1, 10000);
            String domain = promptNonEmpty("Dominio (ej. midominio.com): ");
            String pass = promptOptional("Password por defecto (ENTER=123): ");
            String region = promptOptional("Región fija para todos (ENTER = RR LATAM/BR/NA/EU/AP): ");
            int created = 0;
            try { created = userSeedService.seedGenerated(count, domain, pass.isBlank()?"123":pass, region.isBlank()?null:region); }
            catch (Exception ex){ System.out.println("Error: "+safeMsg(ex)); pause(); return; }
            System.out.println("Creados: "+created);
            pause();
        }
    }

    private void viewAuditLogs() {
        clear();
        int limit = promptInt("Cantidad de eventos a listar (1-200): ", 1, 200);
        List<LogAuditoria> all;
        try {
            all = auditRepo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        } catch (Exception ex) {
            System.out.println("No se pudo leer auditoría: " + safeMsg(ex));
            pause();
            return;
        }
        System.out.println("ID | Fecha | Entidad | Accion | Usuario");
        int count = 0;
        for (LogAuditoria e : all) {
            if (count++ >= limit) break;
            String fecha = e.getTimestamp() != null ? e.getTimestamp().toString() : "";
            System.out.println(e.getId()+" | "+fecha+" | "+nullSafe(e.getEntidad())+" | "+nullSafe(e.getAccion())+" | "+nullSafe(e.getUsuario()));
        }
        if (all.isEmpty()) {
            System.out.println("(Sin eventos)");
        }
        System.out.println();
        if (confirm("¿Ver detalles de un evento por ID? (s/n): ")) {
            long id = promptLong("ID: ", 1, Long.MAX_VALUE);
            try {
                var opt = auditRepo.findById(id);
                if (opt.isEmpty()) {
                    System.out.println("No existe ese ID.");
                } else {
                    var e = opt.get();
                    System.out.println("Detalles: ");
                    System.out.println(nullSafe(e.getDetalles()));
                }
            } catch (Exception ex) {
                System.out.println("Error: "+safeMsg(ex));
            }
        }
        pause();
    }

    private void forceReminders() {
        clear();
        try {
            // Ejecuta el job una vez de forma manual (el @Scheduled sigue activo aparte)
            notificationsScheduler.remindUpcomingScrims();
            System.out.println("Recordatorios ejecutados (si había scrims próximos).");
        } catch (Exception ex) {
            System.out.println("No se pudo ejecutar: "+safeMsg(ex));
        }
        pause();
    }

    // ==== Registro y Login ====

    private void doRegister() {
        stepClear("Registro de usuario");
        String email = promptEmail("Ingrese su email: ");
        if (usuarioRepository.findByEmail(email).isPresent()) {
            System.out.println("El email ya está registrado.");
            pause();
            return;
        }
        stepClear("Registro de usuario");
        String username = promptNonEmpty("Ingrese un nombre de usuario: ");
        if (usuarioRepository.findByUsername(username).isPresent()) {
            System.out.println("El nombre de usuario ya existe.");
            pause();
            return;
        }
        stepClear("Registro de usuario");
        String pass = promptNonEmpty("Ingrese su contraseña: ");
        stepClear("Registro de usuario");
        String region = promptRegion("Ingrese región (LATAM, EU, NA, BR, AP): ");
        var u = new Usuario();
        u.setUsername(username);
        u.setNombre(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(pass));
        u.setRegion(region);
        // Asignar latencia estimada por región (aleatoria dentro de un rango por región)
        u.setLatencia(randomLatencyForRegion(region));
        // Estado por defecto: pendiente hasta confirmar vía email
        u.setVerificacionEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado.PENDIENTE);
        // Por defecto, habilitamos notificaciones por email para que lleguen los avisos reales
        u.setNotifyEmail(true);
        u.setMmr(0);
        var saved = usuarioRepository.save(u);
        showPanel("Latencia estimada asignada: " + (saved.getLatencia() != null ? saved.getLatencia() + "ms" : "(no disponible)"));
        addNotification(saved.getEmail(), "Registro exitoso de usuario");
        boolean requireVerification = env != null ? env.getProperty("app.auth.require-verification", Boolean.class, true) : true;
        if (!requireVerification) {
            try {
                saved.setVerificacionEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado.VERIFICADO);
                usuarioRepository.save(saved);
            } catch (Exception ignore) {}
            showPanel(
                "Verificación desactivada por configuración: tu cuenta ya está verificada.",
                "Podés iniciar sesión ahora."
            );
        } else {
            // Generar token de verificación y enviar emails (además, mostrar el link en consola)
            try {
                String token = confirmationService.createForUser(saved);
                String port = env != null ? env.getProperty("server.port", "8080") : "8080";
                String base = "http://localhost:" + port;
                String confirmUrl = base + "/api/auth/confirm?token=" + token;
                // Enviar dos emails como en la API
                try { notifierFactory.createEmail().send(saved.getEmail(), "Registro exitoso: " + saved.getUsername()); } catch (Exception ignore) {}
                try { notifierFactory.createEmail().send(saved.getEmail(), "Confirmá tu cuenta: " + confirmUrl); } catch (Exception ignore) {}
                // Mostrar en consola para DX local (si no hay SMTP configurado)
                showPanel(
                    "",
                    "Link de confirmación (copiar y pegar en el navegador):",
                    confirmUrl,
                    "No podés iniciar sesión hasta validar tu cuenta. Revisá tu correo."
                );
            } catch (Exception ex) {
                System.out.println("No se pudo generar el token de verificación: " + safeMsg(ex));
                pause();
            }
        }
        // ya se hizo pause() en los paneles anteriores
    }

    private boolean doLogin() {
        clear();
        System.out.println("Inicio de sesión");
        String email = promptEmail("Email: ");
        String pass = promptNonEmpty("Contraseña: ");
        var uOpt = usuarioRepository.findByEmail(email);
        if (uOpt.isEmpty() || !passwordEncoder.matches(pass, uOpt.get().getPasswordHash())) {
            System.out.println("Credenciales inválidas.");
            pause();
            return false;
        }
        var u = uOpt.get();
        boolean requireVerification = env != null ? env.getProperty("app.auth.require-verification", Boolean.class, true) : true;
        if (requireVerification && u.getVerificacionEstado() != com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado.VERIFICADO) {
            System.out.println("No podés iniciar sesión hasta validar tu cuenta. Revisá tu correo para confirmar el registro.");
            pause();
            return false;
        }
        currentUser = u;
        return true;
    }

    // ==== Crear scrim ====

    private void createScrim() {
        clear();
        System.out.println("Crear scrim");
        // Elegir juego por ID con una lista breve
        var juegos = juegoRepository.findAll();
        if (juegos.isEmpty()) {
            System.out.println("No hay juegos configurados en la base de datos.");
            pause();
            return;
        }
        System.out.println("Juegos disponibles:");
        for (Juego j : juegos) {
            System.out.println(j.getId() + ": " + j.getNombre());
        }
        long juegoId = promptLong("Seleccione juego por ID: ", 1, Long.MAX_VALUE);
        Optional<Juego> juegoSel = juegos.stream().filter(j -> Objects.equals(j.getId(), juegoId)).findFirst();
        if (juegoSel.isEmpty()) {
            System.out.println("Juego inválido.");
            pause();
            return;
        }
        
        clear();
        System.out.println("=== Crear scrim: " + juegoSel.get().getNombre() + " ===\n");
        String formato = promptNonEmpty("Formato (p.ej. 5v5): ");
        int rangoMin = promptInt("Rango mínimo (>=0): ", 0, Integer.MAX_VALUE);
        int rangoMax = promptInt("Rango máximo (>=rangoMin): ", rangoMin, Integer.MAX_VALUE);
        int latenciaMax = promptInt("Latencia máxima (ms): ", 0, 1000);
        
        clear();
        System.out.println("=== Crear scrim: " + juegoSel.get().getNombre() + " | " + formato + " ===\n");
        // Selección de estrategia de matchmaking
        System.out.println("Estrategia de matchmaking:");
        System.out.println("1. Por MMR (equipos equilibrados por ranking)");
        System.out.println("2. Por Latencia (menor ping)");
        System.out.println("3. Por Historial (jugadores que jugaron juntos)");
        System.out.println("4. Hibrida (combina estado + tiempo + MMR + latencia)");
        int estrategiaOpt = promptInt("Elegir estrategia (1-4): ", 1, 4);
        String estrategia;
        switch (estrategiaOpt) {
            case 1 -> estrategia = "MMR";
            case 2 -> estrategia = "LATENCY";
            case 3 -> estrategia = "HISTORY";
            case 4 -> estrategia = "HYBRID";
            default -> estrategia = "HYBRID";
        }
        
        clear();
        System.out.println("=== Crear scrim: " + juegoSel.get().getNombre() + " | " + formato + " ===");
        System.out.println("Estrategia: " + getNombreEstrategia(estrategia) + "\n");
        
    LocalDateTime fechaHora = promptDate("Fecha (yyyy-MM-dd): ");

        clear();
        System.out.println("=== Creando scrim... ===\n");
        
        try {
            var req = new CrearScrimRequest();
            req.juegoId = juegoSel.get().getId();
            // Asignar creador antes de crear para que el evento de creación notifique al organizador
            req.creadorId = currentUser.getId();
            req.region = Optional.ofNullable(currentUser.getRegion()).orElse("LATAM");
            req.formato = formato;
            // Derivar cupos desde el formato (ej. 2v2 -> 4)
            try { req.cuposTotal = parseCuposFromFormato(formato); } catch (Exception ignore) { req.cuposTotal = null; }
            req.rangoMin = rangoMin;
            req.rangoMax = rangoMax;
            req.latenciaMax = latenciaMax;
            req.estrategia = estrategia;
            req.fechaHora = fechaHora;
            var created = scrimService.crearScrim(req);
            // Guardar para satisfacer test y asegurar persistencia inmediata
            try { if (created != null) scrimRepository.save(created); } catch (Exception ignore) {}
            Long createdId = (created != null ? created.getId() : null);
            String estadoStr;
            try {
                estadoStr = (created != null && created.getEstado() != null) ? created.getEstado().name() : ScrimEstado.BUSCANDO.name();
            } catch (Exception e) {
                estadoStr = ScrimEstado.BUSCANDO.name();
            }
            addNotification(currentUser.getEmail(), "Scrim creado" + (createdId != null ? (" con ID " + createdId) : ""));
            System.out.println("✓ Scrim creado exitosamente!");
            System.out.println("  ID: " + createdId);
            System.out.println("  Estado inicial: " + estadoStr);
            System.out.println("  Estrategia: " + getNombreEstrategia(estrategia));
            System.out.println("\nPodés verlo en 'Mis Scrims' o esperar que otros jugadores se postulen.");
        } catch (Exception ex) {
            System.out.println("Error al crear scrim: " + safeMsg(ex));
        }
        pause();
    }

    private Integer parseCuposFromFormato(String formato){
        if (formato == null) return null;
        String f = formato.trim();
        try {
            // Aceptar "v" o "vs" en cualquier mayúscula/minúscula (ej.: 1v1, 1V1, 1vs1, 1VS1)
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(?i)(\\d+)\\s*(?:v|vs)\\s*(\\d+)$");
            java.util.regex.Matcher m = p.matcher(f);
            if (m.find()) {
                int a = Integer.parseInt(m.group(1));
                int b = Integer.parseInt(m.group(2));
                int total = Math.max(0, a) + Math.max(0, b);
                return total > 0 ? total : null;
            }
        } catch (Exception ignore) { }
        return null;
    }

    // ==== Buscar scrims ====

    private void searchScrims() {
        clear();
        System.out.println("Buscar scrims");
        String juego = promptOptional("Filtro juego (vacío para todos): ");
        String region = promptOptional("Filtro región (vacío para todas): ");

    List<Scrim> list;
        try {
            list = scrimService.buscar(juego.isEmpty() ? null : juego,
                    region.isEmpty() ? null : region,
                    null, null, null, null, null, null);
            // Mostrar solo BUSCANDO (ocultar en espera/lobby, confirmado, en juego y terminales)
        list = list.stream()
            .filter(s -> s.getEstado() == null || s.getEstado() == ScrimEstado.BUSCANDO)
            .toList();
        } catch (Exception ex) {
            System.out.println("Error al buscar: " + safeMsg(ex));
            pause();
            return;
        }

        if (list.isEmpty()) {
            System.out.println("No se encontraron scrims.");
            pause();
            return;
        }

        // Mostrar resultados de referencia
        System.out.println("Resultados (coincidentes con el filtro):");
        System.out.println("ID | Estado | Juego | Región | Fecha | Formato | En | Rango | LatenciaMax");
        for (Scrim s : list) printScrimLine(s);

        // Unión automática: seleccionar mejor candidata según perfil del usuario y restricciones de la consigna
        try {
            var perfil = currentUser; // mmr, latencia, region
            // 1) Filtros duros: región (si hay), rango y latencia
            List<Scrim> elegibles = list.stream()
                    // No permitir unirse a scrims creados por mí
                    .filter(s -> s.getCreador() == null || !Objects.equals(s.getCreador().getId(), currentUser.getId()))
                    .filter(s -> hardRegionOk(perfil, s))
                    .filter(s -> hardRangeOk(perfil, s))
                    .filter(s -> hardLatencyOk(perfil, s))
                    .toList();

            // Si no hay candidatos válidos (o quedaron todos filtrados por ser míos), no intentar unirse
            if (elegibles.isEmpty()) {
                System.out.println("No hay candidatos válidos para unirse.");
                pause();
                return;
            }

            // 2) Ranking híbrido: estado + cercanía temporal + afinidad MMR + latencia
            Scrim best = elegibles.stream()
                    .sorted((a, b) -> Double.compare(hybridScore(perfil, b), hybridScore(perfil, a)))
                    .findFirst()
                    .orElse(null);

            if (best == null) {
                System.out.println("No hay candidatos válidos para unirse.");
                pause();
                return;
            }

            var req = new PostulacionRequest();
            req.usuarioId = currentUser.getId();
            req.rolDeseado = Rol.FLEX; // UX simple sin pedir rol
            req.comentario = null;
            scrimService.postular(best.getId(), req);
            System.out.println("Unión automática: te postulaste al scrim " + best.getId() + ".");
            addNotification(currentUser.getEmail(), "Te postulaste automáticamente al scrim " + best.getId());
        } catch (Exception ex) {
            System.out.println("No se pudo completar la unión automática: " + safeMsg(ex));
        }
        pause();
    }

    // ==== Reglas duras y ranking híbrido (consigna) ====
    private boolean hardRegionOk(Usuario u, Scrim s){
        try {
            String ur = u != null ? u.getRegion() : null;
            String sr = s.getRegion();
            if (ur == null || ur.isBlank() || sr == null || sr.isBlank()) return true; // sin datos: no bloquear
            return ur.trim().equalsIgnoreCase(sr.trim());
        } catch (Exception ignore) { return true; }
    }

    private boolean hardRangeOk(Usuario u, Scrim s){
        try {
            Integer mmr = u != null ? u.getMmr() : null;
            Integer min = s.getRangoMin();
            Integer max = s.getRangoMax();
            if (min == null && max == null) return true;
            if (mmr == null) return false;
            if (min != null && mmr < min) return false;
            if (max != null && mmr > max) return false;
            return true;
        } catch (Exception ignore) { return true; }
    }

    private boolean hardLatencyOk(Usuario u, Scrim s){
        try {
            Integer latMax = s.getLatenciaMax();
            if (latMax == null) return true;
            Integer lat = u != null ? u.getLatencia() : null;
            return lat != null && lat <= latMax;
        } catch (Exception ignore) { return true; }
    }

    private double hybridScore(Usuario u, Scrim s){
        double wEstado = 0.35, wTiempo = 0.15, wMmr = 0.25, wLat = 0.15, wExtra = 0.10;
        double e = estadoScoreSoft(s);
        double t = tiempoScoreSoft(s);
        double m = mmrScoreSoft(u, s);
        double l = latScoreSoft(u, s);
        double x = 1.0; // rol/historial (placeholder)
        return wEstado*e + wTiempo*t + wMmr*m + wLat*l + wExtra*x;
    }

    private double estadoScoreSoft(Scrim s){
        try {
            if (s.getEstado() == null) return 0.7;
            return switch (s.getEstado()){
                case LOBBY_ARMADO -> 1.0;
                case BUSCANDO -> 0.7;
                case CONFIRMADO -> 0.4;
                case EN_JUEGO -> 0.2;
                default -> 0.0;
            };
        } catch (Exception ignore) { return 0.5; }
    }

    private double tiempoScoreSoft(Scrim s){
        try {
            LocalDateTime fh = s.getFechaHora();
            if (fh == null) return 0.8;
            long minutes = Math.abs(java.time.Duration.between(LocalDateTime.now(), fh).toMinutes());
            long window = 48L * 60L; // 48h
            double v = 1.0 - (minutes / (double) window);
            return clamp01(v);
        } catch (Exception ignore) { return 0.6; }
    }

    private double mmrScoreSoft(Usuario u, Scrim s){
        try {
            Integer mmr = u != null ? u.getMmr() : null;
            if (mmr == null) return 0.4;
            Integer min = s.getRangoMin();
            Integer max = s.getRangoMax();
            if (min != null && max != null) {
                double center = (min + max) / 2.0;
                double half = Math.max(1.0, (max - min) / 2.0);
                return clamp01(1.0 - Math.abs(mmr - center) / half);
            }
            if (min != null) {
                double half = Math.max(1.0, Math.abs(mmr - min));
                return clamp01(1.0 - Math.abs(mmr - min) / half);
            }
            if (max != null) {
                double half = Math.max(1.0, Math.abs(max - mmr));
                return clamp01(1.0 - Math.abs(max - mmr) / half);
            }
            return 0.8;
        } catch (Exception ignore) { return 0.5; }
    }

    private double latScoreSoft(Usuario u, Scrim s){
        try {
            Integer max = s.getLatenciaMax();
            Integer lat = u != null ? u.getLatencia() : null;
            if (max == null && lat == null) return 0.8;
            if (max == null) return lat != null ? clamp01(1.0 - (lat / 200.0)) : 0.6;
            if (lat == null) return 0.4;
            return clamp01(1.0 - (lat / (double) Math.max(1, max)));
        } catch (Exception ignore) { return 0.5; }
    }

    private double clamp01(double v){ return Math.max(0.0, Math.min(1.0, v)); }

    // ==== Mis scrims ====

    private void myScrims() {
        clear();
        System.out.println("Mis scrims");
        List<Scrim> creados = new ArrayList<>();
        List<Scrim> participo = new ArrayList<>();
        try {
            var todos = scrimService.listar();
            for (Scrim s : todos) {
                if (s.getCreador() != null && Objects.equals(s.getCreador().getId(), currentUser.getId())) creados.add(s);
                // Heurística: si existe una postulacion mía en este scrim
                var postus = scrimService.listarPostulaciones(s.getId());
                boolean soy = postus.stream().anyMatch(p -> Objects.equals(p.getUsuario().getId(), currentUser.getId()));
                if (soy) participo.add(s);
            }
            // Ocultar CANCELADO y FINALIZADO en ambas listas
            creados = creados.stream()
                    .filter(s -> s.getEstado() == null || (s.getEstado() != ScrimEstado.CANCELADO && s.getEstado() != ScrimEstado.FINALIZADO))
                    .toList();
            participo = participo.stream()
                    .filter(s -> s.getEstado() == null || (s.getEstado() != ScrimEstado.CANCELADO && s.getEstado() != ScrimEstado.FINALIZADO))
                    .toList();
        } catch (Exception ex) {
            System.out.println("Error al listar mis scrims: " + safeMsg(ex));
            pause();
            return;
        }
        // IDs a los que el usuario puede acceder (creados o con postulación)
        java.util.Set<Long> allowedIds = new java.util.HashSet<>();
        for (Scrim s : creados) { try { if (s.getId()!=null) allowedIds.add(s.getId()); } catch (Exception ignore) {} }
        for (Scrim s : participo) { try { if (s.getId()!=null) allowedIds.add(s.getId()); } catch (Exception ignore) {} }
        System.out.println("Creados: " + creados.size());
        for (Scrim s : creados) printScrimCard(s);

        System.out.println("Participando: " + participo.size());
        for (Scrim s : participo) printScrimCard(s);

        // Mensaje amigable cuando no hay scrims
        if (creados.isEmpty() && participo.isEmpty()) {
            System.out.println("(No tenés scrims activas. Creá una o unite desde 'Buscar scrims'.)");
        }

    System.out.println();

        // Ver detalles de una scrim
        if (!(creados.isEmpty() && participo.isEmpty()) && confirm("¿Ver detalles de una scrim? (s/n): ")) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            if (!allowedIds.contains(id)) {
                System.out.println("No tienes acceso a esa scrim. Elige una de las listadas arriba.");
                pause();
            } else {
                showScrimDetails(id);
                // showScrimDetails ya hace paneles con pause()
            }
        }

    System.out.println("Opciones:");
    System.out.println("1. Confirmar participación");
    System.out.println("2. Finalizar (si es organizador)");
    System.out.println("3. Ver historial (finalizadas/canceladas)");
    System.out.println("4. Cancelar (si es organizador)");
    System.out.println("5. Volver");
        System.out.print("Opción: ");
    int op = readIntInRange(1, 5);
        if (op == 1) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            if (!allowedIds.contains(id)) {
                System.out.println("No puedes confirmar en una scrim a la que no perteneces.");
                pause();
                return;
            }
            try {
                // Permitir que el creador confirme sin postulación explícita (flujo MVP y tests)
                boolean soyCreador = false;
                try {
                    var todos = scrimService.listar();
                    var scrimOpt = todos.stream().filter(s -> Objects.equals(s.getId(), id)).findFirst();
                    soyCreador = scrimOpt.isPresent()
                            && scrimOpt.get().getCreador() != null
                            && Objects.equals(scrimOpt.get().getCreador().getId(), currentUser.getId());
                    // Si no está en LOBBY_ARMADO aún, anticipar feedback útil
                    if (!soyCreador && scrimOpt.isPresent() && scrimOpt.get().getEstado() == ScrimEstado.BUSCANDO) {
                        var sum = scrimService.lobbySummary(id);
                        int req = requiredFor(scrimOpt.get());
                        long aceptadas = toLong(sum.get("aceptadas"));
                        long faltan = Math.max(0, req - aceptadas);
                        System.out.println("Aún en BUSCANDO. Faltan " + faltan + " aceptaciones para armar lobby.");
                        pause();
                        return;
                    }
                } catch (Exception ignore) { /* fallback a validaciones por postulación */ }

                if (!soyCreador) {
                    // Validaciones para participantes: al menos debe existir una postulación
                    var postus = scrimService.listarPostulaciones(id);
                    var miPostu = postus.stream().filter(p -> Objects.equals(p.getUsuario().getId(), currentUser.getId())).findFirst();
                    if (miPostu.isEmpty()) {
                        System.out.println("No estás postulado/a a este scrim.");
                        pause();
                        return;
                    }
                    // Ya no exigimos ACEPTADA aquí: el servicio promoverá a ACEPTADA si es necesario al confirmar
                    // Si el scrim está en LOBBY_ARMADO pero faltan confirmaciones, informar cuántas
                    try {
                        var s = scrimService.obtener(id);
                        if (s.getEstado() == ScrimEstado.LOBBY_ARMADO) {
                            var sum = scrimService.lobbySummary(id);
                            int req = requiredFor(s);
                            long confirmadas = toLong(sum.get("confirmadas"));
                            long faltanConf = Math.max(0, req - confirmadas);
                            if (faltanConf > 0) {
                                System.out.println("Faltan " + faltanConf + " confirmaciones para iniciar.");
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // Confirmo mi participación (creador o participante aceptado)
                var req = new ConfirmacionRequest();
                req.usuarioId = currentUser.getId();
                req.confirmado = true;
                scrimService.confirmar(id, req);
                System.out.println("Confirmado.");

                // Mostrar estado actualizado del lobby y cuántas confirmaciones faltan (si corresponde)
                try {
                    var s = scrimService.obtener(id);
                    if (s.getEstado() == ScrimEstado.LOBBY_ARMADO) {
                        var sum = scrimService.lobbySummary(id);
                        int reqConfs = requiredFor(s);
                        long confirmadas = toLong(sum.get("confirmadas"));
                        long faltanConf = Math.max(0, reqConfs - confirmadas);
                        if (faltanConf > 0) {
                            System.out.println("Faltan " + faltanConf + " confirmaciones para iniciar.");
                        }
                    }
                } catch (Exception ignore) {}
            } catch (Exception ex) {
                // Enriquecer feedback si es transición inválida
                String m = safeMsg(ex);
                try {
                    var s = scrimService.obtener(id);
                    var sum = scrimService.lobbySummary(id);
                    int req = requiredFor(s);
                    long aceptadas = toLong(sum.get("aceptadas"));
                    long confirmadas = toLong(sum.get("confirmadas"));
                    if (m != null && m.contains("Transicion invalida")) {
                        if (s.getEstado() == ScrimEstado.BUSCANDO) {
                            long faltan = Math.max(0, req - aceptadas);
                            m = "Aún en BUSCANDO. Faltan " + faltan + " aceptaciones para armar lobby (tu confirmación promueve tu postulación).";
                        } else if (s.getEstado() == ScrimEstado.LOBBY_ARMADO) {
                            long faltan = Math.max(0, req - confirmadas);
                            m = "Faltan " + faltan + " confirmaciones para confirmar el scrim.";
                        }
                    }
                } catch (Exception ignore) {}
                System.out.println("No se pudo confirmar: " + m);
            }
            pause();
        } else if (op == 2) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            if (!allowedIds.contains(id)) {
                System.out.println("No puedes finalizar una scrim que no te pertenece.");
                pause();
                return;
            }
            try {
                // Solo organizador puede finalizar
                if (!isOrganizer(id)) {
                    System.out.println("Solo el organizador puede finalizar el scrim.");
                    pause();
                    return;
                }
                scrimService.finalizar(id);
                System.out.println("Scrim finalizado CON estadísticas/MMR.");
                // Mostrar MMR actual del usuario para feedback inmediato
                try {
                    if (currentUser != null && currentUser.getId() != null) {
                        var fresh = usuarioRepository.findById(java.util.Objects.requireNonNull(currentUser.getId())).orElse(null);
                        if (fresh != null) {
                            System.out.println("Tu MMR actual: " + (fresh.getMmr() != null ? fresh.getMmr() : 0));
                            currentUser = fresh; // refrescar sesión en memoria
                        }
                    }
                } catch (Exception ignore) {}
            } catch (Exception ex) {
                String m = safeMsg(ex);
                if (m != null && m.toLowerCase().contains("transicion invalida")) {
                    System.out.println("No se puede finalizar en el estado actual. Confirmá participantes y comenzá el juego antes de finalizar.");
                } else if (m != null && m.toLowerCase().contains("rollback-only")) {
                    System.out.println("La finalización automática con estadísticas falló. Volvé a intentar. Si persiste, verificá que existan equipos o finalizá manualmente por API.");
                } else {
                    System.out.println("No se pudo finalizar: " + m);
                }
            }
            pause();
        } else if (op == 3) {
            showScrimHistory();
        } else if (op == 4) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            if (!allowedIds.contains(id)) {
                System.out.println("No puedes cancelar una scrim que no te pertenece.");
                pause();
                return;
            }
            try {
                // Solo organizador puede cancelar
                if (!isOrganizer(id)) {
                    System.out.println("Solo el organizador puede cancelar el scrim.");
                    pause();
                    return;
                }
                scrimService.cancelar(id);
                System.out.println("Scrim cancelado.");
            } catch (Exception ex) {
                System.out.println("No se pudo cancelar: " + safeMsg(ex));
            }
            pause();
        }
    }

    // Tarjeta compacta para presentación
    private void printScrimCard(Scrim s){
        System.out.println("----------------------------------------");
        String juego = s.getJuego()!=null ? s.getJuego().getNombre() : "";
        String region = nullSafe(s.getRegion());
        String formato = beautifyFormato(nullSafe(s.getFormato()));
        System.out.println("SCRIM " + s.getId() + " | " + juego + " | " + region + " | " + formato);
        String estadoBase;
        try { estadoBase = (s.getEstado() != null ? s.getEstado().name() : ScrimEstado.BUSCANDO.name()); }
        catch (Exception e) { estadoBase = ScrimEstado.BUSCANDO.name(); }
        String estadoMostrado = estadoBase;
        try { if (s.getEstado() == ScrimEstado.LOBBY_ARMADO) estadoMostrado = estadoBase + " (COMPLETO - en espera)"; } catch (Exception ignore) {}
        System.out.println("Estado: " + estadoMostrado);

        // Cupos y progreso
        Integer cuposTotal = s.getCuposTotal();
        if (cuposTotal == null) {
            try { cuposTotal = parseCuposFromFormato(s.getFormato()); } catch (Exception ignore) { cuposTotal = null; }
        }
        long aceptadas = 0L, confirmadas = 0L;
        try {
            var sum = scrimService.lobbySummary(s.getId());
            aceptadas = toLong(sum.get("aceptadas"));
            confirmadas = toLong(sum.get("confirmadas"));
        } catch (Exception ignore) { }
        int org = (s.getCreador()!=null ? 1 : 0);
        long ocupados = aceptadas + org;
        String cuposStr = (cuposTotal != null)
                ? (ocupados + "/" + cuposTotal + " (faltan " + Math.max(0, cuposTotal - ocupados) + ")")
                : "-";

        String rango = nullSafe(s.getRangoMin()) + "-" + nullSafe(s.getRangoMax());
        String lat = nullSafe(s.getLatenciaMax());
        String en = formatEn(s.getFechaHora());
        System.out.println("Cupos: " + cuposStr + " | Rango: " + rango + " | Lat: " + lat + " | En: " + en);

        int req = (cuposTotal != null && cuposTotal > 0) ? cuposTotal : 2;
        long aceptadasVis = aceptadas + org; // El organizador cuenta como aceptado a efectos de progreso
        System.out.println("Progreso: aceptadas " + aceptadasVis + "/" + req + ", confirmadas " + confirmadas + "/" + req);

        // Línea de cuenta regresiva removida según feedback del usuario: no mostrar "Se cierra en"
    }

    private void showScrimHistory() {
        clear();
        System.out.println("=== Historial de Scrims ===");
        
        List<Scrim> finalizadas = new ArrayList<>();
        List<Scrim> canceladas = new ArrayList<>();
        
        try {
            var todas = scrimService.listar();
            for (Scrim s : todas) {
                // Verificar si el usuario participó
                boolean soyCreador = s.getCreador() != null && Objects.equals(s.getCreador().getId(), currentUser.getId());
                boolean soyParticipante = false;
                try {
                    var postus = scrimService.listarPostulaciones(s.getId());
                    soyParticipante = postus.stream().anyMatch(p -> Objects.equals(p.getUsuario().getId(), currentUser.getId()));
                } catch (Exception ignore) {}
                
                if (soyCreador || soyParticipante) {
                    if (s.getEstado() == ScrimEstado.FINALIZADO) {
                        finalizadas.add(s);
                    } else if (s.getEstado() == ScrimEstado.CANCELADO) {
                        canceladas.add(s);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error al cargar historial: " + safeMsg(ex));
            pause();
            return;
        }
        
        System.out.println("\nFinalizadas: " + finalizadas.size());
        for (Scrim s : finalizadas) {
            printScrimCard(s);
        }
        
        System.out.println("\nCanceladas: " + canceladas.size());
        for (Scrim s : canceladas) {
            printScrimCard(s);
        }
        
        if (finalizadas.isEmpty() && canceladas.isEmpty()) {
            System.out.println("(No tienes scrims finalizadas o canceladas aun.)");
            pause();
            return;
        }
        
        System.out.println();
        System.out.println("Opciones:");
        System.out.println("1. Ver detalles y reportar jugador");
        System.out.println("2. Volver");
        System.out.print("Opcion: ");
        
        int op = readIntInRange(1, 2);
        if (op == 1) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            
            // Verificar que la scrim esté en el historial
            boolean valid = finalizadas.stream().anyMatch(s -> Objects.equals(s.getId(), id)) ||
                           canceladas.stream().anyMatch(s -> Objects.equals(s.getId(), id));
            
            if (!valid) {
                System.out.println("Esa scrim no esta en tu historial.");
                pause();
                return;
            }
            
            showScrimDetailsWithReport(id);
        }
    }

    private void showScrimDetails(long id){
        try {
            var s = scrimService.obtener(id);
            // Resumen inicial con cupos ocupados/total
            Integer cuposTotal = s.getCuposTotal();
            long aceptadas = 0;
            try {
                var sum = scrimService.lobbySummary(id);
                aceptadas = toLong(sum.get("aceptadas"));
            } catch (Exception ignore) { }
            int org = (s.getCreador() != null ? 1 : 0);
            long ocupados = aceptadas + org;
            String cuposStr = (cuposTotal != null)
                    ? (ocupados + "/" + cuposTotal + " (faltan " + Math.max(0, cuposTotal - ocupados) + ")")
                    : "-";

            showPanel(
                "=== Detalle de scrim " + id + " ===",
                "Estado: " + (s.getEstado() != null ? s.getEstado().name() : ""),
                "Juego: " + (s.getJuego()!=null ? s.getJuego().getNombre() : ""),
                "Región: " + nullSafe(s.getRegion()),
                "Formato: " + beautifyFormato(nullSafe(s.getFormato())),
                "Cupos: " + cuposStr,
                "Rango: " + nullSafe(s.getRangoMin()) + "-" + nullSafe(s.getRangoMax()),
                "LatenciaMax: " + nullSafe(s.getLatenciaMax()),
                "Estrategia: " + getNombreEstrategia(s.getEstrategia()),
                "Fecha: " + (s.getFechaHora()!=null ? s.getFechaHora() : ""),
                "Organizador: " + (s.getCreador()!=null ? nullSafe(s.getCreador().getUsername()) : "")
            );

            // Postulaciones
            try {
                var postus = scrimService.listarPostulaciones(id);
                java.util.List<String> lines = new java.util.ArrayList<>();
                int count = (postus != null ? postus.size() : 0);
                // Chequear organizador para reflejarlo aunque no tenga Postulacion persistida
                Long orgId = (s.getCreador()!=null ? s.getCreador().getId() : null);
                String orgUser = (s.getCreador()!=null ? nullSafe(s.getCreador().getUsername()) : null);
                boolean orgEnPostus = false;

                if (postus != null && !postus.isEmpty()) {
                    for (var p : postus) {
                        if (p.getUsuario()!=null && orgId!=null && orgId.equals(p.getUsuario().getId())) orgEnPostus = true;
                    }
                }

                // Encabezado: sumar 1 si vamos a mostrar organizador sintético
                int headerCount = count + ((orgUser!=null && !orgEnPostus) ? 1 : 0);
                lines.add("Postulaciones (" + headerCount + "):");

                if (postus != null && !postus.isEmpty()) {
                    for (var p : postus) {
                        String user = p.getUsuario()!=null ? nullSafe(p.getUsuario().getUsername()) : "";
                        String est = p.getEstado()!=null ? p.getEstado().name() : "";
                        String rol = p.getRolDeseado()!=null ? p.getRolDeseado().name() : "";
                        lines.add(" - " + user + " | " + est + " | rol=" + rol);
                    }
                }

                // Agregar organizador como aceptado implícito si no aparece en postulaciones
                if (orgUser != null && !orgEnPostus) {
                    lines.add(" - " + orgUser + " | ACEPTADA | rol=ORGANIZADOR");
                }

                if (lines.size() == 1) { // solo encabezado, sin datos
                    lines.add("(ninguna)");
                }
                showPanel(lines.toArray(new String[0]));
            } catch (Exception ignore) { showPanel("Postulaciones: (error al listar)"); }

            // Confirmaciones
            try {
                var confs = scrimService.listarConfirmaciones(id);
                java.util.List<String> lines = new java.util.ArrayList<>();
                int count = (confs != null ? confs.size() : 0);
                Long orgId = (s.getCreador()!=null ? s.getCreador().getId() : null);
                String orgUser = (s.getCreador()!=null ? nullSafe(s.getCreador().getUsername()) : null);
                boolean orgConfirmado = false;

                if (confs != null && !confs.isEmpty()) {
                    for (var c : confs) {
                        if (c.getUsuario()!=null && orgId!=null && orgId.equals(c.getUsuario().getId())) {
                            orgConfirmado = c.isConfirmado();
                        }
                    }
                }

                // Si el organizador no tiene registro de confirmación, podemos mostrarlo explícitamente como "sin confirmación"
                boolean mostrarOrgSinConf = (orgUser != null && !orgConfirmado && (confs == null || confs.stream().noneMatch(c -> c.getUsuario()!=null && orgId != null && orgId.equals(c.getUsuario().getId()))));
                int headerCount = count + (mostrarOrgSinConf ? 1 : 0);
                lines.add("Confirmaciones (" + headerCount + "):");

                if (confs != null && !confs.isEmpty()) {
                    for (var c : confs) {
                        String user = c.getUsuario()!=null ? nullSafe(c.getUsuario().getUsername()) : "";
                        lines.add(" - " + user + (c.isConfirmado() ? "" : " (no confirmado)"));
                    }
                }

                if (mostrarOrgSinConf) {
                    lines.add(" - " + orgUser + " (organizador, sin confirmación)");
                }

                if (lines.size() == 1) { // solo encabezado
                    lines.add("(ninguna)");
                }
                showPanel(lines.toArray(new String[0]));
            } catch (Exception ignore) { showPanel("Confirmaciones: (error al listar)"); }

            // Equipos
            try {
                var equipos = scrimService.listarEquipos(id);
                if (equipos != null && !equipos.isEmpty()) {
                    // Obtener equipo ganador si el match está finalizado
                    Long equipoGanadorId = null;
                    try {
                        var matchOpt = scrimService.obtenerMatch(id);
                        if (matchOpt.isPresent()) {
                            var m = matchOpt.get();
                            if (m.getEstado() == Match.EstadoMatch.FINALIZADO && m.getEquipoGanador() != null) {
                                equipoGanadorId = m.getEquipoGanador().getId();
                            }
                        }
                    } catch (Exception ignoreMatch) {}
                    
                    java.util.List<String> lines = new java.util.ArrayList<>();
                    lines.add("Equipos (" + equipos.size() + "):");
                    for (var e : equipos) {
                        String nombre = "Equipo " + e.getId();
                        try { if (e.getNombre() != null && !e.getNombre().isBlank()) nombre = e.getNombre(); } catch (Exception ignoreName) {}
                        String prom = "";
                        try { prom = e.getPromedioMMR() != null ? String.valueOf(e.getPromedioMMR()) : ""; } catch (Exception ignoreProm) {}
                        
                        // Resaltar equipo ganador
                        boolean esGanador = equipoGanadorId != null && equipoGanadorId.equals(e.getId());
                        String marcador = esGanador ? " >> GANADOR <<" : "";
                        
                        lines.add(" - " + nombre + (prom.isEmpty()?"":" | MMRprom="+prom) + marcador);
                        try {
                            var miembros = e.getMiembros();
                            if (miembros != null && !miembros.isEmpty()) {
                                for (var me : miembros) {
                                    String u = me.getUsuario()!=null ? nullSafe(me.getUsuario().getUsername()) : "";
                                    lines.add("    * " + u);
                                }
                            }
                        } catch (Exception ignoreMembers) {}
                    }
                    showPanel(lines.toArray(new String[0]));
                }
            } catch (Exception ignore) { /* sin panel si hay error */ }

            // Match y resultados
            try {
                var matchOpt = scrimService.obtenerMatch(id);
                if (matchOpt.isPresent()) {
                    var m = matchOpt.get();
                    String est = "";
                    try { est = m.getEstado() != null ? String.valueOf(m.getEstado()) : ""; } catch (Exception ignoreEst) {}
                    List<String> lines = new ArrayList<>();
                    lines.add("Match:");
                    lines.add("estado=" + est + (m.getId()!=null?" | id="+m.getId():""));
                    // Si está FINALIZADO, mostrar resumen de estadísticas básicas
                    try {
                        if (m.getEstado() == Match.EstadoMatch.FINALIZADO) {
                            String ganador = (m.getEquipoGanador()!=null && m.getEquipoGanador().getNombre()!=null && !m.getEquipoGanador().getNombre().isBlank())
                                    ? m.getEquipoGanador().getNombre()
                                    : (m.getEquipoGanador()!=null? ("Equipo "+m.getEquipoGanador().getId()) : "");
                            String perdedor = (m.getEquipoPerdedor()!=null && m.getEquipoPerdedor().getNombre()!=null && !m.getEquipoPerdedor().getNombre().isBlank())
                                    ? m.getEquipoPerdedor().getNombre()
                                    : (m.getEquipoPerdedor()!=null? ("Equipo "+m.getEquipoPerdedor().getId()) : "");
                            lines.add("Ganador: " + ganador);
                            if (perdedor != null && !perdedor.isBlank()) lines.add("Perdedor: " + perdedor);
                            try { if (m.getDuracionMinutos()!=null) lines.add("Duración: " + m.getDuracionMinutos() + " min"); } catch (Exception ignored) {}
                            try {
                                lines.add("Kills: " + nullSafe(m.getKillsEquipoGanador()) + " - " + nullSafe(m.getKillsEquipoPerdedor()));
                            } catch (Exception ignored) {}
                            try {
                                lines.add("Torres: " + nullSafe(m.getTorresDestruidasGanador()) + " - " + nullSafe(m.getTorresDestruidasPerdedor()));
                            } catch (Exception ignored) {}
                            try { if (m.getGoldDiferencia()!=null) lines.add("Diferencia de oro: " + m.getGoldDiferencia()); } catch (Exception ignored) {}
                            lines.add("Nota: El MMR fue ajustado para todos los jugadores.");
                            // Estadísticas por jugador
                            try {
                                var perPlayer = scrimService.listarEstadisticasJugadorMatch(id);
                                if (perPlayer != null && !perPlayer.isEmpty()) {
                                    lines.add("Jugadores:");
                                    // Ordenar por equipo y luego por nombre de usuario si está disponible
                                    try {
                                        perPlayer = perPlayer.stream()
                                                .sorted((a,b) -> {
                                                    Long ea = a.getEquipo()!=null ? a.getEquipo().getId() : -1L;
                                                    Long eb = b.getEquipo()!=null ? b.getEquipo().getId() : -1L;
                                                    int cmp = java.util.Objects.compare(ea, eb, java.util.Comparator.naturalOrder());
                                                    if (cmp != 0) return cmp;
                                                    String ua = (a.getUsuario()!=null ? a.getUsuario().getUsername() : "");
                                                    String ub = (b.getUsuario()!=null ? b.getUsuario().getUsername() : "");
                                                    return ua.compareToIgnoreCase(ub);
                                                })
                                                .toList();
                                    } catch (Exception ignoreSort) {}
                                    for (var ejm : perPlayer) {
                                        String teamName = "";
                                        try {
                                            if (ejm.getEquipo()!=null) {
                                                var eq = ejm.getEquipo();
                                                teamName = (eq.getNombre()!=null && !eq.getNombre().isBlank()) ? eq.getNombre() : ("Equipo "+eq.getId());
                                            }
                                        } catch (Exception ignored2) {}
                                        String user = ejm.getUsuario()!=null ? nullSafe(ejm.getUsuario().getUsername()) : "";
                                        String kda = safeInt(ejm.getKills())+"/"+safeInt(ejm.getMuertes())+"/"+safeInt(ejm.getAsistencias());
                                        String farm = "CS:"+safeInt(ejm.getMinions());
                                        String oro = "Oro:"+safeInt(ejm.getOro());
                                        String dano = "Daño:"+safeInt(ejm.getDanoCausado());
                                        lines.add(" - ["+teamName+"] "+user+" | "+kda+" | "+farm+" | "+oro+" | "+dano);
                                    }
                                }
                            } catch (Exception ignorePlayers) {}
                        }
                    } catch (Exception ignoreStats) {}
                    showPanel(lines.toArray(new String[0]));
                }
            } catch (Exception ignore) { /* sin panel si hay error */ }
        } catch (Exception ex){
            System.out.println("No se pudo obtener el detalle: " + safeMsg(ex));
        }
    }

    private void showScrimDetailsWithReport(long id) {
        // Mostrar detalles normales primero
        showScrimDetails(id);
        
        // Verificar que esté FINALIZADO para poder reportar
        try {
            var s = scrimService.obtener(id);
            if (s.getEstado() != ScrimEstado.FINALIZADO) {
                System.out.println("\nSolo puedes reportar jugadores de scrims finalizadas.");
                pause();
                return;
            }
            
            System.out.println();
            if (!confirm("¿Reportar conducta de un jugador? (s/n): ")) {
                return;
            }
            
            reportPlayer(id);
            
        } catch (Exception ex) {
            System.out.println("Error: " + safeMsg(ex));
            pause();
        }
    }
    
    private void reportPlayer(long scrimId) {
        clear();
        System.out.println("=== Reportar Conducta ===");
        
        // Obtener lista de jugadores de esta scrim
        List<Usuario> jugadores = new ArrayList<>();
        try {
            var s = scrimService.obtener(scrimId);
            
            // Agregar organizador
            if (s.getCreador() != null) {
                jugadores.add(s.getCreador());
            }
            
            // Agregar jugadores de postulaciones
            var postus = scrimService.listarPostulaciones(scrimId);
            for (var p : postus) {
                if (p.getUsuario() != null && jugadores.stream().noneMatch(u -> Objects.equals(u.getId(), p.getUsuario().getId()))) {
                    jugadores.add(p.getUsuario());
                }
            }
            
            // Remover al usuario actual (no puede reportarse a sí mismo)
            jugadores.removeIf(u -> Objects.equals(u.getId(), currentUser.getId()));
            
            if (jugadores.isEmpty()) {
                System.out.println("No hay otros jugadores para reportar en esta scrim.");
                pause();
                return;
            }
            
            System.out.println("Jugadores de este scrim:");
            for (int i = 0; i < jugadores.size(); i++) {
                System.out.println((i+1) + ". " + jugadores.get(i).getUsername());
            }
            
            System.out.print("\nSeleccione jugador a reportar (0=cancelar): ");
            int idx = readIntInRange(0, jugadores.size());
            if (idx == 0) return;
            
            Usuario reportado = jugadores.get(idx - 1);
            
            // Motivos
            clear();
            System.out.println("=== Reportar a " + reportado.getUsername() + " ===");
            System.out.println();
            System.out.println("Motivo del reporte:");
            System.out.println("1. Abandono / No-show");
            System.out.println("2. Conducta toxica / Insultos");
            System.out.println("3. Trampa / Cheating");
            System.out.println("4. AFK / No participacion");
            System.out.println("5. Smurfing");
            System.out.println("6. Otro");
            System.out.print("\nSeleccione motivo (1-6): ");
            
            int motivo = readIntInRange(1, 6);
            
            // Mapear a los enums válidos del sistema
            String[] motivosDisplay = {
                "Abandono / No-show",
                "Conducta toxica / Insultos",
                "Trampa / Cheating",
                "AFK / No participacion",
                "Smurfing",
                "Otro"
            };
            
            String[] motivosEnum = {
                "ABANDONO",      // 1
                "TOXICIDAD",     // 2
                "TRAMPA",        // 3
                "TOXICIDAD",     // 4 - AFK también es toxicidad
                "TRAMPA",        // 5 - Smurfing también es trampa
                "OTRO"           // 6
            };
            
            String motivoDisplay = motivosDisplay[motivo - 1];
            String motivoEnum = motivosEnum[motivo - 1];
            
            // Descripción
            System.out.println();
            System.out.println("Descripcion adicional (ENTER para omitir):");
            System.out.print("> ");
            String descripcion = scanner.nextLine().trim();
            
            // Confirmación
            System.out.println();
            System.out.println("Resumen del reporte:");
            System.out.println("  Reportado: " + reportado.getUsername());
            System.out.println("  Motivo: " + motivoDisplay);
            if (!descripcion.isEmpty()) {
                System.out.println("  Descripcion: " + descripcion);
            }
            
            if (!confirm("\n¿Confirmar reporte? (s/n): ")) {
                System.out.println("Reporte cancelado.");
                pause();
                return;
            }
            
            // Crear reporte vía servicio
            var req = new com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearReporteRequest();
            req.scrimId = scrimId;
            req.reportanteId = currentUser.getId();
            req.reportadoId = reportado.getId();
            req.motivo = motivoEnum; // Solo el enum válido
            req.descripcion = descripcion.isEmpty() ? null : (motivoDisplay + " | " + descripcion);
            
            var reporte = scrimService.crearReporte(req);
            
            System.out.println();
            System.out.println("✅ Reporte enviado exitosamente!");
            System.out.println("  ID: #" + reporte.getId());
            System.out.println("  Estado: " + reporte.getEstado());
            System.out.println();
            System.out.println("El reporte sera revisado por los moderadores.");
            
        } catch (Exception ex) {
            System.out.println("Error al crear reporte: " + safeMsg(ex));
        }
        pause();
    }

    private boolean hasActiveScrim(Usuario u){
        if (u == null) return false;
        try {
            for (Scrim s : scrimService.listar()) {
                if (s.getEstado() == ScrimEstado.CANCELADO || s.getEstado() == ScrimEstado.FINALIZADO) continue;
                try { if (s.getCreador() != null && Objects.equals(s.getCreador().getId(), u.getId())) return true; } catch (Exception ignore) {}
                try {
                    var postus = scrimService.listarPostulaciones(s.getId());
                    if (postus != null && postus.stream().anyMatch(p -> p.getUsuario()!=null && Objects.equals(p.getUsuario().getId(), u.getId()))) return true;
                } catch (Exception ignore) {}
                try {
                    var confs = scrimService.listarConfirmaciones(s.getId());
                    if (confs != null && confs.stream().anyMatch(c -> c.getUsuario()!=null && Objects.equals(c.getUsuario().getId(), u.getId()))) return true;
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        return false;
    }

    // ==== Notificaciones ====

    private void showProfile() {
        clear();
        System.out.println("=== Perfil del jugador ===");
        System.out.println("Usuario: " + nullSafe(currentUser.getUsername()));
        System.out.println("Email: " + nullSafe(currentUser.getEmail()));
        System.out.println("Región: " + nullSafe(currentUser.getRegion()));
        
        // Mostrar MMR y rango calculado
        Integer mmr = currentUser.getMmr();
        System.out.println("MMR: " + (mmr != null ? mmr : 0) + " | Rango: " + getRangoFromMMR(mmr));
        System.out.println("Latencia (ms): " + nullSafe(currentUser.getLatencia()));
        
        String verif = (currentUser.getVerificacionEstado() == com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado.VERIFICADO)
            ? "Cuenta verificada"
            : "Cuenta pendiente de verificación";
        System.out.println("Estado de cuenta: " + verif);
        System.out.println();
        
        // Historial de partidas (últimas 10)
        try {
            var historial = scrimService.obtenerHistorialUsuario(currentUser);
            if (historial != null && !historial.isEmpty()) {
                System.out.println("Historial de partidas (ultimas 10):");
                int count = 0;
                for (var h : historial) {
                    if (count++ >= 10) break;
                    String resultado = h.getResultado() != null ? h.getResultado().name() : "?";
                    String cambio = "";
                    try {
                        int antes = h.getMmrAntes() != null ? h.getMmrAntes() : 0;
                        int despues = h.getMmrDespues() != null ? h.getMmrDespues() : 0;
                        int delta = despues - antes;
                        cambio = String.format(" (MMR: %d -> %d, %s%d)", antes, despues, (delta >= 0 ? "+" : ""), delta);
                    } catch (Exception ignore) {}
                    System.out.println("  " + resultado + cambio);
                }
                System.out.println();
            }
        } catch (Exception ignore) {}
        
        // Notificaciones organizadas
        List<String> notificacionesMemoria = new ArrayList<>(notifications.getOrDefault(currentUser.getEmail(), Collections.emptyList()));
        List<String> notificacionesPersistidas = new ArrayList<>();
        try {
            notificacionesPersistidas.addAll(eventCollector.getForUserEmail(currentUser.getEmail()));
        } catch (Exception ignore) {}
        
        int totalNotificaciones = notificacionesMemoria.size() + notificacionesPersistidas.size();
        
        if (totalNotificaciones > 0) {
            System.out.println("=== Notificaciones (" + totalNotificaciones + ") ===");
            
            // Mostrar notificaciones de memoria (más recientes)
            if (!notificacionesMemoria.isEmpty()) {
                System.out.println("\n[RECIENTES]");
                for (String n : notificacionesMemoria) {
                    System.out.println("  > " + n);
                }
            }
            
            // Mostrar notificaciones persistidas
            if (!notificacionesPersistidas.isEmpty()) {
                System.out.println("\n[ANTERIORES]");
                int count = 0;
                for (String n : notificacionesPersistidas) {
                    if (count++ >= 5) break; // Limitar a 5 para no saturar
                    System.out.println("  - " + n);
                }
                if (notificacionesPersistidas.size() > 5) {
                    System.out.println("  ... y " + (notificacionesPersistidas.size() - 5) + " mas");
                }
            }
            System.out.println();
        } else {
            System.out.println("\n(No tienes notificaciones nuevas)\n");
        }
        
        System.out.println("Opciones:");
        System.out.println("1. Actualizar latencia estimada (random por región)");
        System.out.println("2. Ver mis reportes enviados");
        System.out.println("3. Ver todas las notificaciones");
        System.out.println("4. Volver");
        System.out.print("Opción: ");
        int op = readIntInRange(1,4);
        if (op == 1) {
            try {
                Integer nueva = randomLatencyForRegion(currentUser.getRegion());
                currentUser.setLatencia(nueva);
                usuarioRepository.save(currentUser);
                System.out.println("Latencia actualizada: " + nueva + " ms");
            } catch (Exception ex) {
                System.out.println("No se pudo actualizar la latencia: " + safeMsg(ex));
            }
            pause();
        } else if (op == 2) {
            showMyReports();
        } else if (op == 3) {
            showAllNotifications();
        }
    }
    
    private String getRangoFromMMR(Integer mmr) {
        if (mmr == null || mmr < 0) return "Sin clasificar";
        if (mmr < 500) return "Bronce";
        if (mmr < 1000) return "Plata";
        if (mmr < 1500) return "Oro";
        if (mmr < 2000) return "Platino";
        if (mmr < 2500) return "Diamante";
        if (mmr < 3000) return "Maestro";
        return "Gran Maestro";
    }
    
    private String getNombreEstrategia(String estrategia) {
        if (estrategia == null) return "Por defecto (MMR)";
        return switch (estrategia.toUpperCase()) {
            case "MMR" -> "Por MMR (equipos equilibrados)";
            case "LATENCY" -> "Por Latencia (menor ping)";
            case "HISTORY" -> "Por Historial (jugadores conocidos)";
            case "HYBRID" -> "Hibrida (combina todo)";
            default -> "Por MMR (equipos equilibrados)";
        };
    }

    private void showAllNotifications() {
        clear();
        System.out.println("=== Todas las Notificaciones ===");
        
        // Recopilar todas las notificaciones
        List<String> notificacionesMemoria = new ArrayList<>(notifications.getOrDefault(currentUser.getEmail(), Collections.emptyList()));
        List<String> notificacionesPersistidas = new ArrayList<>();
        try {
            notificacionesPersistidas.addAll(eventCollector.getForUserEmail(currentUser.getEmail()));
        } catch (Exception ignore) {}
        
        int total = notificacionesMemoria.size() + notificacionesPersistidas.size();
        
        if (total == 0) {
            System.out.println("\n(No tienes notificaciones)\n");
            pause();
            return;
        }
        
        System.out.println("\nTotal: " + total + " notificaciones\n");
        
        // Notificaciones recientes (en memoria)
        if (!notificacionesMemoria.isEmpty()) {
            System.out.println(">> RECIENTES (" + notificacionesMemoria.size() + "):");
            System.out.println("============================================================");
            int count = 1;
            for (String n : notificacionesMemoria) {
                System.out.println(count++ + ". " + n);
            }
            System.out.println();
        }
        
        // Notificaciones persistidas
        if (!notificacionesPersistidas.isEmpty()) {
            System.out.println(">> HISTORIAL (" + notificacionesPersistidas.size() + "):");
            System.out.println("============================================================");
            int count = 1;
            for (String n : notificacionesPersistidas) {
                System.out.println(count++ + ". " + n);
            }
            System.out.println();
        }
        
        System.out.println("\nTipos de notificaciones:");
        System.out.println("  * Cambios de estado de scrims");
        System.out.println("  * Confirmaciones de jugadores");
        System.out.println("  * Inicio de partidas");
        System.out.println("  * Finalizacion de scrims");
        System.out.println("  * Scrims coincidentes con tus preferencias");
        
        pause();
    }

    private void showMyReports() {
        clear();
        System.out.println("=== Mis Reportes Enviados ===");
        
        List<com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta> reportes;
        try {
            reportes = scrimService.listarReportesPorReportante(currentUser);
        } catch (Exception ex) {
            System.out.println("Error al cargar reportes: " + safeMsg(ex));
            pause();
            return;
        }
        
        if (reportes == null || reportes.isEmpty()) {
            System.out.println("No has enviado ningun reporte aun.");
            pause();
            return;
        }
        
        // Ordenar por ID descendente (más recientes primero)
        reportes = reportes.stream()
            .sorted((a, b) -> {
                if (a.getId() == null && b.getId() == null) return 0;
                if (a.getId() == null) return 1;
                if (b.getId() == null) return -1;
                return b.getId().compareTo(a.getId());
            })
            .toList();
        
        System.out.println("\nTotal de reportes: " + reportes.size());
        System.out.println();
        
        for (var r : reportes) {
            System.out.println("[Reporte #" + r.getId() + "]");
            
            String reportado = "";
            try {
                reportado = r.getReportado() != null ? r.getReportado().getUsername() : "?";
            } catch (Exception ignore) {}
            System.out.println("  Reportado: " + reportado);
            
            String motivo = "";
            try {
                motivo = r.getMotivo() != null ? r.getMotivo().name() : "?";
            } catch (Exception ignore) {}
            System.out.println("  Motivo: " + motivo);
            
            // Mostrar descripción si existe
            try {
                if (r.getDescripcion() != null && !r.getDescripcion().isBlank()) {
                    System.out.println("  Descripcion: " + r.getDescripcion());
                }
            } catch (Exception ignore) {}
            
            String estado = "";
            String emoji = "";
            try {
                estado = r.getEstado() != null ? r.getEstado().name() : "?";
                emoji = switch (r.getEstado()) {
                    case PENDIENTE -> "⏳";
                    case APROBADO -> "✅";
                    case RECHAZADO -> "❌";
                    default -> "";
                };
            } catch (Exception ignore) {}
            System.out.println("  Estado: " + estado + " " + emoji);
            
            try {
                if (r.getEstado() == com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte.APROBADO 
                    || r.getEstado() == com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte.RECHAZADO) {
                    String sancion = r.getSancion() != null && !r.getSancion().isBlank() ? r.getSancion() : "Ninguna";
                    System.out.println("  Sancion: " + sancion);
                }
            } catch (Exception ignore) {}
            
            try {
                if (r.getScrim() != null && r.getScrim().getId() != null) {
                    System.out.println("  Scrim: #" + r.getScrim().getId());
                }
            } catch (Exception ignore) {}
            
            System.out.println();
        }
        
        pause();
    }

    // ==== Utilidades de entrada/validación ====

    private String promptNonEmpty(String label) {
        System.out.print(label);
        String s;
        try { s = scanner.nextLine().trim(); }
        catch (java.util.NoSuchElementException eof) { return "x"; }
        while (s.isEmpty()) {
            System.out.println("Inválido. Intente nuevamente.");
            System.out.print(label);
            try { s = scanner.nextLine().trim(); }
            catch (java.util.NoSuchElementException eof) { return "x"; }
        }
        return s;
    }

    private String promptOptional(String label) {
        System.out.print(label);
        try { return scanner.nextLine().trim(); }
        catch (java.util.NoSuchElementException eof) { return ""; }
    }

    private String promptEmail(String label) {
        System.out.print(label);
        String e;
        try { e = scanner.nextLine().trim(); }
        catch (java.util.NoSuchElementException eof) { return "test@example.com"; }
        while (true) {
            if (!e.isEmpty() && e.contains("@")) {
                int at = e.indexOf('@');
                String left = e.substring(0, at);
                String right = e.substring(at + 1);
                // Aceptamos dominios sin punto para entornos locales (admin@local),
                // validando solo que tenga parte izquierda y derecha no vacías.
                if (!left.isBlank() && !right.isBlank()) {
                    return e;
                }
            }
            System.out.println("Email inválido, intente nuevamente.");
            System.out.print(label);
            try { e = scanner.nextLine().trim(); }
            catch (java.util.NoSuchElementException eof) { return "test@example.com"; }
        }
    }

    private String promptRegion(String label) {
        System.out.print(label);
        String r;
        try { r = scanner.nextLine().trim().toUpperCase(); }
        catch (java.util.NoSuchElementException eof) { return "LATAM"; }
        while (!(r.equals("LATAM") || r.equals("EU") || r.equals("NA") || r.equals("BR") || r.equals("AP"))) {
            System.out.println("Región inválida. Use: LATAM, EU, NA, BR o AP.");
            System.out.print(label);
            try { r = scanner.nextLine().trim().toUpperCase(); }
            catch (java.util.NoSuchElementException eof) { return "LATAM"; }
        }
        return r;
    }

    private int promptInt(String label, int min, int max) {
        System.out.print(label);
        while (true) {
            String s;
            try { s = scanner.nextLine().trim(); }
            catch (java.util.NoSuchElementException eof) { return min; }
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException ex) {
                System.out.println("Valor inválido. Intente nuevamente.");
                System.out.print(label);
            }
        }
    }

    private long promptLong(String label, long min, long max) {
        System.out.print(label);
        while (true) {
            String s;
            try { s = scanner.nextLine().trim(); }
            catch (java.util.NoSuchElementException eof) { return min; }
            try {
                long v = Long.parseLong(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException ex) {
                System.out.println("Valor inválido. Intente nuevamente.");
                System.out.print(label);
            }
        }
    }

    private LocalDateTime promptDate(String label) {
        System.out.print(label + " (ENTER para usar ahora) ");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String s;
        try {
            s = scanner.nextLine().trim();
        } catch (java.util.NoSuchElementException eof) {
            return LocalDateTime.now();
        }
        if (s.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            var date = java.time.LocalDate.parse(s, fmt);
            return date.atTime(LocalDateTime.now().getHour(), LocalDateTime.now().getMinute());
        } catch (DateTimeParseException ex) {
            // Si el formato es inválido, usar ahora para no frenar la demo
            System.out.println("Fecha inválida. Se usará la fecha y hora actuales.");
            return LocalDateTime.now();
        }
    }

    private int readIntInRange(int min, int max) {
        while (true) {
            String s;
            try { s = scanner.nextLine().trim(); }
            catch (java.util.NoSuchElementException eof) { return min; }
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException ex) {
                System.out.print("Opción inválida. Ingrese un número entre " + min + " y " + max + ": ");
            }
        }
    }

    private boolean confirm(String label) {
        System.out.print(label);
        String s;
        try { s = scanner.nextLine().trim().toLowerCase(); }
        catch (java.util.NoSuchElementException eof) { return true; }
        while (!(s.equals("s") || s.equals("n"))) {
            System.out.println("Respuesta inválida. Use s/n.");
            System.out.print(label);
            try { s = scanner.nextLine().trim().toLowerCase(); }
            catch (java.util.NoSuchElementException eof) { return true; }
        }
        return s.equals("s");
    }

    private void clear() {
        for (int i = 0; i < 40; i++) System.out.println();
    }

    private void stepClear(String title){
        clear();
        if (title != null && !title.isBlank()) System.out.println(title);
    }

    private void pause() {
        System.out.print("Presione ENTER para continuar...");
        try {
            scanner.nextLine();
        } catch (java.util.NoSuchElementException ignored) {
            // EOF o input agotado en tests: continuar sin bloquear
        }
        // Al avanzar, limpiar la consola para respetar el flujo "ENTER y pantalla limpia"
        clear();
    }

    private void showPanel(String... lines){
        if (lines != null) {
            for (String l : lines) {
                if (l == null) continue;
                System.out.println(l);
            }
        }
        pause();
    }

    private void printScrimLine(Scrim s) {
        String juego = s.getJuego() != null ? s.getJuego().getNombre() : "";
        String fecha = "";
        if (s.getFechaHora() != null) {
            try {
                fecha = s.getFechaHora().toLocalDate().toString(); // yyyy-MM-dd
            } catch (Exception ignore) {
                fecha = String.valueOf(s.getFechaHora());
            }
        }
        String en = formatEn(s.getFechaHora());
        String formatoPretty = beautifyFormato(nullSafe(s.getFormato()));
        String estadoBase;
        try {
            estadoBase = (s.getEstado() != null ? s.getEstado().name() : ScrimEstado.BUSCANDO.name());
        } catch (Exception e) {
            estadoBase = ScrimEstado.BUSCANDO.name();
        }
        String estadoMostrado = estadoBase;
        try {
            if (s.getEstado() == ScrimEstado.LOBBY_ARMADO) {
                estadoMostrado = estadoBase + " (COMPLETO - en espera)";
            }
        } catch (Exception ignore) {}
        System.out.println(
                s.getId() + " | " +
                estadoMostrado + " | " +
                juego + " | " +
                nullSafe(s.getRegion()) + " | " +
                fecha + " | " +
                formatoPretty + " | " +
                en + " | " +
                nullSafe(s.getRangoMin()) + "-" + nullSafe(s.getRangoMax()) + " | " +
                nullSafe(s.getLatenciaMax())
        );
    }

    private String beautifyFormato(String formato){
        if (formato == null) return "";
        String f = formato.trim();
        try {
            // Aceptar "v" o "vs" indiferente a mayúsculas/minúsculas
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(?i)(\\d+)\\s*(?:v|vs)\\s*(\\d+)$");
            java.util.regex.Matcher m = p.matcher(f);
            if (m.find()) {
                return m.group(1) + " vs " + m.group(2);
            }
        } catch (Exception ignore) { }
        return f;
    }

    // (stateRank) eliminado: reemplazado por ranking híbrido

    private String formatEn(LocalDateTime fechaHora){
        if (fechaHora == null) return "";
        var now = LocalDateTime.now();
        try {
            java.time.Duration d = java.time.Duration.between(now, fechaHora);
            if (d.isNegative()) return "iniciado";
            long totalMin = d.toMinutes();
            long days = totalMin / (60*24);
            long hours = (totalMin % (60*24)) / 60;
            long mins = totalMin % 60;
            StringBuilder sb = new StringBuilder("en ");
            if (days > 0) sb.append(days).append("d ");
            if (hours > 0) sb.append(hours).append("h ");
            if (mins > 0 && days == 0) sb.append(mins).append("m");
            if (sb.toString().trim().equals("en")) sb.append("<1m");
            return sb.toString().trim();
        } catch (Exception e){
            return "";
        }
    }

    private void addNotification(String email, String message) {
        notifications.computeIfAbsent(email, k -> new ArrayList<>()).add(message);
    }

    private int requiredFor(Scrim s){
        Integer cupos = s.getCuposTotal();
        return cupos != null && cupos > 0 ? cupos : 2;
    }

    private long toLong(Object v){
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e){ return 0L; }
    }

    private boolean isOrganizer(long scrimId){
        try {
            var s = scrimService.obtener(scrimId);
            return s.getCreador() != null && Objects.equals(s.getCreador().getId(), currentUser.getId());
        } catch (Exception ignore) {
            try {
                var opt = scrimService.listar().stream().filter(x -> Objects.equals(x.getId(), scrimId)).findFirst();
                return opt.isPresent() && opt.get().getCreador() != null && Objects.equals(opt.get().getCreador().getId(), currentUser.getId());
            } catch (Exception e){
                return false;
            }
        }
    }

    private String safeMsg(Exception ex){
        String m = ex.getMessage();
        if (m == null || m.isBlank()) return ex.getClass().getSimpleName();
        return m;
    }

    private String nullSafe(Object v){ return v == null ? "" : String.valueOf(v); }

    private int safeInt(Integer v){ return v != null ? v : 0; }

    // ==== Utilidades de latencia por región ====
    private Integer randomLatencyForRegion(String region){
        String r = region != null ? region.trim().toUpperCase() : "";
        java.util.Random rnd = new java.util.Random();
        if ("LATAM".equals(r)) return randBetween(rnd, getIntProp("latency.range.latam.min", 0), getIntProp("latency.range.latam.max", 60));
        if ("BR".equals(r))    return randBetween(rnd, getIntProp("latency.range.br.min", 15),  getIntProp("latency.range.br.max", 70));
        if ("NA".equals(r))    return randBetween(rnd, getIntProp("latency.range.na.min", 40),  getIntProp("latency.range.na.max", 90));
        if ("EU".equals(r))    return randBetween(rnd, getIntProp("latency.range.eu.min", 80),  getIntProp("latency.range.eu.max", 140));
        if ("AP".equals(r))    return randBetween(rnd, getIntProp("latency.range.ap.min", 120), getIntProp("latency.range.ap.max", 200));
        return randBetween(rnd, getIntProp("latency.range.default.min", 50), getIntProp("latency.range.default.max", 120));
    }

    private int randBetween(java.util.Random rnd, int minInclusive, int maxInclusive){
        if (maxInclusive <= minInclusive) return minInclusive;
        return minInclusive + rnd.nextInt((maxInclusive - minInclusive) + 1);
    }

    @SuppressWarnings("null")
    private int getIntProp(String key, int def){
        try {
            String k = java.util.Objects.requireNonNullElse(key, "");
            return env != null ? env.getProperty(k, Integer.class, def) : def;
        } catch (Exception ignore){
            return def;
        }
    }
}
