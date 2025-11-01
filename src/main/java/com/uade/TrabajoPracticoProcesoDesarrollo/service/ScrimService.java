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
import com.uade.TrabajoPracticoProcesoDesarrollo.service.gamerules.GameRulesRegistry;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.AuditLogService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimContext;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.*;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.FinalizarMatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import org.springframework.core.env.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
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
    private final GameRulesRegistry gameRules;
    private final AuditLogService auditLogService;
    private final Environment env;
    private final MeterRegistry meterRegistry;
    private final Map<String, com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.ScrimCommand> commands;

    public ScrimService(ScrimRepository scrimRepo, JuegoRepository juegoRepo, UsuarioRepository usuarioRepo, PostulacionRepository postulacionRepo, DomainEventBus bus,
                        ConfirmacionRepository confirmacionRepo, EstadisticaRepository estadisticaRepo,
                        WaitlistRepository waitlistRepo, FeedbackRepository feedbackRepo, ReporteConductaRepository reporteRepo,
                        MatchRepository matchRepo, EstadisticaJugadorMatchRepository estadisticaMatchRepo,
                        HistorialUsuarioRepository historialRepo, EquipoRepository equipoRepo,
                        Map<String, com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.ScrimCommand> commands,
                        MeterRegistry meterRegistry,
                        Environment env,
                        GameRulesRegistry gameRules,
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
        this.meterRegistry = meterRegistry;
        this.env = env;
        this.gameRules = gameRules;
        this.auditLogService = auditLogService;
    }

    public Scrim crearScrim(CrearScrimRequest req){
        Juego juego = juegoRepo.findById(req.juegoId).orElseThrow();
        // Per-game validations (hooks)
        gameRules.forGame(juego).validateCreate(req, juego);

    Scrim s = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.builders.ScrimBuilder()
        .juego(juego)
        .region(req.region)
        .formato(req.formato)
        .cupos(req.cuposTotal)
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
        Scrim saved = scrimRepo.save(s);

        // Publicar evento de creación para disparar notificaciones
        bus.publish(new com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent(saved.getId()));
        // Audit creation
        try { auditLogService.log("Scrim", saved.getId(), "CREAR", "system", Map.of("juego", juego.getNombre(), "formato", req.formato)); } catch(Exception ignored){}
        return saved;
    }

    public Postulacion postular(Long scrimId, PostulacionRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();
        // Per-game postulacion validations
        gameRules.forGame(s.getJuego()).validatePostular(req, s, u);
        // Defense-in-depth: organizer cannot postular to their own scrim
        if (s.getCreador() != null && s.getCreador().getId() != null && s.getCreador().getId().equals(u.getId())) {
            if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.organizer").increment();
            throw new BusinessException("Organizador no puede postularse a su propio scrim");
        }
        // Only accept postulaciones while scrim is in BUSCANDO
        if (s.getEstado() != null && s.getEstado() != ScrimEstado.BUSCANDO) {
            if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.state").increment();
            throw new BusinessException("No es posible postularse a un scrim que no está en estado BUSCANDO");
        }
        // Prevent duplicate postulacion
        var existing = postulacionRepo.findByScrimIdAndUsuarioId(scrimId, u.getId());
        if (existing != null) {
            if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.duplicate").increment();
            throw new BusinessException("Usuario ya se postulo a este scrim");
        }
        // Enforce cooldown: a user with an active cooldown cannot postular
        if (u.getCooldownHasta() != null && u.getCooldownHasta().isAfter(java.time.LocalDateTime.now())) {
            if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.cooldown").increment();
            throw new BusinessException("Usuario en cooldown hasta " + u.getCooldownHasta());
        }
        // Validaciones estrictas: si el scrim declara requisitos, el usuario debe tener datos y estar dentro
        if (s.getRangoMin() != null || s.getRangoMax() != null) {
            if (u.getMmr() == null) {
                if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.mmr_missing").increment();
                throw new BusinessException("Usuario sin MMR no puede postular a scrim con requisito de rango");
            }
            if (s.getRangoMin() != null && u.getMmr() < s.getRangoMin()) {
                if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.mmr_too_low").increment();
                throw new BusinessException("Usuario fuera del rango MMR minimo permitido para este scrim");
            }
            if (s.getRangoMax() != null && u.getMmr() > s.getRangoMax()) {
                if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.mmr_too_high").increment();
                throw new BusinessException("Usuario fuera del rango MMR maximo permitido para este scrim");
            }
        }
        if (s.getLatenciaMax() != null) {
            if (u.getLatencia() == null) {
                if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.latency_missing").increment();
                throw new BusinessException("Usuario sin latencia conocida no puede postular a scrim con requisito de latencia");
            }
            if (u.getLatencia() > s.getLatenciaMax()) {
                if (meterRegistry != null) meterRegistry.counter("scrim.postular.rejected.latency_too_high").increment();
                throw new BusinessException("Latencia del usuario supera el máximo permitido para este scrim");
            }
        }

        Postulacion p = new Postulacion();
        p.setScrim(s); p.setUsuario(u); p.setRolDeseado(req.rolDeseado);
        if (req.comentario != null && !req.comentario.isBlank()) {
            p.setComentario(req.comentario);
        }
        // Auto-aceptar hasta completar cupos si esta definido
        if (s.getCuposTotal() != null) {
            long aceptadasActuales = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
            p.setEstado(aceptadasActuales < s.getCuposTotal() ? PostulacionEstado.ACEPTADA : PostulacionEstado.PENDIENTE);
        } else {
            p.setEstado(PostulacionEstado.PENDIENTE);
        }
    Postulacion saved = postulacionRepo.save(p);
    if (meterRegistry != null) meterRegistry.counter("scrim.postular.accepted").increment();
    // Asegurar visibilidad inmediata de la nueva postulacion en la transaccion antes de contar
    postulacionRepo.flush();
        // Si llegamos a cupo completo (Aceptadas == cuposTotal) o al menos 2 aceptadas, mover a LOBBY_ARMADO
        long aceptadas = postulacionRepo.countByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
        boolean listoLobby = (s.getCuposTotal() != null && aceptadas >= s.getCuposTotal()) || aceptadas >= 2;
        if (listoLobby) {
            cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO);
        }
        // Audit postulacion
        try { auditLogService.log("Postulacion", saved.getId(), "CREAR", "system", Map.of("scrimId", scrimId, "usuarioId", req.usuarioId)); } catch(Exception ignored){}
        return saved;
    }

    public Confirmacion confirmar(Long scrimId, ConfirmacionRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();
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
        return saved;
    }

    public Object ejecutarCommand(Long scrimId, String command, CommandRequest req){
        var cmd = commands.values().stream().filter(c -> c.name().equalsIgnoreCase(command)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comando no soportado: "+command));
        return cmd.execute(scrimId, req.actorId, req.payload);
    }

    public void cancelar(Long scrimId, Long actorId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        // If scrim has a creator, only the creator may cancel
        if (s.getCreador() != null && s.getCreador().getId() != null) {
            if (actorId == null || !s.getCreador().getId().equals(actorId)) {
                if (meterRegistry != null) meterRegistry.counter("scrim.cancel.denied").increment();
                throw new AccessDeniedException("Solo el creador puede cancelar este scrim");
            } else {
                // success metric + log
                if (meterRegistry != null) meterRegistry.counter("scrim.cancel.success").increment();
                // optional: log
            }
        } else {
            // No creator: allow only in dev/test/local profiles (fallback for legacy data)
            String[] active = env.getActiveProfiles();
            boolean allowed = false;
            for (String p : active) if (p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test") || p.equalsIgnoreCase("local")) allowed = true;
            if (!allowed) {
                if (meterRegistry != null) meterRegistry.counter("scrim.cancel.denied").increment();
                throw new AccessDeniedException("Operacion prohibida: scrim sin creador en ambiente productivo");
            }
        }
        cambiarEstado(scrimId, ScrimEstado.CANCELADO);
        // Al cancelar, registrar posible no-show para confirmaciones que no generaron estadisticas
        applyNoShowPenalties(scrimId);
    }

    public void finalizar(Long scrimId, Long actorId){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        // If scrim has a creator, only the creator may finalizar
        if (s.getCreador() != null && s.getCreador().getId() != null) {
            if (actorId == null || !s.getCreador().getId().equals(actorId)) {
                if (meterRegistry != null) meterRegistry.counter("scrim.finalize.denied").increment();
                throw new AccessDeniedException("Solo el creador puede finalizar este scrim");
            } else {
                // success metric
                if (meterRegistry != null) meterRegistry.counter("scrim.finalize.success").increment();
            }
        } else {
            // No creator: allow only in dev/test/local profiles (fallback for legacy data)
            String[] active = env.getActiveProfiles();
            boolean allowed = false;
            for (String p : active) if (p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test") || p.equalsIgnoreCase("local")) allowed = true;
            if (!allowed) {
                if (meterRegistry != null) meterRegistry.counter("scrim.finalize.denied").increment();
                throw new AccessDeniedException("Operacion prohibida: scrim sin creador en ambiente productivo");
            }
        }
        cambiarEstado(scrimId, ScrimEstado.FINALIZADO);
        // Al finalizar, aplicar no-shows a quienes confirmaron pero no registraron estadisticas
        applyNoShowPenalties(scrimId);
    }

    private void applyNoShowPenalties(Long scrimId) {
        // Obtener confirmaciones confirmadas
        var confirmaciones = confirmacionRepo.findByScrimId(scrimId);
        var estadisticas = estadisticaRepo.findByScrimId(scrimId);
        java.util.Set<Long> usuariosConEstadistica = new java.util.HashSet<>();
        for (var e : estadisticas) if (e.getUsuario() != null && e.getUsuario().getId() != null) usuariosConEstadistica.add(e.getUsuario().getId());

        for (var c : confirmaciones) {
            if (!c.isConfirmado()) continue;
            var u = c.getUsuario();
            if (u == null || u.getId() == null) continue;
            if (usuariosConEstadistica.contains(u.getId())) continue; // participó, no penalizar

            // aplicar strike y cooldown similar a la moderación manual
            int strikes = u.getStrikes() != null ? u.getStrikes() : 0;
            strikes++;
            u.setStrikes(strikes);
            if (strikes >= 3) {
                u.setCooldownHasta(java.time.LocalDateTime.now().plusDays(7));
            }
            usuarioRepo.save(u);
            // publicar evento para notificar a otros subsistemas
            bus.publish(new com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.StrikeAppliedEvent(u.getId(), u.getStrikes()));
        }
    }

    public Object cargarEstadisticas(Long scrimId, EstadisticaRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.usuarioId).orElseThrow();
        Estadistica e = new Estadistica();
        e.setScrim(s); e.setUsuario(u); e.setMvp(req.mvp); e.setObservaciones(req.observaciones);
        return estadisticaRepo.save(e);
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
        // Audit the state change
        try { auditLogService.log("Scrim", saved.getId(), "EstadoCambiar", "system", Map.of("from", anterior != null ? anterior.name() : null, "to", nuevo.name())); } catch(Exception ignored){}
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
        return saved;
    }

    public Scrim runMatchmaking(Long scrimId, String strategy){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        // Candidatos: usuarios con postulacion PENDIENTE al scrim
    var pendientes = postulacionRepo.findByScrimAndEstado(s, PostulacionEstado.PENDIENTE);
        java.util.List<Usuario> candidatos = new java.util.ArrayList<>();
        for (var p : pendientes) candidatos.add(p.getUsuario());

    // Allow per-game hook to validate or veto team formation
    gameRules.forGame(s.getJuego()).validateFormTeam(s, candidatos);

    // Seleccionar estrategia
        com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.MatchmakingStrategy strat;
        switch (strategy.toLowerCase()){
            case "latency":
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.ByLatencyStrategy();
                break;
            case "history":
                strat = new com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking.ByHistoryStrategy();
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
            return cambiarEstado(scrimId, ScrimEstado.LOBBY_ARMADO);
        }
        return s;
    }

    public void retirarPostulacion(Long scrimId, Long postulacionId){
        var p = postulacionRepo.findById(postulacionId).orElseThrow();
        if (!p.getScrim().getId().equals(scrimId)) throw new IllegalArgumentException("Postulacion no pertenece al scrim");
        postulacionRepo.delete(p);
    }

    // Resultados del match: elegir ganador/perdedor, set K/D/A agregadas y ajustar MMR/historial
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
    // Respuesta liviana para tests: include estado FINALIZADO y match id
    return java.util.Map.of(
        "scrimId", fin.getId(),
        "estado", fin.getEstado().name(),
        "matchId", m.getId()
    );
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
        return waitlistRepo.save(e);
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
        return p;
    }

    // Feedback
    public Feedback crearFeedback(Long scrimId, FeedbackRequest req){
        Scrim s = scrimRepo.findById(scrimId).orElseThrow();
        Usuario u = usuarioRepo.findById(req.autorId).orElseThrow();
        Feedback f = new Feedback();
        f.setScrim(s); f.setAutor(u); f.setRating(req.rating); f.setComentario(req.comentario);
        return feedbackRepo.save(f);
    }

    public java.util.List<Feedback> listarFeedback(Long scrimId){
        return feedbackRepo.findByScrimId(scrimId);
    }

    public ReporteConducta crearReporte(CrearReporteRequest req){
        Scrim s = scrimRepo.findById(req.scrimId).orElseThrow();
        Usuario reportado = usuarioRepo.findById(req.reportadoId).orElseThrow();
        ReporteConducta r = new ReporteConducta();
        r.setScrim(s);
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
        return reporteRepo.save(r);
    }

    public java.util.List<ReporteConducta> listarReportes(EstadoReporte estado){
        return estado != null ? reporteRepo.findByEstado(estado) : reporteRepo.findAll();
    }

    public ReporteConducta resolverReporte(Long id, ResolverReporteRequest req){
        var r = reporteRepo.findById(id).orElseThrow();
        if (req.estado != null) r.setEstado(req.estado);
        if (req.sancion != null) r.setSancion(req.sancion);
        return reporteRepo.save(r);
    }
}
