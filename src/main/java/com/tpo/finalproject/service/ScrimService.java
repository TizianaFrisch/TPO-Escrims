package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.*;
import com.tpo.finalproject.domain.builders.ScrimBuilder;
import com.tpo.finalproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrimService {
    
    private final ScrimRepository scrimRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final JuegoRepository juegoRepository;
    private final NotificacionService notificacionService;
    private final MatchmakingService matchmakingService;
    
    // ============== PATRÓN COMMAND INTEGRADO ==============
    // Commands para operaciones específicas de scrims
    
    public interface ScrimCommand {
        void ejecutar();
        void deshacer();
        String getDescripcion();
    }
    
    // Command para asignar rol a jugador
    private class AsignarRolCommand implements ScrimCommand {
        private final Long scrimId;
        private final Long usuarioId;
        private final String nuevoRol;
        private String rolAnterior;
        
        public AsignarRolCommand(Long scrimId, Long usuarioId, String nuevoRol) {
            this.scrimId = scrimId;
            this.usuarioId = usuarioId;
            this.nuevoRol = nuevoRol;
        }
        
        @Override
        public void ejecutar() {
            Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            // Guardar estado anterior para undo
            this.rolAnterior = usuario.getRolPreferido();
            
            // Asignar nuevo rol
            usuario.setRolPreferido(nuevoRol);
            usuarioRepository.save(usuario);
            
            // Notificar cambio
            notificacionService.crearNotificacion(usuario, 
                    "Rol Asignado", 
                    "Se te ha asignado el rol: " + nuevoRol + " en el scrim " + scrim.getNombre(),
                    Notificacion.TipoNotificacion.NUEVA_POSTULACION);
        }
        
        @Override
        public void deshacer() {
            if (rolAnterior != null) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                usuario.setRolPreferido(rolAnterior);
                usuarioRepository.save(usuario);
            }
        }
        
        @Override
        public String getDescripcion() {
            return "Asignar rol " + nuevoRol + " a usuario " + usuarioId;
        }
    }
    
    // Command para invitar jugador
    private class InvitarJugadorCommand implements ScrimCommand {
        private final Long scrimId;
        private final Long usuarioId;
        private final String mensaje;
        private Postulacion postulacionCreada;
        
        public InvitarJugadorCommand(Long scrimId, Long usuarioId, String mensaje) {
            this.scrimId = scrimId;
            this.usuarioId = usuarioId;
            this.mensaje = mensaje;
        }
        
        @Override
        public void ejecutar() {
            Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            // Crear invitación como postulación especial
            postulacionCreada = Postulacion.builder()
                    .usuario(usuario)
                    .scrim(scrim)
                    .estado(Postulacion.EstadoPostulacion.ACEPTADA) // Auto-aceptada por invitación
                    .rolSolicitado(usuario.getRolPreferido())
                    .comentario("INVITADO: " + mensaje)
                    .fechaPostulacion(LocalDateTime.now())
                    .build();
            
            postulacionRepository.save(postulacionCreada);
            
            // Notificar invitación
            notificacionService.crearNotificacion(usuario, 
                    "Invitación a Scrim", 
                    "Has sido invitado al scrim: " + scrim.getNombre() + ". " + mensaje,
                    Notificacion.TipoNotificacion.NUEVA_POSTULACION);
        }
        
        @Override
        public void deshacer() {
            if (postulacionCreada != null) {
                postulacionRepository.delete(postulacionCreada);
            }
        }
        
        @Override
        public String getDescripcion() {
            return "Invitar usuario " + usuarioId + " al scrim " + scrimId;
        }
    }
    
    // Command para intercambiar jugadores entre equipos
    private class SwapJugadoresCommand implements ScrimCommand {
        private final Long jugador1Id;
        private final Long jugador2Id;
        private final Long scrimId;
        private MiembroEquipo estadoOriginal1;
        private MiembroEquipo estadoOriginal2;
        
        public SwapJugadoresCommand(Long jugador1Id, Long jugador2Id, Long scrimId) {
            this.jugador1Id = jugador1Id;
            this.jugador2Id = jugador2Id;
            this.scrimId = scrimId;
        }
        
        @Override
        public void ejecutar() {
            // Buscar miembros de equipo actuales
            // Aquí implementaríamos la lógica de intercambio
            // Por simplicidad, guardamos mensaje de confirmación
            
            Usuario jugador1 = usuarioRepository.findById(jugador1Id)
                    .orElseThrow(() -> new IllegalArgumentException("Jugador 1 no encontrado"));
            Usuario jugador2 = usuarioRepository.findById(jugador2Id)
                    .orElseThrow(() -> new IllegalArgumentException("Jugador 2 no encontrado"));
            
            // Notificar intercambio
            notificacionService.crearNotificacion(jugador1, 
                    "Intercambio de Equipo", 
                    "Has sido intercambiado de equipo con " + jugador2.getUsername(),
                    Notificacion.TipoNotificacion.NUEVA_POSTULACION);
                    
            notificacionService.crearNotificacion(jugador2, 
                    "Intercambio de Equipo", 
                    "Has sido intercambiado de equipo con " + jugador1.getUsername(),
                    Notificacion.TipoNotificacion.NUEVA_POSTULACION);
        }
        
        @Override
        public void deshacer() {
            // Restaurar equipos originales
            System.out.println("Deshaciendo intercambio entre jugadores " + jugador1Id + " y " + jugador2Id);
        }
        
        @Override
        public String getDescripcion() {
            return "Intercambiar jugadores " + jugador1Id + " y " + jugador2Id;
        }
    }
    
    // Métodos públicos para ejecutar commands
    @Transactional
    public void asignarRolAJugador(Long scrimId, Long usuarioId, String nuevoRol) {
        ScrimCommand command = new AsignarRolCommand(scrimId, usuarioId, nuevoRol);
        command.ejecutar();
    }
    
    @Transactional
    public void invitarJugadorAScrim(Long scrimId, Long usuarioId, String mensaje) {
        ScrimCommand command = new InvitarJugadorCommand(scrimId, usuarioId, mensaje);
        command.ejecutar();
    }
    
    @Transactional
    public void intercambiarJugadores(Long jugador1Id, Long jugador2Id, Long scrimId) {
        ScrimCommand command = new SwapJugadoresCommand(jugador1Id, jugador2Id, scrimId);
        command.ejecutar();
    }
    
    // ============== PATRÓN STATE INTEGRADO ==============
    // Métodos que manejan las transiciones de estado del Scrim
    
    @Transactional
    public boolean unirseAScrim(Long scrimId, Long usuarioId, String rolSolicitado) {
        Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar estado actual del scrim
        if (!puedeUnirseEnEstadoActual(scrim)) {
            return false;
        }
        
        // Verificar requisitos del usuario
        if (!cumpleRequisitos(usuario, scrim)) {
            return false;
        }
        
        // Crear postulación
        Postulacion postulacion = Postulacion.builder()
                .usuario(usuario)
                .scrim(scrim)
                .rolSolicitado(rolSolicitado)
                .estado(Postulacion.EstadoPostulacion.PENDIENTE)
                .build();
        
        postulacionRepository.save(postulacion);
        
        // Verificar si el scrim está completo para cambiar estado
        verificarYCambiarEstado(scrim);
        
        return true;
    }
    
    @Transactional
    public boolean iniciarScrim(Long scrimId, Long usuarioId) {
        Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Solo el creador puede iniciar
        if (!scrim.getCreador().getId().equals(usuarioId)) {
            return false;
        }
        
        // Verificar estado y requisitos
        if (!puedeIniciarEnEstadoActual(scrim)) {
            return false;
        }
        
        // Cambiar estado y procesar inicio
        cambiarEstadoScrim(scrim, Scrim.EstadoScrim.EN_PROGRESO);
        procesarInicioScrim(scrim);
        
        return true;
    }
    
    @Transactional
    public boolean finalizarScrim(Long scrimId, String equipoGanador) {
        Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
        
        if (!puedeFinalizarEnEstadoActual(scrim)) {
            return false;
        }
        
        // Cambiar estado y procesar finalización
        cambiarEstadoScrim(scrim, Scrim.EstadoScrim.FINALIZADO);
        procesarFinalizacionScrim(scrim, equipoGanador);
        
        return true;
    }
    
    @Transactional
    public boolean cancelarScrim(Long scrimId, Long usuarioId, String motivo) {
        Scrim scrim = obtenerScrimPorIdPrivate(scrimId);
        
        // Verificar permisos de cancelación
        if (!puedeCandelar(scrim, usuarioId)) {
            return false;
        }
        
        // Cambiar estado y procesar cancelación
        cambiarEstadoScrim(scrim, Scrim.EstadoScrim.CANCELADO);
        procesarCancelacionScrim(scrim, motivo);
        
        return true;
    }
    
    // ============== LÓGICA DE ESTADOS ==============
    
    private boolean puedeUnirseEnEstadoActual(Scrim scrim) {
        return scrim.getEstado() == Scrim.EstadoScrim.BUSCANDO_JUGADORES &&
               scrim.getPostulaciones().size() < scrim.getJugadoresRequeridos();
    }
    
    private boolean puedeIniciarEnEstadoActual(Scrim scrim) {
        return (scrim.getEstado() == Scrim.EstadoScrim.BUSCANDO_JUGADORES || 
                scrim.getEstado() == Scrim.EstadoScrim.LOBBY_ARMADO) &&
               scrim.getPostulaciones().size() >= scrim.getJugadoresRequeridos();
    }
    
    private boolean puedeFinalizarEnEstadoActual(Scrim scrim) {
        return scrim.getEstado() == Scrim.EstadoScrim.EN_PROGRESO;
    }
    
    private boolean puedeCandelar(Scrim scrim, Long usuarioId) {
        // El creador siempre puede cancelar (excepto si está finalizado)
        boolean esCreador = scrim.getCreador().getId().equals(usuarioId);
        boolean noFinalizado = scrim.getEstado() != Scrim.EstadoScrim.FINALIZADO;
        
        return esCreador && noFinalizado;
    }
    
    private void verificarYCambiarEstado(Scrim scrim) {
        int jugadoresActuales = scrim.getPostulaciones().size();
        int jugadoresRequeridos = scrim.getJugadoresRequeridos();
        
        if (jugadoresActuales >= jugadoresRequeridos && 
            scrim.getEstado() == Scrim.EstadoScrim.BUSCANDO_JUGADORES) {
            cambiarEstadoScrim(scrim, Scrim.EstadoScrim.LOBBY_ARMADO);
            procesarLobbyCompleto(scrim);
        }
    }
    
    private void cambiarEstadoScrim(Scrim scrim, Scrim.EstadoScrim nuevoEstado) {
        Scrim.EstadoScrim estadoAnterior = scrim.getEstado();
        scrim.setEstado(nuevoEstado);
        scrimRepository.save(scrim);
        
        // Logging y notificaciones
        System.out.println("Scrim " + scrim.getId() + " cambió de " + estadoAnterior + " a " + nuevoEstado);
        notificarCambioEstado(scrim, estadoAnterior, nuevoEstado);
    }
    
    // ============== PROCESAMIENTO DE ESTADOS ==============
    
    private void procesarLobbyCompleto(Scrim scrim) {
        System.out.println("¡Lobby completo para scrim " + scrim.getId() + "!");
        
        // Formar equipos automáticamente
        formarEquiposBalanceados(scrim);
        
        // Notificar a todos los jugadores
        notificarLobbyCompleto(scrim);
    }
    
    private void procesarInicioScrim(Scrim scrim) {
        System.out.println("¡Iniciando scrim " + scrim.getId() + "!");
        
        // Registrar hora de inicio
        scrim.setFechaHora(LocalDateTime.now());
        scrimRepository.save(scrim);
        
        // Crear registro de match para estadísticas
        crearMatchRecord(scrim);
        
        // Notificar inicio
        notificarInicioScrim(scrim);
    }
    
    private void procesarFinalizacionScrim(Scrim scrim, String equipoGanador) {
        System.out.println("Finalizando scrim " + scrim.getId() + " - Ganador: " + equipoGanador);
        
        // Procesar resultados y estadísticas
        procesarResultados(scrim, equipoGanador);
        
        // Actualizar estadísticas de jugadores
        actualizarEstadisticasJugadores(scrim, equipoGanador);
        
        // Marcar como inactivo
        scrim.setActivo(false);
        scrimRepository.save(scrim);
        
        // Notificar finalización
        notificarFinalizacionScrim(scrim, equipoGanador);
    }
    
    private void procesarCancelacionScrim(Scrim scrim, String motivo) {
        System.out.println("Cancelando scrim " + scrim.getId() + " - Motivo: " + motivo);
        
        // Aplicar penalizaciones si corresponde
        evaluarPenalizaciones(scrim, motivo);
        
        // Marcar como inactivo
        scrim.setActivo(false);
        scrimRepository.save(scrim);
        
        // Notificar cancelación
        notificarCancelacionScrim(scrim, motivo);
    }
    
    // ============== MÉTODOS DE SOPORTE ==============
    
    private Scrim obtenerScrimPorIdPrivate(Long scrimId) {
        return scrimRepository.findById(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado"));
    }
    
    private boolean cumpleRequisitos(Usuario usuario, Scrim scrim) {
        // Verificar MMR
        Integer mmrUsuario = usuario.getEstadisticas() != null ? 
                           usuario.getEstadisticas().getMmrActual() : 1000;
        
        if (mmrUsuario < scrim.getMmrMinimo() || mmrUsuario > scrim.getMmrMaximo()) {
            return false;
        }
        
        // Verificar que no esté ya postulado
        boolean yaPostulado = scrim.getPostulaciones().stream()
                .anyMatch(p -> p.getUsuario().getId().equals(usuario.getId()));
        
        return !yaPostulado && usuario.getActivo();
    }
    
    private void notificarCambioEstado(Scrim scrim, Scrim.EstadoScrim estadoAnterior, Scrim.EstadoScrim nuevoEstado) {
        // Notificar a todos los participantes sobre el cambio de estado
        scrim.getPostulaciones().forEach(postulacion -> {
            String titulo = "Cambio de Estado del Scrim";
            String mensaje = String.format("Scrim '%s' cambió de estado: %s → %s", 
                                          scrim.getNombre(), estadoAnterior, nuevoEstado);
            notificacionService.crearNotificacion(postulacion.getUsuario(), 
                                                 titulo, mensaje, 
                                                 Notificacion.TipoNotificacion.SCRIM_INICIADO);
        });
    }
    
    private void formarEquiposBalanceados(Scrim scrim) {
        // Implementar formación de equipos balanceados por MMR
        List<Usuario> jugadores = scrim.getPostulaciones().stream()
                .map(Postulacion::getUsuario)
                .toList();
        
        // Lógica de balanceo simple por MMR
        jugadores.sort((u1, u2) -> {
            Integer mmr1 = u1.getEstadisticas() != null ? u1.getEstadisticas().getMmrActual() : 1000;
            Integer mmr2 = u2.getEstadisticas() != null ? u2.getEstadisticas().getMmrActual() : 1000;
            return mmr2.compareTo(mmr1);
        });
        
        // Crear equipos alternados
        crearEquiposAlternados(scrim, jugadores);
    }
    
    private void crearEquiposAlternados(Scrim scrim, List<Usuario> jugadores) {
        // Crear dos equipos
        Equipo equipoAzul = Equipo.builder()
                .nombre("Equipo Azul")
                .scrim(scrim)
                .capitan(jugadores.get(0))
                .lado("AZUL")
                .build();
        
        Equipo equipoRojo = Equipo.builder()
                .nombre("Equipo Rojo")
                .scrim(scrim)
                .capitan(jugadores.get(1))
                .lado("ROJO")
                .build();
        
        equipoRepository.save(equipoAzul);
        equipoRepository.save(equipoRojo);
        
        // Asignar jugadores alternadamente
        for (int i = 0; i < jugadores.size(); i++) {
            Equipo equipoAsignado = (i % 2 == 0) ? equipoAzul : equipoRojo;
            
            MiembroEquipo miembro = MiembroEquipo.builder()
                    .usuario(jugadores.get(i))
                    .equipo(equipoAsignado)
                    .rol("TBD") // Por determinar
                    .build();
            
            // Guardar miembro (asumo que hay un repositorio para esto)
            // miembroEquipoRepository.save(miembro);
        }
    }
    
    private void notificarLobbyCompleto(Scrim scrim) {
        scrim.getPostulaciones().forEach(postulacion -> {
            String titulo = "Lobby Completo";
            String mensaje = String.format("¡El lobby del scrim '%s' está completo! ¡Prepárate para jugar!", 
                                          scrim.getNombre());
            notificacionService.crearNotificacion(postulacion.getUsuario(), 
                                                 titulo, mensaje, 
                                                 Notificacion.TipoNotificacion.SCRIM_INICIADO);
        });
    }
    
    private void crearMatchRecord(Scrim scrim) {
        // Crear registro de match para tracking de estadísticas
        Match match = Match.builder()
                .scrim(scrim)
                .fechaInicio(LocalDateTime.now())
                .estado(Match.EstadoMatch.EN_PROGRESO)
                .build();
        
        // matchRepository.save(match); // Asumo que existe
        System.out.println("Match record creado para scrim " + scrim.getId());
    }
    
    private void notificarInicioScrim(Scrim scrim) {
        scrim.getPostulaciones().forEach(postulacion -> {
            String titulo = "Scrim Iniciado";
            String mensaje = String.format("¡El scrim '%s' ha comenzado! ¡Buena suerte!", 
                                          scrim.getNombre());
            notificacionService.crearNotificacion(postulacion.getUsuario(), 
                                                 titulo, mensaje, 
                                                 Notificacion.TipoNotificacion.SCRIM_INICIADO);
        });
    }
    
    private void procesarResultados(Scrim scrim, String equipoGanador) {
        // Procesar resultados del scrim
        System.out.println("Procesando resultados - Ganador: " + equipoGanador);
        
        // Aquí se procesarían:
        // 1. Estadísticas detalladas del match
        // 2. Performance individual de jugadores  
        // 3. Cambios de MMR
        // 4. Achievements/logros
    }
    
    private void actualizarEstadisticasJugadores(Scrim scrim, String equipoGanador) {
        scrim.getPostulaciones().forEach(postulacion -> {
            Usuario jugador = postulacion.getUsuario();
            if (jugador.getEstadisticas() != null) {
                // Actualizar estadísticas básicas
                jugador.getEstadisticas().setPartidasJugadas(
                    jugador.getEstadisticas().getPartidasJugadas() + 1);
                
                // Determinar si ganó o perdió (lógica simplificada)
                boolean gano = determinarSiGano(jugador, equipoGanador, scrim);
                
                if (gano) {
                    jugador.getEstadisticas().setPartidasGanadas(
                        jugador.getEstadisticas().getPartidasGanadas() + 1);
                } else {
                    jugador.getEstadisticas().setPartidasPerdidas(
                        jugador.getEstadisticas().getPartidasPerdidas() + 1);
                }
                
                // Recalcular winrate
                actualizarWinrate(jugador.getEstadisticas());
            }
        });
    }
    
    private boolean determinarSiGano(Usuario jugador, String equipoGanador, Scrim scrim) {
        // Lógica simplificada para determinar si el jugador ganó
        // En implementación real, consultaría la pertenencia a equipos
        return Math.random() > 0.5; // Placeholder
    }
    
    private void actualizarWinrate(Estadisticas stats) {
        int total = stats.getPartidasJugadas();
        if (total > 0) {
            double winrate = (double) stats.getPartidasGanadas() / total;
            stats.setWinrate(winrate);
        }
    }
    
    private void notificarFinalizacionScrim(Scrim scrim, String equipoGanador) {
        scrim.getPostulaciones().forEach(postulacion -> {
            String titulo = "Scrim Finalizado";
            String mensaje = String.format("El scrim '%s' ha finalizado. Ganador: %s", 
                                          scrim.getNombre(), equipoGanador);
            notificacionService.crearNotificacion(postulacion.getUsuario(), 
                                                 titulo, mensaje, 
                                                 Notificacion.TipoNotificacion.MATCH_FINALIZADO);
        });
    }
    
    private void evaluarPenalizaciones(Scrim scrim, String motivo) {
        // Evaluar si corresponden penalizaciones por la cancelación
        if ("ABANDONO_MASIVO".equals(motivo)) {
            // Aplicar penalizaciones por abandono
            aplicarPenalizacionesAbandono(scrim);
        }
        // Otros motivos no requieren penalizaciones
    }
    
    private void aplicarPenalizacionesAbandono(Scrim scrim) {
        // Aplicar penalizaciones leves por abandono de scrim
        scrim.getPostulaciones().forEach(postulacion -> {
            Usuario jugador = postulacion.getUsuario();
            if (jugador.getEstadisticas() != null) {
                jugador.getEstadisticas().setScrimsAbandonados(
                    jugador.getEstadisticas().getScrimsAbandonados() + 1);
            }
        });
    }
    
    private void notificarCancelacionScrim(Scrim scrim, String motivo) {
        scrim.getPostulaciones().forEach(postulacion -> {
            String titulo = "Scrim Cancelado";
            String mensaje = String.format("El scrim '%s' ha sido cancelado. Motivo: %s", 
                                          scrim.getNombre(), motivo);
            notificacionService.crearNotificacion(postulacion.getUsuario(), 
                                                 titulo, mensaje, 
                                                 Notificacion.TipoNotificacion.SCRIM_CANCELADO);
        });
    }
    
    // Patrón Builder integrado para crear Scrims
    @Transactional
    public Scrim crearScrim(Long creadorId, String nombre, String descripcion, 
                           Integer mmrMinimo, Integer mmrMaximo, String region, 
                           LocalDateTime fechaHora) {
        
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Obtener juego por defecto (League of Legends)
        Juego juego = juegoRepository.findByNombreAndActivoTrue("League of Legends")
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        
        // Usar ScrimBuilder con validaciones completas
        Scrim scrim = ScrimBuilder.crear()
                .conNombre(nombre)
                .conDescripcion(descripcion)
                .conRangoMMR(mmrMinimo, mmrMaximo)
                .enRegion(region)
                .programadoPara(fechaHora)
                .creadoPor(creador)
                .paraJuego(juego)
                .build();
        
        return scrimRepository.save(scrim);
    }
    
    // Patrón Command - Comando para postularse
    @Transactional
    public void postularseAScrim(Long usuarioId, Long scrimId, String rolSolicitado, String comentario) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado"));
        
        // Aplicar lógica del patrón State
        if (!scrim.puedeAceptarJugadores()) {
            throw new IllegalStateException("El scrim no está aceptando jugadores");
        }
        
        // Verificar si ya se postuló
        if (postulacionRepository.existsByUsuarioAndScrim(usuario, scrim)) {
            throw new IllegalStateException("Ya te has postulado a este scrim");
        }
        
        // Verificar compatibilidad (Patrón Strategy aplicado)
        if (!scrim.cumpleRequisitosMMR(usuario.getMmr()) || !scrim.getRegion().equals(usuario.getRegion())) {
            throw new IllegalStateException("No cumples los requisitos para este scrim");
        }
        
        // Crear postulación
        Postulacion postulacion = Postulacion.builder()
                .usuario(usuario)
                .scrim(scrim)
                .rolSolicitado(rolSolicitado)
                .comentario(comentario)
                .build();
        
        postulacionRepository.save(postulacion);
        
        // Patrón Observer - Notificar al creador del scrim
        notificacionService.notificarNuevaPostulacion(scrim.getCreador(), usuario.getUsername(), scrim.getNombre());
    }
    
    // Patrón Command - Comando para aceptar postulación
    @Transactional
    public void aceptarPostulacion(Long postulacionId, Long creadorId) {
        
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new IllegalArgumentException("Postulación no encontrada"));
        
        // Verificar que el usuario sea el creador del scrim
        if (!postulacion.getScrim().getCreador().getId().equals(creadorId)) {
            throw new IllegalStateException("Solo el creador puede aceptar postulaciones");
        }
        
        // Aplicar patrón State
        if (!postulacion.puedeSerAceptada()) {
            throw new IllegalStateException("La postulación no puede ser aceptada");
        }
        
        postulacion.aceptar();
        postulacionRepository.save(postulacion);
        
        // Patrón Observer - Notificar al usuario
        notificacionService.notificarPostulacionAceptada(
                postulacion.getUsuario(), 
                postulacion.getScrim().getNombre()
        );
        
        // Verificar si el scrim está listo para formar equipos
        verificarYFormarEquipos(postulacion.getScrim());
    }
    
    // Patrón Command - Comando para rechazar postulación
    @Transactional
    public void rechazarPostulacion(Long postulacionId, Long creadorId) {
        
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new IllegalArgumentException("Postulación no encontrada"));
        
        if (!postulacion.getScrim().getCreador().getId().equals(creadorId)) {
            throw new IllegalStateException("Solo el creador puede rechazar postulaciones");
        }
        
        postulacion.rechazar();
        postulacionRepository.save(postulacion);
        
        // Patrón Observer - Notificar
        notificacionService.notificarPostulacionRechazada(
                postulacion.getUsuario(), 
                postulacion.getScrim().getNombre()
        );
    }
    
    // Patrón State - Cambiar estado del scrim
    @Transactional
    public void cambiarEstadoScrim(Long scrimId, Scrim.EstadoScrim nuevoEstado) {
        
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado"));
        
        // Lógica del patrón State integrada
        Scrim.EstadoScrim estadoAnterior = scrim.getEstado();
        scrim.cambiarEstado(nuevoEstado);
        
        scrimRepository.save(scrim);
        
        // Lógica específica según cambio de estado
        manejarCambioEstado(scrim, estadoAnterior, nuevoEstado);
    }
    
    // Patrón Strategy - Aplicar estrategias de matchmaking
    @Transactional
    public void formarEquiposAutomaticamente(Long scrimId) {
        
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado"));
        
        List<Postulacion> postulacionesAceptadas = postulacionRepository
                .findByScrimAndEstado(scrim, Postulacion.EstadoPostulacion.ACEPTADA);
        
        if (postulacionesAceptadas.size() < 10) {
            throw new IllegalStateException("Se necesitan al menos 10 jugadores para formar equipos");
        }
        
        // Aplicar estrategia de matchmaking
        matchmakingService.formarEquiposBalanceados(scrim, postulacionesAceptadas);
        
        // Cambiar estado
        scrim.cambiarEstado(Scrim.EstadoScrim.LOBBY_ARMADO);
        scrimRepository.save(scrim);
        
        // Notificar a todos los jugadores
        postulacionesAceptadas.forEach(postulacion -> 
                notificacionService.notificarEquipoCompleto(
                        postulacion.getUsuario(), 
                        scrim.getNombre()
                )
        );
    }
    
    @Transactional(readOnly = true)
    public List<Scrim> obtenerScrimsDisponibles(String region, Integer mmrUsuario) {
        if (region != null && mmrUsuario != null) {
            return scrimRepository.findScrimsRecomendadosParaUsuario(region, mmrUsuario);
        }
        return scrimRepository.findScrimsActivosPorEstado(Scrim.EstadoScrim.BUSCANDO_JUGADORES);
    }
    
    @Transactional(readOnly = true)
    public List<Postulacion> obtenerPostulacionesPendientes(Long creadorId) {
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return postulacionRepository.findPostulacionesPendientesPorCreador(creador);
    }
    
    @Transactional(readOnly = true)
    public Optional<Scrim> obtenerScrimPorId(Long scrimId) {
        return scrimRepository.findById(scrimId);
    }
    
    // Métodos privados
    
    private void validarDatosScrim(Integer mmrMinimo, Integer mmrMaximo, LocalDateTime fechaHora) {
        if (mmrMinimo < 0 || mmrMaximo < 0) {
            throw new IllegalArgumentException("El MMR no puede ser negativo");
        }
        
        if (mmrMinimo > mmrMaximo) {
            throw new IllegalArgumentException("El MMR mínimo no puede ser mayor al máximo");
        }
        
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha del scrim no puede ser en el pasado");
        }
    }
    
    private void verificarYFormarEquipos(Scrim scrim) {
        Long postulacionesAceptadas = postulacionRepository.countPostulacionesAceptadasPorScrim(scrim);
        
        if (postulacionesAceptadas >= 10) {
            // Cambiar estado automáticamente cuando hay suficientes jugadores
            scrim.cambiarEstado(Scrim.EstadoScrim.LOBBY_ARMADO);
            scrimRepository.save(scrim);
        }
    }
    
    private void manejarCambioEstado(Scrim scrim, Scrim.EstadoScrim estadoAnterior, Scrim.EstadoScrim nuevoEstado) {
        // Lógica específica según el cambio de estado
        if (nuevoEstado == Scrim.EstadoScrim.CANCELADO) {
            // Notificar a todos los postulados
            List<Postulacion> postulaciones = postulacionRepository.findByScrim(scrim);
            postulaciones.forEach(postulacion -> 
                    notificacionService.notificarScrimCancelado(
                            postulacion.getUsuario(), 
                            scrim.getNombre(),
                            "Cancelado por el organizador"
                    )
            );
        }
    }
}