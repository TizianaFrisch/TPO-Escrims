package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.Match;
import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Equipo;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.EstadisticaJugadorMatch;
import com.tpo.finalproject.domain.entities.EventoMatch;
import com.tpo.finalproject.repository.MatchRepository;
import com.tpo.finalproject.repository.EstadisticasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {
    
    private final MatchRepository matchRepository;
    private final EstadisticasRepository estadisticasRepository;
    private final NotificacionService notificacionService;
    
    @Transactional
    public Match iniciarMatch(Scrim scrim, Equipo equipoAzul, Equipo equipoRojo) {
        // Verificar que no existe un match para este scrim
        Optional<Match> matchExistente = matchRepository.findByScrim(scrim);
        if (matchExistente.isPresent()) {
            throw new IllegalStateException("Ya existe un match para este scrim");
        }
        
        Match match = Match.builder()
                .scrim(scrim)
                .estado(Match.EstadoMatch.EN_PROGRESO)
                .fechaInicio(LocalDateTime.now())
                .build();
        
        Match matchGuardado = matchRepository.save(match);
        
        // Crear evento de inicio
        crearEvento(matchGuardado, EventoMatch.TipoEvento.TEAMFIGHT, null, 
                   "Match iniciado entre " + equipoAzul.getNombre() + " vs " + equipoRojo.getNombre());
        
        return matchGuardado;
    }
    
    @Transactional
    public void finalizarMatch(Long matchId, Long equipoGanadorId, String observaciones) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match no encontrado"));
                
        if (!match.estaEnProgreso()) {
            throw new IllegalStateException("El match no está en progreso");
        }
        
        // Obtener equipos del scrim
        Equipo equipoGanador = match.getScrim().getEquipos().stream()
                .filter(e -> e.getId().equals(equipoGanadorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Equipo ganador no encontrado"));
                
        Equipo equipoPerdedor = match.getScrim().getEquipos().stream()
                .filter(e -> !e.getId().equals(equipoGanadorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Equipo perdedor no encontrado"));
        
        match.finalizarMatch(equipoGanador, equipoPerdedor);
        match.setObservaciones(observaciones);
        
        matchRepository.save(match);
        
        // Actualizar estadísticas de jugadores
        actualizarEstadisticasJugadores(match);
        
        // Enviar notificaciones
        enviarNotificacionesFinalizacion(match);
    }
    
    @Transactional
    public void registrarEstadisticaJugador(Long matchId, EstadisticaJugadorMatch estadistica) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match no encontrado"));
                
        estadistica.setMatch(match);
        match.getEstadisticasJugadores().add(estadistica);
        
        matchRepository.save(match);
    }
    
    @Transactional
    public void registrarEvento(Long matchId, EventoMatch.TipoEvento tipoEvento, 
                               Usuario jugadorPrincipal, String descripcion) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match no encontrado"));
                
        crearEvento(match, tipoEvento, jugadorPrincipal, descripcion);
    }
    
    private void crearEvento(Match match, EventoMatch.TipoEvento tipoEvento, 
                           Usuario jugador, String descripcion) {
        Integer minutoJuego = null;
        if (match.getFechaInicio() != null) {
            minutoJuego = (int) java.time.Duration.between(match.getFechaInicio(), LocalDateTime.now()).toMinutes();
        }
        
        EventoMatch evento = EventoMatch.builder()
                .match(match)
                .tipoEvento(tipoEvento)
                .minutoJuego(minutoJuego)
                .descripcion(descripcion)
                .jugadorPrincipal(jugador)
                .timestamp(LocalDateTime.now())
                .build();
        
        match.getEventos().add(evento);
        
        // Si es un evento importante, enviar notificación
        if (evento.esEventoImportante()) {
            enviarNotificacionEvento(evento);
        }
    }
    
    private void actualizarEstadisticasJugadores(Match match) {
        for (EstadisticaJugadorMatch stats : match.getEstadisticasJugadores()) {
            boolean gano = match.getEquipoGanador().getMiembros().stream()
                    .anyMatch(m -> m.getUsuario().equals(stats.getUsuario()));
            
            // Buscar o crear estadísticas del usuario
            var estadisticas = estadisticasRepository.findByUsuario(stats.getUsuario())
                    .orElseGet(() -> {
                        var nuevasStats = com.tpo.finalproject.domain.entities.Estadisticas.builder()
                                .usuario(stats.getUsuario())
                                .build();
                        return estadisticasRepository.save(nuevasStats);
                    });
            
            estadisticas.actualizarConMatch(stats, gano);
            estadisticasRepository.save(estadisticas);
        }
    }
    
    private void enviarNotificacionesFinalizacion(Match match) {
        // Notificar a todos los jugadores del resultado
        String mensaje = String.format("Match finalizado! %s vs %s - Ganador: %s", 
                match.getEquipoGanador().getNombre(),
                match.getEquipoPerdedor().getNombre(),
                match.getEquipoGanador().getNombre());
        
        // Notificar jugadores del equipo ganador
        match.getEquipoGanador().getMiembros().forEach(miembro -> {
            notificacionService.crearNotificacion(
                    miembro.getUsuario(),
                    "¡Victoria!",
                    "¡Felicidades! Has ganado el match: " + mensaje,
                    com.tpo.finalproject.domain.entities.Notificacion.TipoNotificacion.MATCH_FINALIZADO
            );
        });
        
        // Notificar jugadores del equipo perdedor
        match.getEquipoPerdedor().getMiembros().forEach(miembro -> {
            notificacionService.crearNotificacion(
                    miembro.getUsuario(),
                    "Match finalizado",
                    "El match ha terminado: " + mensaje + ". ¡Mejor suerte la próxima vez!",
                    com.tpo.finalproject.domain.entities.Notificacion.TipoNotificacion.MATCH_FINALIZADO
            );
        });
    }
    
    private void enviarNotificacionEvento(EventoMatch evento) {
        if (evento.getJugadorPrincipal() != null) {
            String mensaje = evento.generarNotificacion();
            notificacionService.crearNotificacion(
                    evento.getJugadorPrincipal(),
                    "Evento destacado en match",
                    mensaje,
                    com.tpo.finalproject.domain.entities.Notificacion.TipoNotificacion.EVENTO_MATCH
            );
        }
    }
    
    @Transactional(readOnly = true)
    public List<Match> obtenerMatchesDelUsuario(Usuario usuario) {
        return matchRepository.findMatchesConParticipacionDelUsuario(usuario);
    }
    
    @Transactional(readOnly = true)
    public Optional<Match> obtenerMatchPorScrim(Scrim scrim) {
        return matchRepository.findByScrim(scrim);
    }
    
    @Transactional(readOnly = true)
    public List<Match> obtenerMatchesRecientes() {
        return matchRepository.findMatchesFinalizadosRecientes();
    }
}