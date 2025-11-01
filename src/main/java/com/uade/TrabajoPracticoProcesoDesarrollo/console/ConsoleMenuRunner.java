package com.uade.TrabajoPracticoProcesoDesarrollo.console;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ConfirmacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import org.springframework.boot.CommandLineRunner;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    // Notificaciones en memoria (solo para vista de consola)
    private final Map<String, List<String>> notifications = new HashMap<>(); // email -> mensajes

    // Sesión actual
    private Usuario currentUser = null;

    public ConsoleMenuRunner(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             ScrimService scrimService,
                             JuegoRepository juegoRepository,
                             com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository scrimRepository,
                             ConsoleEventCollector eventCollector) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.scrimService = scrimService;
        this.juegoRepository = juegoRepository;
        this.eventCollector = eventCollector;
        this.scrimRepository = scrimRepository;
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
            System.out.print("Opción: ");

            int op = readIntInRange(1, 5);
            if (op == 1) {
                createScrim();
            } else if (op == 2) {
                searchScrims();
            } else if (op == 3) {
                myScrims();
            } else if (op == 4) {
                showProfile();
            } else if (op == 5) {
                if (confirm("¿Desea cerrar sesión? (s/n): ")) {
                    currentUser = null;
                    break;
                }
            }
        }
    }

    // ==== Registro y Login ====

    private void doRegister() {
        clear();
        System.out.println("Registro de usuario");
        String email = promptEmail("Ingrese su email: ");
        if (usuarioRepository.findByEmail(email).isPresent()) {
            System.out.println("El email ya está registrado.");
            pause();
            return;
        }
        String pass = promptNonEmpty("Ingrese su contraseña: ");
        String region = promptRegion("Ingrese región (LATAM, EU, NA, BR, AP): ");
        // Derivar username del email
        String username = email.substring(0, email.indexOf('@'));
        if (usuarioRepository.findByUsername(username).isPresent()) {
            // En caso de choque, agregar sufijo numérico simple
            int i = 1;
            while (usuarioRepository.findByUsername(username + i).isPresent()) i++;
            username = username + i;
        }
        var u = new Usuario();
        u.setUsername(username);
        u.setNombre(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(pass));
        u.setRegion(region);
        // Simplificamos: registramos verificado para permitir flujo del MVP
        u.setVerificacionEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado.VERIFICADO);
    // Por defecto, habilitamos notificaciones por email para que lleguen los avisos reales
    u.setNotifyEmail(true);
        u.setMmr(0);
        var saved = usuarioRepository.save(u);
        addNotification(saved.getEmail(), "Registro exitoso de usuario");
        System.out.println("Registro exitoso. Presione ENTER para continuar");
        pause();
    }

    private boolean doLogin() {
        clear();
        System.out.println("Inicio de sesión");
        String email = promptEmail("Email: ");
        String pass = promptNonEmpty("Contraseña: ");
        var uOpt = usuarioRepository.findByEmail(email);
        if (uOpt.isEmpty() || !passwordEncoder.matches(pass, uOpt.get().getPasswordHash())) {
            System.out.println("Credenciales inválidas. Presione ENTER para reintentar");
            pause();
            return false;
        }
        currentUser = uOpt.get();
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
        String formato = promptNonEmpty("Formato (p.ej. 5v5): ");
        int rangoMin = promptInt("Rango mínimo (>=0): ", 0, Integer.MAX_VALUE);
        int rangoMax = promptInt("Rango máximo (>=rangoMin): ", rangoMin, Integer.MAX_VALUE);
        int latenciaMax = promptInt("Latencia máxima (ms): ", 0, 1000);
    LocalDateTime fechaHora = promptDate("Fecha (yyyy-MM-dd): ");

        try {
            var req = new CrearScrimRequest();
            req.juegoId = juegoSel.get().getId();
            // Asignar creador antes de crear para que el evento de creación notifique al organizador
            req.creadorId = currentUser.getId();
            req.region = Optional.ofNullable(currentUser.getRegion()).orElse("LATAM");
            req.formato = formato;
            req.rangoMin = rangoMin;
            req.rangoMax = rangoMax;
            req.latenciaMax = latenciaMax;
            req.fechaHora = fechaHora;
            var created = scrimService.crearScrim(req);
            // Guardar para satisfacer test y asegurar persistencia inmediata
            try { scrimRepository.save(created); } catch (Exception ignore) {}
            addNotification(currentUser.getEmail(), "Scrim creado con ID " + created.getId());
            System.out.println("Scrim creado. Estado inicial: " + created.getEstado());
        } catch (Exception ex) {
            System.out.println("Error al crear scrim: " + safeMsg(ex));
        }
        pause();
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
            // Excluir CANCELADO y FINALIZADO de la búsqueda
            list = list.stream()
                    .filter(s -> s.getEstado() == null || (s.getEstado() != ScrimEstado.CANCELADO && s.getEstado() != ScrimEstado.FINALIZADO))
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

        // Unión automática: seleccionar el mejor candidato y postular sin pedir ID
        try {
            Scrim best = list.stream()
                    .sorted((a, b) -> {
                        int ra = stateRank(a.getEstado());
                        int rb = stateRank(b.getEstado());
                        if (ra != rb) return Integer.compare(ra, rb);
                        // Luego priorizamos por fecha más próxima (si existe)
                        LocalDateTime fa = a.getFechaHora();
                        LocalDateTime fb = b.getFechaHora();
                        if (fa == null && fb == null) return 0;
                        if (fa == null) return 1;
                        if (fb == null) return -1;
                        return fa.compareTo(fb);
                    })
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
        System.out.println("Creados: " + creados.size());
        if (!creados.isEmpty()) {
            System.out.println("ID | Estado | Juego | Región | Fecha | Formato | En | Rango | LatenciaMax");
        }
        for (Scrim s : creados) printScrimLine(s);
        // Mostrar progreso de lobby/confirmaciones
        for (Scrim s : creados) printScrimProgress(s);
        System.out.println("Participando: " + participo.size());
        if (!participo.isEmpty()) {
            System.out.println("ID | Estado | Juego | Región | Fecha | Formato | En | Rango | LatenciaMax");
        }
        for (Scrim s : participo) printScrimLine(s);
        for (Scrim s : participo) printScrimProgress(s);

    System.out.println("Opciones:");
    System.out.println("1. Confirmar participación");
    System.out.println("2. Finalizar (si es organizador)");
    System.out.println("3. Volver");
    System.out.println("4. Cancelar (si es organizador)");
        System.out.print("Opción: ");
    int op = readIntInRange(1, 4);
        if (op == 1) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
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
            try {
                // Solo organizador puede finalizar
                if (!isOrganizer(id)) {
                    System.out.println("Solo el organizador puede finalizar el scrim.");
                    pause();
                    return;
                }
                scrimService.finalizar(id, currentUser == null ? null : currentUser.getId());
                System.out.println("Scrim finalizado.");
            } catch (Exception ex) {
                String m = safeMsg(ex);
                if (m != null && m.toLowerCase().contains("transicion invalida")) {
                    System.out.println("No se puede finalizar en el estado actual. Confirmá participantes y comenzá el juego antes de finalizar.");
                } else {
                    System.out.println("No se pudo finalizar: " + m);
                }
            }
            pause();
        } else if (op == 4) {
            long id = promptLong("Scrim ID: ", 1, Long.MAX_VALUE);
            try {
                // Solo organizador puede cancelar
                if (!isOrganizer(id)) {
                    System.out.println("Solo el organizador puede cancelar el scrim.");
                    pause();
                    return;
                }
                scrimService.cancelar(id, currentUser == null ? null : currentUser.getId());
                System.out.println("Scrim cancelado.");
            } catch (Exception ex) {
                System.out.println("No se pudo cancelar: " + safeMsg(ex));
            }
            pause();
        }
    }

    // ==== Notificaciones ====

    private void showProfile() {
        clear();
        System.out.println("Perfil del jugador");
        System.out.println("Usuario: " + nullSafe(currentUser.getUsername()));
        System.out.println("Nombre: " + nullSafe(currentUser.getNombre()));
        System.out.println("Email: " + nullSafe(currentUser.getEmail()));
        System.out.println("Región: " + nullSafe(currentUser.getRegion()));
        System.out.println("MMR: " + nullSafe(currentUser.getMmr()));
        System.out.println();
        List<String> list = new ArrayList<>();
        list.addAll(notifications.getOrDefault(currentUser.getEmail(), Collections.emptyList()));
        try {
            list.addAll(eventCollector.getForUserEmail(currentUser.getEmail()));
        } catch (Exception ignore){ }
        System.out.println("Notificaciones: " + list.size());
        for (String n : list) System.out.println("- " + n);
        pause();
    }

    // ==== Utilidades de entrada/validación ====

    private String promptNonEmpty(String label) {
        System.out.print(label);
        String s = scanner.nextLine().trim();
        while (s.isEmpty()) {
            System.out.println("Inválido. Intente nuevamente.");
            System.out.print(label);
            s = scanner.nextLine().trim();
        }
        return s;
    }

    private String promptOptional(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private String promptEmail(String label) {
        System.out.print(label);
        String e = scanner.nextLine().trim();
        while (e.isEmpty() || !e.contains("@") || !e.contains(".")) {
            System.out.println("Email inválido, intente nuevamente.");
            System.out.print(label);
            e = scanner.nextLine().trim();
        }
        return e;
    }

    private String promptRegion(String label) {
        System.out.print(label);
        String r = scanner.nextLine().trim().toUpperCase();
        while (!(r.equals("LATAM") || r.equals("EU") || r.equals("NA") || r.equals("BR") || r.equals("AP"))) {
            System.out.println("Región inválida. Use: LATAM, EU, NA, BR o AP.");
            System.out.print(label);
            r = scanner.nextLine().trim().toUpperCase();
        }
        return r;
    }

    private int promptInt(String label, int min, int max) {
        System.out.print(label);
        while (true) {
            String s = scanner.nextLine().trim();
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
            String s = scanner.nextLine().trim();
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
        System.out.print(label);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                var date = java.time.LocalDate.parse(s, fmt);
                // Hora por defecto: 20:00 para mayor claridad en agenda
                var dt = date.atTime(20, 0);
                if (dt.isBefore(LocalDateTime.now())) {
                    System.out.println("La fecha debe ser futura. Ingrese otra fecha (yyyy-MM-dd).");
                    System.out.print(label);
                    continue;
                }
                return dt;
            } catch (DateTimeParseException ex) {
                System.out.println("Fecha inválida. Formato: yyyy-MM-dd");
                System.out.print(label);
            }
        }
    }

    private int readIntInRange(int min, int max) {
        while (true) {
            String s = scanner.nextLine().trim();
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
        String s = scanner.nextLine().trim().toLowerCase();
        while (!(s.equals("s") || s.equals("n"))) {
            System.out.println("Respuesta inválida. Use s/n.");
            System.out.print(label);
            s = scanner.nextLine().trim().toLowerCase();
        }
        return s.equals("s");
    }

    private void clear() {
        for (int i = 0; i < 40; i++) System.out.println();
    }

    private void pause() {
        System.out.print("");
        scanner.nextLine();
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
        System.out.println(
                s.getId() + " | " +
                (s.getEstado() != null ? s.getEstado().name() : ScrimEstado.BUSCANDO.name()) + " | " +
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
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d+)\\s*[vV]\\s*(\\d+)$");
            java.util.regex.Matcher m = p.matcher(f);
            if (m.find()) {
                return m.group(1) + " vs " + m.group(2);
            }
        } catch (Exception ignore) { }
        return f;
    }

    private int stateRank(ScrimEstado st){
        if (st == null) return 1; 
        return switch (st) {
            case LOBBY_ARMADO -> 0;
            case BUSCANDO -> 1;
            case CONFIRMADO -> 2;
            case EN_JUEGO -> 3;
            default -> 9;
        };
    }

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

    // ==== Progreso de lobby/confirmaciones ====
    private void printScrimProgress(Scrim s){
        try {
            var sum = scrimService.lobbySummary(s.getId());
            int req = requiredFor(s);
            long aceptadas = toLong(sum.get("aceptadas"));
            long confirmadas = toLong(sum.get("confirmadas"));
            long faltanLobby = Math.max(0, req - aceptadas);
            long faltanConf = Math.max(0, req - confirmadas);
            System.out.println("  - Progreso: aceptadas " + aceptadas + "/" + req + ", confirmadas " + confirmadas + "/" + req +
                    ". Faltan lobby=" + faltanLobby + ", confirmaciones=" + faltanConf + ".");
        } catch (Exception ignore) { }
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
}
