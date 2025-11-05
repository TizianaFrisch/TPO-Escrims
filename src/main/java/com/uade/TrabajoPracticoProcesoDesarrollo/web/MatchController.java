package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.EventoMatch;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.EstadisticaJugadorMatch;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.*;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MatchController {
    private final ScrimRepository scrimRepository;
    private final MatchRepository matchRepository;
    private final EventoMatchRepository eventoRepository;
    private final EstadisticaJugadorMatchRepository estadisticaJugadorMatchRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final NotificacionService notificacionService;

    public MatchController(ScrimRepository scrimRepository, MatchRepository matchRepository,
                           EventoMatchRepository eventoRepository,
                           EstadisticaJugadorMatchRepository estadisticaJugadorMatchRepository,
                           UsuarioRepository usuarioRepository,
                           EquipoRepository equipoRepository,
                           NotificacionService notificacionService) {
        this.scrimRepository = scrimRepository;
        this.matchRepository = matchRepository;
        this.eventoRepository = eventoRepository;
        this.estadisticaJugadorMatchRepository = estadisticaJugadorMatchRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
        this.notificacionService = notificacionService;
    }

    // GET matches de un scrim (lista, aunque hoy es 0..1)
    @GetMapping("/api/scrims/{scrimId}/matches")
    public ResponseEntity<List<Match>> matchesPorScrim(@PathVariable Long scrimId){
        Scrim s = scrimRepository.findById(scrimId).orElseThrow();
        return ResponseEntity.ok(matchRepository.findByScrim(s).map(java.util.List::of).orElse(java.util.List.of()));
    }

    // GET eventos por match
    @GetMapping("/api/matches/{matchId}/eventos")
    public ResponseEntity<Object> eventosPorMatch(@PathVariable Long matchId){
        Match m = matchRepository.findById(matchId).orElseThrow();
        return ResponseEntity.ok(eventoRepository.findByMatch(m));
    }

    // GET estadísticas por match
    @GetMapping("/api/matches/{matchId}/estadisticas")
    public ResponseEntity<Object> estadisticasPorMatch(@PathVariable Long matchId){
        Match m = matchRepository.findById(matchId).orElseThrow();
        return ResponseEntity.ok(estadisticaJugadorMatchRepository.findByMatch(m));
    }

    // === New: registrar evento en match ===
    public static class RegistrarEventoRequest {
        public String tipo; // EventoMatch.TipoEvento
        public Long usuarioId; // opcional
        public Long equipoId;  // opcional
        public String descripcion; // opcional
        public String momento; // ISO opcional
    }

    @PostMapping("/api/matches/{matchId}/eventos")
    public ResponseEntity<EventoMatch> registrarEvento(@PathVariable Long matchId, @RequestBody RegistrarEventoRequest req){
        Match m = matchRepository.findById(matchId).orElseThrow();
        EventoMatch e = new EventoMatch();
        e.setMatch(m);
        if (req.usuarioId != null) {
            Usuario u = usuarioRepository.findById(req.usuarioId).orElseThrow();
            e.setUsuario(u);
        }
        if (req.equipoId != null) {
            var eq = equipoRepository.findById(req.equipoId).orElseThrow();
            e.setEquipo(eq);
        }
        if (req.tipo != null) e.setTipo(EventoMatch.TipoEvento.valueOf(req.tipo.toUpperCase()));
        if (req.descripcion != null) e.setDescripcion(req.descripcion);
        if (req.momento != null) e.setMomento(java.time.LocalDateTime.parse(req.momento));
        EventoMatch saved = eventoRepository.save(e);

        // optional: notify on notable events
        if (saved.getTipo() == EventoMatch.TipoEvento.BARON || saved.getTipo() == EventoMatch.TipoEvento.DRAGON || saved.getTipo() == EventoMatch.TipoEvento.KILL) {
            var scrim = m.getScrim();
            String msg = "Evento: " + saved.getTipo() + (saved.getDescripcion() != null ? (" - " + saved.getDescripcion()) : "");
            scrim.getPostulaciones().forEach(p -> {
                notificacionService.crearYEnviarATodosCanales(p.getUsuario().getId(), "Evento de match", msg, TipoNotificacion.EN_JUEGO);
            });
        }

        return ResponseEntity.ok(saved);
    }

    // === New: registrar estadistica de jugador ===
    public static class RegistrarEstadisticaJugadorRequest {
        public Long usuarioId;
        public Long equipoId; // opcional
        public Integer kills;
        public Integer muertes;
        public Integer asistencias;
        public Integer minions;
        public Integer oro;
        public Integer danoCausado;
        public Integer danoRecibido;
        public Integer torres;
        public Integer objetivos;
    }

    @PostMapping("/api/matches/{matchId}/estadisticas")
    public ResponseEntity<EstadisticaJugadorMatch> registrarEstadistica(@PathVariable Long matchId, @RequestBody RegistrarEstadisticaJugadorRequest req){
        Match m = matchRepository.findById(matchId).orElseThrow();
        Usuario u = usuarioRepository.findById(req.usuarioId).orElseThrow();
        EstadisticaJugadorMatch sj = new EstadisticaJugadorMatch();
        sj.setMatch(m); sj.setUsuario(u);
        if (req.equipoId != null) {
            var eq = equipoRepository.findById(req.equipoId).orElseThrow();
            sj.setEquipo(eq);
        }
        sj.setKills(req.kills); sj.setMuertes(req.muertes); sj.setAsistencias(req.asistencias);
        sj.setMinions(req.minions); sj.setOro(req.oro); sj.setDanoCausado(req.danoCausado); sj.setDanoRecibido(req.danoRecibido);
        sj.setTorres(req.torres); sj.setObjetivos(req.objetivos);
        return ResponseEntity.ok(estadisticaJugadorMatchRepository.save(sj));
    }
}
