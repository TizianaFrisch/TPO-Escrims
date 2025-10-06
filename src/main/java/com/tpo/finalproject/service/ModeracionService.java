package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.*;
import com.tpo.finalproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModeracionService {
    
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    
    // Patrón Chain of Responsibility - Handler base
    public abstract static class ReporteHandler {
        protected ReporteHandler siguiente;
        
        public void setSiguiente(ReporteHandler handler) {
            this.siguiente = handler;
        }
        
        public abstract boolean manejar(Reporte reporte, ModeracionService servicio);
        
        protected boolean pasarAlSiguiente(Reporte reporte, ModeracionService servicio) {
            if (siguiente != null) {
                return siguiente.manejar(reporte, servicio);
            }
            return false;
        }
    }
    
    // Handler para reportes automáticos (bot)
    private class BotHandler extends ReporteHandler {
        @Override
        public boolean manejar(Reporte reporte, ModeracionService servicio) {
            
            // Lógica automática para casos obvios
            if (esReporteObvio(reporte)) {
                return resolverReporteAutomaticamente(reporte);
            }
            
            // Si no puede resolver automáticamente, pasa al siguiente
            return pasarAlSiguiente(reporte, servicio);
        }
        
        private boolean esReporteObvio(Reporte reporte) {
            String descripcion = reporte.getDescripcion().toLowerCase();
            
            // Lista de palabras que indican reportes obvios
            String[] palabrasProhibidas = {"hack", "cheat", "bot", "afk", "troll", "int"};
            
            for (String palabra : palabrasProhibidas) {
                if (descripcion.contains(palabra)) {
                    return true;
                }
            }
            
            // Si el usuario reportado tiene muchos reportes recientes
            return usuarioTieneMuchosReportes(reporte.getReportado());
        }
    }
    
    // Handler para moderadores humanos
    private class ModeradorHandler extends ReporteHandler {
        @Override
        public boolean manejar(Reporte reporte, ModeracionService servicio) {
            
            // Buscar moderador disponible
            Usuario moderador = buscarModeradorDisponible();
            
            if (moderador != null) {
                return asignarAModerador(reporte, moderador);
            }
            
            // Si no hay moderadores disponibles, pasa al siguiente
            return pasarAlSiguiente(reporte, servicio);
        }
    }
    
    // Handler para administradores
    private class AdministradorHandler extends ReporteHandler {
        @Override
        public boolean manejar(Reporte reporte, ModeracionService servicio) {
            
            // Los administradores manejan casos complejos
            Usuario admin = buscarAdministradorDisponible();
            
            if (admin != null) {
                return asignarAAdministrador(reporte, admin);
            }
            
            // Si no hay administradores, queda pendiente
            return false;
        }
    }
    
    // Configurar la cadena de responsabilidad
    private ReporteHandler configurarCadena() {
        BotHandler botHandler = new BotHandler();
        ModeradorHandler moderadorHandler = new ModeradorHandler();
        AdministradorHandler adminHandler = new AdministradorHandler();
        
        botHandler.setSiguiente(moderadorHandler);
        moderadorHandler.setSiguiente(adminHandler);
        
        return botHandler;
    }
    
    // Patrón Command - Comando para crear reporte
    @Transactional
    public Reporte crearReporte(Long reportadorId, Long reportadoId, String motivo, String descripcion) {
        
        Usuario reportador = usuarioRepository.findById(reportadorId)
                .orElseThrow(() -> new IllegalArgumentException("Reportador no encontrado"));
        
        Usuario reportado = usuarioRepository.findById(reportadoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario reportado no encontrado"));
        
        // Validaciones
        if (reportadorId.equals(reportadoId)) {
            throw new IllegalArgumentException("No puedes reportarte a ti mismo");
        }
        
        // Crear reporte
        Reporte reporte = Reporte.builder()
                .reportador(reportador)
                .reportado(reportado)
                .motivo(motivo)
                .descripcion(descripcion)
                .build();
        
        Reporte reporteGuardado = reporteRepository.save(reporte);
        
        // Procesar inmediatamente con la cadena de responsabilidad
        procesarReporte(reporteGuardado);
        
        return reporteGuardado;
    }
    
    // Procesar reporte usando Chain of Responsibility
    @Transactional
    public void procesarReporte(Reporte reporte) {
        
        ReporteHandler cadena = configurarCadena();
        boolean procesado = cadena.manejar(reporte, this);
        
        if (!procesado) {
            // Si no pudo ser procesado, queda pendiente
            reporte.setEstado(Reporte.EstadoReporte.PENDIENTE);
            reporteRepository.save(reporte);
        }
    }
    
    // Métodos auxiliares para la cadena
    
    @Transactional
    public boolean resolverReporteAutomaticamente(Reporte reporte) {
        
        // Lógica de resolución automática
        String accion = determinarAccionAutomatica(reporte);
        
        Usuario botModerador = obtenerUsuarioBot();
        reporte.resolver(botModerador, accion);
        reporteRepository.save(reporte);
        
        // Aplicar sanción automática si es necesario
        aplicarSancion(reporte.getReportado(), accion);
        
        return true;
    }
    
    @Transactional
    public boolean asignarAModerador(Reporte reporte, Usuario moderador) {
        
        reporte.setEstado(Reporte.EstadoReporte.EN_REVISION);
        reporte.setModerador(moderador);
        reporteRepository.save(reporte);
        
        // Notificar al moderador
        notificacionService.crearNotificacion(moderador, 
                "Nuevo Reporte Asignado", 
                "Se te ha asignado un reporte para revisar",
                Notificacion.TipoNotificacion.NUEVA_POSTULACION);
        
        return true;
    }
    
    @Transactional
    public boolean asignarAAdministrador(Reporte reporte, Usuario admin) {
        
        reporte.setEstado(Reporte.EstadoReporte.EN_REVISION);
        reporte.setModerador(admin);
        reporteRepository.save(reporte);
        
        return true;
    }
    
    // Patrón Command - Comando para resolver reporte manualmente
    @Transactional
    public void resolverReporteManualmente(Long reporteId, Long moderadorId, String accion) {
        
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));
        
        Usuario moderador = usuarioRepository.findById(moderadorId)
                .orElseThrow(() -> new IllegalArgumentException("Moderador no encontrado"));
        
        // Verificar permisos
        if (!esModerador(moderador)) {
            throw new IllegalStateException("Usuario no tiene permisos de moderación");
        }
        
        reporte.resolver(moderador, accion);
        reporteRepository.save(reporte);
        
        // Aplicar sanción si es necesario
        aplicarSancion(reporte.getReportado(), accion);
        
        // Notificar resolución
        notificacionService.crearNotificacion(reporte.getReportado(),
                "Reporte Resuelto",
                "Un reporte en tu contra ha sido resuelto. Acción tomada: " + accion,
                Notificacion.TipoNotificacion.REPORTE_RESUELTO);
    }
    
    @Transactional(readOnly = true)
    public List<Reporte> obtenerReportesPendientes() {
        return reporteRepository.findReportesPendientesOrdenadosPorFecha();
    }
    
    @Transactional(readOnly = true)
    public List<Reporte> obtenerReportesParaModerador(Long moderadorId) {
        Usuario moderador = usuarioRepository.findById(moderadorId)
                .orElseThrow(() -> new IllegalArgumentException("Moderador no encontrado"));
        return reporteRepository.findByModerador(moderador);
    }
    
    // Métodos auxiliares
    
    public boolean usuarioTieneMuchosReportes(Usuario usuario) {
        Long reportesResueltos = reporteRepository.countReportesResueltosContraUsuario(usuario);
        return reportesResueltos >= 3; // Umbral configurable
    }
    
    public Usuario buscarModeradorDisponible() {
        return usuarioRepository.findByActivoTrue().stream()
                .filter(u -> u.getRol() == Usuario.Rol.MODERADOR)
                .findFirst()
                .orElse(null);
    }
    
    public Usuario buscarAdministradorDisponible() {
        return usuarioRepository.findByActivoTrue().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ADMINISTRADOR)
                .findFirst()
                .orElse(null);
    }
    
    private Usuario obtenerUsuarioBot() {
        return usuarioRepository.findByUsername("SYSTEM_BOT")
                .orElseGet(() -> crearUsuarioBot());
    }
    
    private Usuario crearUsuarioBot() {
        Usuario bot = Usuario.builder()
                .username("SYSTEM_BOT")
                .email("bot@sistema.com")
                .password("N/A")
                .region("GLOBAL")
                .rol(Usuario.Rol.MODERADOR)
                .build();
        
        return usuarioRepository.save(bot);
    }
    
    private String determinarAccionAutomatica(Reporte reporte) {
        
        if (usuarioTieneMuchosReportes(reporte.getReportado())) {
            return "Suspensión temporal de 24 horas por acumulación de reportes";
        }
        
        String descripcion = reporte.getDescripcion().toLowerCase();
        
        if (descripcion.contains("hack") || descripcion.contains("cheat")) {
            return "Investigación por posible uso de hacks";
        }
        
        if (descripcion.contains("afk") || descripcion.contains("abandono")) {
            return "Advertencia por abandono de partida";
        }
        
        return "Advertencia general";
    }
    
    private void aplicarSancion(Usuario usuario, String accion) {
        
        if (accion.contains("Suspensión")) {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        }
        
        // Aquí se podrían implementar más tipos de sanciones
        // Como reducción de MMR, restricción de funciones, etc.
    }
    
    private boolean esModerador(Usuario usuario) {
        return usuario.getRol() == Usuario.Rol.MODERADOR || 
               usuario.getRol() == Usuario.Rol.ADMINISTRADOR;
    }
}