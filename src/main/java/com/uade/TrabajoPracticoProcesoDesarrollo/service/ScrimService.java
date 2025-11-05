package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.*;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.MotivoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ConfirmacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CommandRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.EstadisticaRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FeedbackRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearReporteRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ResolverReporteRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.DomainEventBus;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimContext;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.*;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FinalizarMatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@SuppressWarnings("null")
public class ScrimService {
    private final ScrimRepository scrimRepo;
    private final JuegoRepository juegoRepo;
    private final UsuarioRepository usuarioRepo;
    private final PostulacionRepository postulacionRepo;
    private final DomainEventBus bus;
    private final ConfirmacionRepository confirmacionRepo;
    private final EstadisticaRepository estadisticaRepo;
    private final WaitlistRepository waitlistRepo;
    private final FeedbackRepository feedbackRepo;
    private final ReporteConductaRepository reporteRepo;
    private final MatchRepository matchRepo;
    private final EstadisticaJugadorMatchRepository estadisticaMatchRepo;
    private final HistorialUsuarioRepository historialRepo;
    private final EquipoRepository equipoRepo;
    private final Map<String, com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.ScrimCommand> commands;
    private final AuditLogService auditLogService;
    private final PlatformTransactionManager txManager;

    public ScrimService(ScrimRepository scrimRepo, JuegoRepository juegoRepo, UsuarioRepository usuarioRepo, PostulacionRepository postulacionRepo, DomainEventBus bus,
                        ConfirmacionRepository confirmacionRepo, EstadisticaRepository estadisticaRepo,
                        WaitlistRepository waitlistRepo, FeedbackRepository feedbackRepo, ReporteConductaRepository reporteRepo,
                        MatchRepository matchRepo, EstadisticaJugadorMatchRepository estadisticaMatchRepo,
                        HistorialUsuarioRepository historialRepo, EquipoRepository equipoRepo,
                        MatchmakingService matchmakingService,
                        PlatformTransactionManager txManager,
                        Map<String, com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.ScrimCommand> commands,
                        AuditLogService auditLogService) {
        this.scrimRepo = scrimRepo;
        this.juegoRepo = juegoRepo;
        this.usuarioRepo = usuarioRepo;
        this.postulacionRepo = postulacionRepo;
        this.bus = bus;
        this.confirmacionRepo = confirmacionRepo;
        this.estadisticaRepo = estadisticaRepo;
        this.waitlistRepo = waitlistRepo;
        this.feedbackRepo = feedbackRepo;
        this.reporteRepo = reporteRepo;
        this.matchRepo = matchRepo;
        this.estadisticaMatchRepo = estadisticaMatchRepo;
        this.historialRepo = historialRepo;
        this.equipoRepo = equipoRepo;
        this.commands = commands;
        this.auditLogService = auditLogService;
        this.txManager = txManager;
    }

    public Scrim crearScrim(CrearScrimRequest req){
        if (req.creadorId != null) {
            // Política: un usuario no puede crear una nueva scrim si ya tiene una scrim activa (creada o participada)
            if (userHasActiveScrim(req.creadorId)) {
                throw new IllegalArgumentException("Ya tenés una scrim activa. Finalizala o cancelala antes de crear otra.");
            }
        }
        Juego juego = juegoRepo.findById(req.juegoId).orElseThrow();
        Integer cupos = req.cuposTotal != null ? req.cuposTotal : parseCuposFromFormato(req.formato);
        Scrim s = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.builders.ScrimBuilder()
        .juego(juego)
        .region(req.region)
        .formato(req.formato)
        .cupos(cupos)
        .rango(req.rangoMin, req.rangoMax)
        .latencia(req.latenciaMax)
        .fechaHora(req.fechaHora)
        .duracion(req.duracionMinutos)
        .estado(ScrimEstado.BUSCANDO)
        .build();
        // Asociar creador si se envió en la request, antes de persistir y publicar eventos
        if (req.creadorId != null) {
            Usuario creador = usuarioRepo.findById(req.creadorId).orElseThrow();
            s.setCreador(creador);
        }
        // Asignar estrategia de matchmaking
        if (req.estrategia != null && !req.estrategia.isBlank()) {
            s.setEstrategia(req.estrategia);
        }
        Scrim saved = scrimRepo.save(s);
        
        // Publicar evento de creación para disparar notificaciones
        bus.publish(new com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent(saved.getId()));
        // Log de auditoría
        try {
            String usuario = null;
            if (req.creadorId != null) {
                try { usuario = usuarioRepo.findById(req.creadorId).map(Usuario::getEmail).orElse(null); } catch (Exception ignore) {}
            }
            if (usuario == null && s.getCreador() != null) {
                try { usuario = s.getCreador().getEmail(); } catch (Exception ignore) {}
            }
            if (usuario == null) usuario = "system";
            auditLogService.log("Scrim", saved.getId(), "CREAR", usuario, req);
        } catch (Exception ignore) {}
        
        return saved;
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

    private boolean isActive(Scrim s){
        if (s == null) return false;
        var e = s.getEstado();
        return e != ScrimEstado.CANCELADO && e != ScrimEstado.FINALIZADO;
    }

    private boolean userHasActiveScrim(Long userId){
        if (userId == null) return false;
        for (Scrim s : scrimRepo.findAll()) {
            if (!isActive(s)) continue;
            try {
                if (s.getCreador() != null && userId.equals(s.getCreador().getId())) return true;
            } catch (Exception ignore) {}
            try {
                var p = postulacionRepo.findByScrimIdAndUsuarioId(s.getId(), userId);
                if (p != null) return true;
            } catch (Exception ignore) {}
            try {
                var confs = confirmacionRepo.findByScrimId(s.getId());
                if (confs != null && confs.stream().anyMatch(c -> c.getUsuario() != null && userId.equals(c.getUsuario().getId()))) return true;
            } catch (Exception ignore) {}
        }
        return false;
    }

    private java.util.Set<Long> activeScrimIdsForUser(Long userId){
        java.util.Set<Long> ids = new java.util.HashSet<>();
        if (userId == null) return ids;
        for (Scrim s : scrimRepo.findAll()) {
            if (!isActive(s)) continue;
            try {
                if (s.getCreador() != null && userId.equals(s.getCreador().getId())) ids.add(s.getId());
            } catch (Exception ignore) {}
            try {
                var p = postulacionRepo.findByScrimIdAndUsuarioId(s.getId(), userId);
                if (p != null) ids.add(s.getId());
            } catch (Exception ignore) {}
            try {
                var confs = confirmacionRepo.findByScrimId(s.getId());
                if (confs != null && confs.stream().anyMatch(c -> c.getUsuario() != null && userId.equals(c.getUsuario().getId()))) ids.add(s.getId());
            } catch (Exception ignore) {}
        }
        return ids;
    }

    public Postulacion postular(Long scrimId, PostulacionRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();
        // El organizador no puede postularse a su propio scrim
        if (s.getCreador() != null && u.getId() != null && u.getId().equals(s.getCreador().getId())) {
            throw new IllegalArgumentException("El organizador no puede postularse a su propio scrim; usá Confirmar desde 'Mis scrims'.");
        }
        // Política: un usuario no puede unirse/crear otra scrim si ya participa o creó una que sigue activa (distinta de esta)
        var actives = activeScrimIdsForUser(u.getId());
        boolean tieneOtraActiva = actives.stream().anyMatch(id -> !id.equals(scrimId));
        if (tieneOtraActiva) {
            throw new IllegalArgumentException("Ya estás participando/organizando una scrim activa. Finalizala o cancelala antes de unirte a otra.");
        }
        // Si el scrim tiene cupos y ya está completo (aceptadas + organizador), bloquear nuevas postulaciones
        if (s.getCuposTotal() != null) {
            long aceptadasActuales = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
            int org = (s.getCreador() != null ? 1 : 0);
            if (aceptadasActuales + org >= s.getCuposTotal()) {
                throw new IllegalArgumentException("Scrim completo, en espera de confirmación");
            }
        }
        // Validaciones duras por rango/latencia si están definidas en el scrim
        Integer mmr = u.getMmr();
        Integer min = s.getRangoMin();
        Integer max = s.getRangoMax();
        if ((min != null || max != null)) {
            if (mmr == null) {
                throw new IllegalArgumentException("MMR requerido para postular a este scrim");
            }
            if (min != null && mmr < min) {
                throw new IllegalArgumentException("MMR fuera de rango (menor al mínimo)");
            }
            if (max != null && mmr > max) {
                throw new IllegalArgumentException("MMR fuera de rango (mayor al máximo)");
            }
        }
        Integer latMax = s.getLatenciaMax();
        Integer latUsuario = u.getLatencia();
        if (latMax != null && latUsuario != null && latUsuario > latMax) {
            throw new IllegalArgumentException("Latencia supera el máximo permitido para este scrim");
        }
        Postulacion p = new Postulacion();
        p.setScrim(s); p.setUsuario(u); p.setRolDeseado(req.rolDeseado);
        if (req.comentario != null && !req.comentario.isBlank()) {
            p.setComentario(req.comentario);
        }
        // Auto-aceptar hasta completar cupos si esta definido (contando al organizador como 1 lugar ocupado)
        if (s.getCuposTotal() != null) {
            long aceptadasActuales = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
            int org = (s.getCreador() != null ? 1 : 0);
            p.setEstado((aceptadasActuales + org) < s.getCuposTotal() ? PostulacionEstado.ACEPTADA : PostulacionEstado.PENDIENTE);
        } else {
            p.setEstado(PostulacionEstado.PENDIENTE);
        }
    Postulacion saved = postulacionRepo.save(p);
    // Asegurar visibilidad inmediata de la nueva postulacion en la transaccion antes de contar
    postulacionRepo.flush();
        // Si llegamos a cupo completo (aceptadas + organizador >= cuposTotal) o al menos 2 ocupados, mover a LOBBY_ARMADO
        long aceptadas = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
        int org = (s.getCreador() != null ? 1 : 0);
        boolean listoLobby = (s.getCuposTotal() != null && (aceptadas + org) >= s.getCuposTotal()) || (aceptadas + org) >= 2;
        if (listoLobby) {
            cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO);
        }
        // Auditoría: postulación
        try {
            var det = java.util.Map.of(
                "usuarioId", u.getId(),
                "rolDeseado", req.rolDeseado != null ? req.rolDeseado.name() : null,
                "comentario", req.comentario
            );
            auditLogService.log("Scrim", s.getId(), "POSTULAR", u.getEmail(), det);
        } catch (Exception ignore) {}
        return saved;
    }

    public Confirmacion confirmar(Long scrimId, ConfirmacionRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();

        // No permitir múltiples confirmaciones por el mismo usuario en el mismo scrim
        if (confirmacionRepo.existsByScrimIdAndUsuarioId(scrimId, u.getId())) {
            throw new IllegalArgumentException("Ya confirmaste este scrim.");
        }
        // Robustecer el flujo: si aún está en BUSCANDO, intentar armar lobby
        if (s.getEstado() == ScrimEstado.BUSCANDO) {
            // Si el usuario está postulado y no aceptado, promoverlo a ACEPTADA para no bloquear el avance
            try {
                var post = postulacionRepo.findByScrimIdAndUsuarioId(scrimId, u.getId());
                if (post != null && post.getEstado() != PostulacionEstado.ACEPTADA) {
                    post.setEstado(PostulacionEstado.ACEPTADA);
                    postulacionRepo.save(post);
                    postulacionRepo.flush();
                }
            } catch (Exception ignore) {}
            // Recalcular aceptadas y avanzar a LOBBY_ARMADO si se alcanza umbral (cupos o >=2)
            long aceptadas = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
            Integer cupos = s.getCuposTotal();
            boolean listoLobby = (cupos != null && aceptadas >= cupos) || aceptadas >= 2;
            if (listoLobby && s.getEstado() == ScrimEstado.BUSCANDO) {
                try { cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO); } catch (Exception ignore) { }
                // refrescar entidad para estado actualizado
                s = scrimRepo.findById(scrimId).orElseThrow();
            }
        }
        Confirmacion c = new Confirmacion();
        c.setScrim(s); c.setUsuario(u); c.setConfirmado(req.confirmado);
    Confirmacion saved = confirmacionRepo.save(c);
    // Asegurar visibilidad inmediata
    confirmacionRepo.flush();
        // Umbral mínimo: con 2 confirmaciones avanzamos, o con cupos completos si así aplica
        if (req.confirmado) {
            long confirmados = confirmacionRepo.countByScrimIdAndConfirmado(scrimId, true);
            Integer cupos = s.getCuposTotal();
            boolean listo = (cupos != null && confirmados >= cupos) || confirmados >= 2;
            if (listo) {
                // Si todavía no está en LOBBY_ARMADO, intentar transicionar primero
                if (s.getEstado() == ScrimEstado.BUSCANDO) {
                    try { cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO); } catch (Exception ignore) { }
                }
                // Confirmado automáticamente y arranca al instante
                try { cambiarEstado(scrimId, ScrimEstado.CONFIRMADO); } catch (IllegalStateException ise) {
                    // Si no aplica por estado, dejamos solo el avance a LOBBY si ocurrió
                }
                try {
                    iniciar(scrimId);
                } catch (Exception ignore) { /* si la transición no aplica, lo dejamos en CONFIRMADO */ }
            }
        }
        // Auditoría: confirmación
        try {
            var det = java.util.Map.of(
                "usuarioId", u.getId(),
                "confirmado", req.confirmado
            );
            auditLogService.log("Scrim", s.getId(), "CONFIRMAR", u.getEmail(), det);
        } catch (Exception ignore) {}
        return saved;
    }

    public Object ejecutarCommand(Long scrimId, String command, CommandRequest req){
        var cmd = commands.values().stream().filter(c -> c.name().equalsIgnoreCase(command)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comando no soportado: "+command));
        return cmd.execute(scrimId, req.actorId, req.payload);
    }

    public void cancelar(Long scrimId){
        var fin = cambiarEstado(scrimId, ScrimEstado.CANCELADO);
        try { auditLogService.log("Scrim", fin.getId(), "CANCELAR", "system", java.util.Map.of()); } catch (Exception ignore) {}
    }
    public void finalizar(Long scrimId){
        // SIEMPRE genera estadísticas automáticas aleatorias y ajusta MMR
        TransactionTemplate tmpl = new TransactionTemplate(txManager);
        tmpl.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        tmpl.execute(status -> {
            Scrim s = scrimRepo.findById(scrimId).orElseThrow();
            
            // 1. Garantizar que exista un Match
            var matchOpt = matchRepo.findByScrim(s);
            if (matchOpt.isEmpty()) {
                Match m = new Match();
                m.setScrim(s);
                m.setEstado(Match.EstadoMatch.EN_PROGRESO);
                matchRepo.save(m);
                matchRepo.flush();
                matchOpt = matchRepo.findByScrim(s);
            }

            // 2. SIEMPRE armar equipos desde confirmaciones (organizador + confirmados)
            var confs = confirmacionRepo.findByScrimId(scrimId);
            java.util.List<Usuario> jugadores = new java.util.ArrayList<>();
            
            // Primero el organizador
            if (s.getCreador() != null) {
                jugadores.add(s.getCreador());
            }
            
            // Luego todos los confirmados (que no sean el organizador)
            for (var c : confs) {
                try { 
                    if (c.isConfirmado() && c.getUsuario()!=null) {
                        Usuario u = c.getUsuario();
                        // No duplicar al organizador
                        if (s.getCreador() == null || !u.getId().equals(s.getCreador().getId())) {
                            jugadores.add(u);
                        }
                    }
                } catch (Exception ignore) {}
            }

            // Ahora SIEMPRE crear los 2 equipos (tomando todos los disponibles)
            if (jugadores.size() >= 2) {
                // Repartir en 2 equipos de forma alternada
                java.util.List<Usuario> eq1 = new java.util.ArrayList<>();
                java.util.List<Usuario> eq2 = new java.util.ArrayList<>();
                for (int i=0; i<jugadores.size(); i++) { 
                    if (i%2==0) eq1.add(jugadores.get(i)); 
                    else eq2.add(jugadores.get(i)); 
                }
                
                // Crear los equipos
                crearEquipoAutomatico(s, "Equipo Azul", "AZUL", eq1);
                crearEquipoAutomatico(s, "Equipo Rojo", "ROJO", eq2);
                equipoRepo.flush();
                
                // Recargar equipos
                var equipos = equipoRepo.findByScrimId(scrimId);
                
                // SIEMPRE finalizar con estadísticas
                var req = generarFinalizacionAutomatica(s, equipos);
                finalizarMatch(scrimId, req);
            } else {
                // No debería llegar aquí si está lleno, pero por si acaso
                throw new IllegalStateException("No hay suficientes jugadores confirmados (necesita al menos 2)");
            }
            
            return null;
        });
    }

    private Equipo crearEquipoAutomatico(Scrim s, String nombre, String lado, java.util.List<Usuario> miembros){
        Equipo e = new Equipo();
        e.setScrim(s); e.setNombre(nombre); e.setLado(lado);
        e.setCapitan(miembros.get(0));
        var saved = equipoRepo.save(e);
        for (int i = 0; i < miembros.size(); i++){
            var me = new MiembroEquipo();
            me.setEquipo(saved);
            me.setUsuario(miembros.get(i));
            me.setRol(rolPorPosicion(i));
            saved.getMiembros().add(me);
        }
        saved.setPromedioMMR(miembros.stream().map(Usuario::getMmr).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0.0));
        return equipoRepo.save(saved);
    }

    private String rolPorPosicion(int i){
        String[] roles = {"TOP","JUNGLE","MID","ADC","SUPPORT"};
        return roles[i % roles.length];
    }

    public Object cargarEstadisticas(Long scrimId, EstadisticaRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();
        Estadistica e = new Estadistica();
        e.setScrim(s); e.setUsuario(u); e.setMvp(req.mvp); e.setObservaciones(req.observaciones);
        var res = estadisticaRepo.save(e);
        try {
            var det = java.util.Map.of(
                "usuarioId", u.getId(),
                "mvp", req.mvp,
                "observaciones", req.observaciones
            );
            auditLogService.log("Scrim", s.getId(), "CARGAR_ESTADISTICAS", u.getEmail(), det);
        } catch (Exception ignore) {}
        return res;
    }

    // Búsqueda con filtros simples usando Specification (campos principales)
    public List<Scrim> buscar(String juegoNombre, String region, String formato, Integer rangoMin, Integer rangoMax, Integer latenciaMax,
                              java.time.LocalDateTime fechaDesde, java.time.LocalDateTime fechaHasta){
        return scrimRepo.findAll((root, q, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (juegoNombre != null) {
                predicates.add(cb.like(cb.lower(root.get("juego").get("nombre")), "%"+juegoNombre.toLowerCase()+"%"));
            }
            if (region != null) {
                predicates.add(cb.equal(root.get("region"), region));
            }
            if (formato != null) {
                predicates.add(cb.equal(root.get("formato"), formato));
            }
            if (rangoMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rangoMin"), rangoMin));
            }
            if (rangoMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rangoMax"), rangoMax));
            }
            if (latenciaMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("latenciaMax"), latenciaMax));
            }
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), fechaHasta));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    public Scrim cambiarEstado(Long scrimId, ScrimEstado nuevo){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        var ctx = new ScrimContext(s);
        if (!ctx.canTransitionTo(nuevo)) {
            throw new IllegalStateException("Transicion invalida de "+s.getEstado()+" a "+nuevo);
        }
        var anterior = s.getEstado();
        ctx.transitionTo(nuevo);
        Scrim saved = scrimRepo.save(ctx.getScrim());
        bus.publish(new com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimStateChanged(saved.getId(), anterior.name(), nuevo.name(), java.time.Instant.now()));
        // Log de auditoría del cambio de estado
        try {
            var detalles = java.util.Map.of(
                "desde", anterior != null ? anterior.name() : null,
                "hacia", nuevo != null ? nuevo.name() : null
            );
            auditLogService.log("Scrim", saved.getId(), "CAMBIO_ESTADO", "system", detalles);
        } catch (Exception ignore) {}
        return saved;
    }

    public List<Scrim> listar(){ return scrimRepo.findAll(); }

    public Scrim obtener(Long id){ return scrimRepo.findById(id).orElseThrow(); }

    public List<Postulacion> listarPostulaciones(Long scrimId){
        // Devolver desde el repositorio para reflejar estados más frescos
        return postulacionRepo.findByScrimId(scrimId);
    }

    public java.util.List<Confirmacion> listarConfirmaciones(Long scrimId){
        return confirmacionRepo.findByScrimId(scrimId);
    }

    // Exponer datos de match/equipos para consola/detalles
    public java.util.Optional<Match> obtenerMatch(Long scrimId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        return matchRepo.findByScrim(s);
    }

    public java.util.List<Equipo> listarEquipos(Long scrimId){
        return equipoRepo.findByScrimId(scrimId);
    }

    public java.util.List<EstadisticaJugadorMatch> listarEstadisticasJugadorMatch(Long scrimId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        var mo = matchRepo.findByScrim(s);
        if (mo.isEmpty()) return java.util.List.of();
        return estadisticaMatchRepo.findByMatch(mo.get());
    }

    public java.util.List<HistorialUsuario> obtenerHistorialUsuario(Usuario usuario){
        var historial = historialRepo.findByUsuario(usuario);
        // Ordenar por fecha descendente (más reciente primero)
        historial.sort((a, b) -> {
            if (a.getFechaRegistro() == null && b.getFechaRegistro() == null) return 0;
            if (a.getFechaRegistro() == null) return 1;
            if (b.getFechaRegistro() == null) return -1;
            return b.getFechaRegistro().compareTo(a.getFechaRegistro());
        });
        return historial;
    }

    public Scrim iniciar(Long scrimId){
        // Valid transition: CONFIRMADO -> EN_JUEGO, and create Match if none exists
        scrimRepo.findById(scrimId).orElseThrow();
        Scrim saved = cambiarEstado(scrimId, ScrimEstado.EN_JUEGO);
        matchRepo.findByScrim(saved).orElseGet(() -> {
            Match m = new Match();
            m.setScrim(saved);
            m.setEstado(Match.EstadoMatch.EN_PROGRESO);
            return matchRepo.save(m);
        });
        try { auditLogService.log("Scrim", saved.getId(), "INICIAR", "system", java.util.Map.of()); } catch (Exception ignore) {}
        return saved;
    }

    public Scrim runMatchmaking(Long scrimId, String strategy){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        // Candidatos: usuarios con postulacion PENDIENTE al scrim
    var pendientes = postulacionRepo.findByScrimAndEstado(s, PostulacionEstado.PENDIENTE);
        java.util.List<Usuario> candidatos = new java.util.ArrayList<>();
        for (var p : pendientes) candidatos.add(p.getUsuario());

        // Seleccionar estrategia
        com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.MatchmakingStrategy strat;
        switch (strategy.toLowerCase()){
            case "latency":
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.ByLatencyStrategy();
                break;
            case "history":
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.ByHistoryStrategy();
                break;
            case "hybrid":
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.HybridStrategy();
                break;
            case "mmr":
            default:
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.ByMMRStrategy();
        }

        // Seleccionar y aceptar hasta cupo
        var seleccionados = strat.seleccionar(candidatos, s);
        for (var u : seleccionados) {
            // Encontrar su postulacion por repositorio para evitar colecciones desactualizadas
            var postOpt = postulacionRepo.findByScrimIdAndUsuarioId(scrimId, u.getId());
            if (postOpt != null && postOpt.getEstado() == PostulacionEstado.PENDIENTE) {
                postOpt.setEstado(PostulacionEstado.ACEPTADA);
                postulacionRepo.save(postOpt);
            }
        }
        postulacionRepo.flush();

        // Si hay al menos 2 aceptadas avanzamos a LOBBY_ARMADO (o si cupos completo)
        long aceptadas = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
        boolean listoLobby = (s.getCuposTotal() != null && aceptadas >= s.getCuposTotal()) || aceptadas >= 2;
        if (listoLobby && s.getEstado() != ScrimEstado.LOBBY_ARMADO) {
            var moved = cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO);
            try {
                var det = java.util.Map.of(
                    "strategy", strategy,
                    "seleccionados", seleccionados.stream().map(Usuario::getId).toList()
                );
                auditLogService.log("Scrim", moved.getId(), "MATCHMAKING", "system", det);
            } catch (Exception ignore) {}
            return moved;
        }
        // No audit log cuando no hay cambio de estado para evitar ruido
        return s;
    }

    public void retirarPostulacion(Long scrimId, Long postulacionId){
        var p = postulacionRepo.findById(postulacionId).orElseThrow();
        if (!p.getScrim().getId().equals(scrimId)) throw new IllegalArgumentException("Postulacion no pertenece al scrim");
        postulacionRepo.delete(p);
        try {
            var det = java.util.Map.of(
                "postulacionId", postulacionId
            );
            auditLogService.log("Scrim", p.getScrim().getId(), "RETIRAR_POSTULACION", "system", det);
        } catch (Exception ignore) {}
    }

    // Resultados del match: elegir ganador/perdedor, set K/D/A agregadas y ajustar MMR/historial
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Object finalizarMatch(Long scrimId, FinalizarMatchRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Match m = matchRepo.findByScrim(s).orElseThrow();

        // Equipos (asumimos 2 equipos existentes para el scrim)
    var equipos = equipoRepo.findByScrimId(scrimId);
    if (equipos.size() < 2) throw new IllegalStateException("Se requieren 2 equipos para finalizar el match");
        Equipo ganador = equipos.stream().filter(e -> e.getId().equals(req.equipoGanadorId)).findFirst().orElseThrow();
        Equipo perdedor = equipos.stream().filter(e -> !e.getId().equals(req.equipoGanadorId)).findFirst().orElseThrow();

        m.setEquipoGanador(ganador);
        m.setEquipoPerdedor(perdedor);
        m.setEstado(Match.EstadoMatch.FINALIZADO);
        m.setDuracionMinutos(req.duracionMinutos);
        m.setObservaciones(req.observaciones);
        m.setKillsEquipoGanador(req.killsGanador);
        m.setKillsEquipoPerdedor(req.killsPerdedor);
        m.setTorresDestruidasGanador(req.torresGanador);
        m.setTorresDestruidasPerdedor(req.torresPerdedor);
        m.setGoldDiferencia(req.goldDiff);

        // Persistir estadísticas por jugador si llegan
        if (req.jugadores != null) {
            for (FinalizarMatchRequest.EstadisticaJugador j : req.jugadores) {
                Usuario u = usuarioRepo.findById(j.usuarioId).orElseThrow();
                var ejm = new EstadisticaJugadorMatch();
                ejm.setMatch(m); ejm.setUsuario(u); ejm.setEquipo(j.equipoId != null ? equipos.stream().filter(e -> e.getId().equals(j.equipoId)).findFirst().orElse(null) : null);
                ejm.setKills(j.kills); ejm.setMuertes(j.muertes); ejm.setAsistencias(j.asistencias);
                ejm.setMinions(j.minions); ejm.setOro(j.oro); ejm.setDanoCausado(j.danoCausado); ejm.setDanoRecibido(j.danoRecibido);
                ejm.setTorres(j.torres); ejm.setObjetivos(j.objetivos);
                estadisticaMatchRepo.save(ejm);
            }
        }

        // Calcular delta MMR simple: +15 a ganadores, -12 a perdedores (evitar negativos)
        int deltaWin = req.deltaWin != null ? req.deltaWin : 15;
        int deltaLose = req.deltaLose != null ? req.deltaLose : -12;

        for (Equipo e : equipos) {
            boolean esGanador = e.getId().equals(ganador.getId());
            int delta = esGanador ? deltaWin : deltaLose;
            for (MiembroEquipo me : e.getMiembros()) {
                Usuario u = me.getUsuario();
                Integer antes = u.getMmr() != null ? u.getMmr() : 0;
                int despues = Math.max(0, antes + delta);
                u.setMmr(despues);
                usuarioRepo.save(u);

                HistorialUsuario h = new HistorialUsuario();
                h.setUsuario(u);
                h.setMatch(m);
                h.setResultado(esGanador ? HistorialUsuario.Resultado.VICTORIA : HistorialUsuario.Resultado.DERROTA);
                h.setMmrAntes(antes);
                h.setMmrDespues(despues);
                historialRepo.save(h);
            }
        }

    matchRepo.save(m);
    var fin = cambiarEstado(scrimId, ScrimEstado.FINALIZADO);
    try {
        var det = java.util.Map.of(
            "equipoGanadorId", ganador.getId(),
            "equipoPerdedorId", perdedor.getId(),
            "duracionMinutos", req.duracionMinutos,
            "killsG", req.killsGanador,
            "killsP", req.killsPerdedor
        );
        auditLogService.log("Scrim", fin.getId(), "FINALIZAR_MATCH", "system", det);
    } catch (Exception ignore) {}
    // Respuesta liviana para tests: include estado FINALIZADO y match id
    return java.util.Map.of(
        "scrimId", fin.getId(),
        "estado", fin.getEstado().name(),
        "matchId", m.getId()
    );
    }

    // Generador de estadísticas aleatorias y request de finalización
    private FinalizarMatchRequest generarFinalizacionAutomatica(Scrim s, java.util.List<Equipo> equipos){
        var req = new com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FinalizarMatchRequest();
        java.util.Random rnd = new java.util.Random();

        // Elegir ganador (sesgo leve al mayor promedio MMR si está disponible)
        Equipo e0 = equipos.get(0);
        Equipo e1 = equipos.get(1);
        double mmr0 = e0.getPromedioMMR() != null ? e0.getPromedioMMR() : 0.0;
        double mmr1 = e1.getPromedioMMR() != null ? e1.getPromedioMMR() : 0.0;
        double p0 = 0.5;
        if (mmr0 + mmr1 > 0) {
            p0 = mmr0 / (mmr0 + mmr1);
            // suavizar el sesgo para no ser determinista
            p0 = 0.35 + 0.3 * p0; // en rango ~[0.35,0.65]
        }
        boolean gana0 = rnd.nextDouble() < p0;
        var ganador = gana0 ? e0 : e1;
        var perdedor = gana0 ? e1 : e0;
        req.equipoGanadorId = ganador.getId();

        // Duración típica 20-45 minutos
        req.duracionMinutos = 20 + rnd.nextInt(26);
        req.observaciones = "Auto-finalizado por consola (stats aleatorias)";

        // Kills y torres por equipo (plausibles para MOBA)
        int killsG = 22 + rnd.nextInt(20); // 22-41
        int killsP = 8 + rnd.nextInt(18);  // 8-25
        int torresG = 7 + rnd.nextInt(5);  // 7-11
        int torresP = rnd.nextInt(6);      // 0-5
        int goldDiff = 1500 + rnd.nextInt(10000); // 1.5k - 11.5k

        req.killsGanador = killsG;
        req.killsPerdedor = killsP;
        req.torresGanador = torresG;
        req.torresPerdedor = torresP;
        req.goldDiff = goldDiff;
        req.deltaWin = 15;
        req.deltaLose = -12;

        // Armar estadísticas por jugador distribuyendo aproximadamente las kills/torres
        var jugadores = new java.util.ArrayList<com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FinalizarMatchRequest.EstadisticaJugador>();

        java.util.function.BiConsumer<Equipo, Integer> poblarEquipo = (equipo, killsEquipo) -> {
            var miembros = equipo.getMiembros();
            if (miembros == null || miembros.isEmpty()) return;
            // repartir kills entre miembros
            int restantes = killsEquipo;
            for (int i = 0; i < miembros.size(); i++) {
                var me = miembros.get(i);
                var ej = new com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FinalizarMatchRequest.EstadisticaJugador();
                ej.usuarioId = me.getUsuario().getId();
                ej.equipoId = equipo.getId();
                int partesRestantes = miembros.size() - i;
                int asignadas = Math.max(0, restantes > 0 ? rnd.nextInt(restantes + 1) / partesRestantes : 0);
                if (i == miembros.size() - 1) asignadas = restantes; // último se lleva lo que queda
                ej.kills = asignadas;
                restantes -= asignadas;
                ej.muertes = rnd.nextInt(8); // 0-7
                ej.asistencias = 3 + rnd.nextInt(18); // 3-20
                ej.minions = 80 + rnd.nextInt(220); // 80-299
                ej.oro = 6000 + rnd.nextInt(14000); // 6k-20k
                ej.danoCausado = 7000 + rnd.nextInt(43000); // 7k-50k
                ej.danoRecibido = 4000 + rnd.nextInt(26000); // 4k-30k
                ej.torres = rnd.nextInt(3); // 0-2
                ej.objetivos = rnd.nextInt(4); // 0-3
                jugadores.add(ej);
            }
        };

        poblarEquipo.accept(ganador, killsG);
        poblarEquipo.accept(perdedor, killsP);
        req.jugadores = jugadores;
        return req;
    }

    public Map<String, Object> lobbySummary(Long scrimId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
    long pendientes = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.PENDIENTE);
    long aceptadas = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
        long confirmadas = confirmacionRepo.countByScrimIdAndConfirmado(scrimId, true);
        return java.util.Map.of(
                "scrimId", s.getId(),
                "estado", s.getEstado().name(),
                "cuposTotal", s.getCuposTotal(),
                "pendientes", pendientes,
                "aceptadas", aceptadas,
                "confirmadas", confirmadas,
                "postulaciones", postulacionRepo.findByScrimId(scrimId),
                "confirmaciones", confirmacionRepo.findByScrimId(scrimId)
        );
    }

    // Waitlist
    public WaitlistEntry addToWaitlist(Long scrimId, Long usuarioId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(usuarioId).orElseThrow();
        var e = new WaitlistEntry();
        e.setScrim(s); e.setUsuario(u);
        var saved = waitlistRepo.save(e);
        try {
            var det = java.util.Map.of(
                "usuarioId", u.getId()
            );
            auditLogService.log("Scrim", s.getId(), "WAITLIST_ADD", u.getEmail(), det);
        } catch (Exception ignore) {}
        return saved;
    }

    public java.util.List<WaitlistEntry> getWaitlist(Long scrimId){
        return waitlistRepo.findByScrimIdOrderByCreatedAtAsc(scrimId);
    }

    public Postulacion promoverDesdeWaitlist(Long scrimId){
        var list = waitlistRepo.findByScrimIdOrderByCreatedAtAsc(scrimId);
        if (list.isEmpty()) throw new IllegalStateException("Waitlist vacia");
        var first = list.get(0);
        // crear postulacion aceptada
        var req = new PostulacionRequest();
        req.usuarioId = first.getUsuario().getId();
        req.rolDeseado = Rol.FLEX;
        var p = postular(scrimId, req);
        waitlistRepo.delete(first);
        try {
            var det = java.util.Map.of(
                "usuarioId", first.getUsuario().getId()
            );
            auditLogService.log("Scrim", first.getScrim().getId(), "WAITLIST_PROMOVER", "system", det);
        } catch (Exception ignore) {}
        return p;
    }

    // Feedback
    public Feedback crearFeedback(Long scrimId, FeedbackRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.autorId).orElseThrow();
        Feedback f = new Feedback();
        f.setScrim(s); f.setAutor(u); f.setRating(req.rating); f.setComentario(req.comentario);
        var savedFb = feedbackRepo.save(f);
        try {
            var det = java.util.Map.of(
                "autorId", u.getId(),
                "rating", req.rating
            );
            auditLogService.log("Scrim", s.getId(), "CREAR_FEEDBACK", u.getEmail(), det);
        } catch (Exception ignore) {}
        return savedFb;
    }

    public java.util.List<Feedback> listarFeedback(Long scrimId){
        return feedbackRepo.findByScrimId(scrimId);
    }

    public ReporteConducta crearReporte(CrearReporteRequest req){
        Scrim s = scrimRepo.findById(req.scrimId).orElseThrow();
        Usuario reportado = usuarioRepo.findById(req.reportadoId).orElseThrow();
        Usuario reportante = req.reportanteId != null ? usuarioRepo.findById(req.reportanteId).orElse(null) : null;
        ReporteConducta r = new ReporteConducta();
        r.setScrim(s);
        r.setReportante(reportante);
        r.setReportado(reportado);
        MotivoReporte motivoEnum = null;
        if (req.motivo != null) {
            try {
                motivoEnum = MotivoReporte.valueOf(req.motivo);
            } catch (IllegalArgumentException ex) {
                // try case-insensitive match and fail with clear message if not found
                motivoEnum = java.util.Arrays.stream(MotivoReporte.values())
                        .filter(m -> m.name().equalsIgnoreCase(req.motivo))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Motivo invalido: " + req.motivo));
            }
        }
        r.setMotivo(motivoEnum);
        r.setDescripcion(req.descripcion); // Guardar descripción adicional
        var savedRep = reporteRepo.save(r);
        try {
            var det = java.util.Map.of(
                "scrimId", req.scrimId,
                "reportadoId", req.reportadoId,
                "motivo", motivoEnum != null ? motivoEnum.name() : null
            );
            auditLogService.log("Scrim", s.getId(), "CREAR_REPORTE", "system", det);
        } catch (Exception ignore) {}
        return savedRep;
    }

    public java.util.List<ReporteConducta> listarReportes(EstadoReporte estado){
        return estado != null ? reporteRepo.findByEstado(estado) : reporteRepo.findAll();
    }

    public java.util.List<ReporteConducta> listarReportesPorReportante(Usuario reportante){
        return reporteRepo.findByReportante(reportante);
    }

    public ReporteConducta resolverReporte(Long id, ResolverReporteRequest req){
        var r = reporteRepo.findById(id).orElseThrow();
        if (req.estado != null) r.setEstado(req.estado);
        if (req.sancion != null) r.setSancion(req.sancion);
        var savedRes = reporteRepo.save(r);
        try {
            var det = java.util.Map.of(
                "reporteId", r.getId(),
                "estado", r.getEstado() != null ? r.getEstado().name() : null,
                "sancion", r.getSancion()
            );
            auditLogService.log("Scrim", r.getScrim() != null ? r.getScrim().getId() : null, "RESOLVER_REPORTE", "system", det);
        } catch (Exception ignore) {}
        return savedRes;
    }
}
