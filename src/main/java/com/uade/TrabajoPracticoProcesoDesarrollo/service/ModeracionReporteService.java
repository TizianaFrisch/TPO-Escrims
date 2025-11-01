package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ReporteConductaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.MotivoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.DomainEventBus;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.StrikeAppliedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.AuditLogService;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModeracionReporteService {
    private final ReporteConductaRepository repo;

    private final UsuarioRepository usuarioRepo;
    private final DomainEventBus bus;
    private final AuditLogService auditLogService;

    public ModeracionReporteService(ReporteConductaRepository repo, UsuarioRepository usuarioRepo, DomainEventBus bus, AuditLogService auditLogService) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
        this.bus = bus;
        this.auditLogService = auditLogService;
    }

    public List<ReporteConducta> listarPendientes() {
        return repo.findAll().stream().filter(r -> !Boolean.TRUE.equals(r.getResuelto())).toList();
    }

    public Optional<ReporteConducta> buscarPorId(Long id) {
        return repo.findById(id);
    }

    public void resolverReporte(Long id, EstadoReporte estado, String resolucion) {
        var reporte = repo.findById(id).orElseThrow();
        if (estado != null) reporte.setEstado(estado);
        if (resolucion != null) reporte.setResolucion(resolucion);
        reporte.setResuelto(true);
        repo.save(reporte);

        // Si el reporte se aprobó y el motivo es abandono, aplicar strike al usuario reportado
        if (estado == EstadoReporte.APROBADO && reporte.getMotivo() == MotivoReporte.ABANDONO) {
            var usuario = reporte.getReportado();
            if (usuario != null) {
                int strikes = usuario.getStrikes() != null ? usuario.getStrikes() : 0;
                strikes++;
                usuario.setStrikes(strikes);
                if (strikes >= 3) {
                    usuario.setCooldownHasta(java.time.LocalDateTime.now().plusDays(3));
                }
                usuarioRepo.save(usuario);
                // publicar evento para que otros subsistemas reaccionen
                bus.publish(new StrikeAppliedEvent(usuario.getId(), usuario.getStrikes()));
            }
        }
        // Audit the moderation action
        try { auditLogService.log("ReporteConducta", reporte.getId(), "ResolverReporte", "system", Map.of("estado", estado != null ? estado.name() : null, "resolucion", resolucion)); } catch(Exception ignored){}
    }
}
