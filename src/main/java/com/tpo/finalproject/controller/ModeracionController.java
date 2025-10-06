package com.tpo.finalproject.controller;

import com.tpo.finalproject.domain.entities.Reporte;
import com.tpo.finalproject.service.ModeracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/moderacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModeracionController {
    
    private final ModeracionService moderacionService;
    
    @PostMapping("/reportes")
    public ResponseEntity<?> crearReporte(@RequestBody CrearReporteRequest request) {
        try {
            Reporte reporte = moderacionService.crearReporte(
                    request.getReportadorId(),
                    request.getReportadoId(),
                    request.getMotivo(),
                    request.getDescripcion()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ReporteResponse("Reporte creado exitosamente", reporte.getId()));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear reporte"));
        }
    }
    
    @PutMapping("/reportes/{reporteId}/resolver")
    public ResponseEntity<?> resolverReporte(
            @PathVariable Long reporteId,
            @RequestBody ResolverReporteRequest request) {
        try {
            moderacionService.resolverReporteManualmente(
                    reporteId,
                    request.getModeradorId(),
                    request.getAccion()
            );
            
            return ResponseEntity.ok(new MessageResponse("Reporte resuelto exitosamente"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al resolver reporte"));
        }
    }
    
    @GetMapping("/reportes/pendientes")
    public ResponseEntity<List<Reporte>> obtenerReportesPendientes() {
        try {
            List<Reporte> reportes = moderacionService.obtenerReportesPendientes();
            return ResponseEntity.ok(reportes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/reportes/moderador/{moderadorId}")
    public ResponseEntity<List<Reporte>> obtenerReportesParaModerador(@PathVariable Long moderadorId) {
        try {
            List<Reporte> reportes = moderacionService.obtenerReportesParaModerador(moderadorId);
            return ResponseEntity.ok(reportes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DTOs
    
    public static class CrearReporteRequest {
        private Long reportadorId;
        private Long reportadoId;
        private String motivo;
        private String descripcion;
        
        public Long getReportadorId() { return reportadorId; }
        public void setReportadorId(Long reportadorId) { this.reportadorId = reportadorId; }
        public Long getReportadoId() { return reportadoId; }
        public void setReportadoId(Long reportadoId) { this.reportadoId = reportadoId; }
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }
    
    public static class ResolverReporteRequest {
        private Long moderadorId;
        private String accion;
        
        public Long getModeradorId() { return moderadorId; }
        public void setModeradorId(Long moderadorId) { this.moderadorId = moderadorId; }
        public String getAccion() { return accion; }
        public void setAccion(String accion) { this.accion = accion; }
    }
    
    public static class ReporteResponse {
        private String mensaje;
        private Long reporteId;
        
        public ReporteResponse(String mensaje, Long reporteId) {
            this.mensaje = mensaje;
            this.reporteId = reporteId;
        }
        
        public String getMensaje() { return mensaje; }
        public Long getReporteId() { return reporteId; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
    
    public static class MessageResponse {
        private String mensaje;
        
        public MessageResponse(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() { return mensaje; }
    }
}