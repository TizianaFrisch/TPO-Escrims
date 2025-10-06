package com.tpo.finalproject.controller;

import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Postulacion;
import com.tpo.finalproject.service.ScrimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scrims")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScrimController {
    
    private final ScrimService scrimService;
    
    @PostMapping
    public ResponseEntity<?> crearScrim(@RequestBody CrearScrimRequest request) {
        try {
            Scrim scrim = scrimService.crearScrim(
                    request.getCreadorId(),
                    request.getNombre(),
                    request.getDescripcion(),
                    request.getMmrMinimo(),
                    request.getMmrMaximo(),
                    request.getRegion(),
                    request.getFechaHora()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ScrimResponse("Scrim creado exitosamente", scrim));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear scrim"));
        }
    }
    
    @PostMapping("/{scrimId}/postular")
    public ResponseEntity<?> postularseAScrim(
            @PathVariable Long scrimId,
            @RequestBody PostulacionRequest request) {
        try {
            scrimService.postularseAScrim(
                    request.getUsuarioId(),
                    scrimId,
                    request.getRolSolicitado(),
                    request.getComentario()
            );
            
            return ResponseEntity.ok(new MessageResponse("Postulación enviada exitosamente"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al procesar postulación"));
        }
    }
    
    @PutMapping("/postulaciones/{postulacionId}/aceptar")
    public ResponseEntity<?> aceptarPostulacion(
            @PathVariable Long postulacionId,
            @RequestParam Long creadorId) {
        try {
            scrimService.aceptarPostulacion(postulacionId, creadorId);
            return ResponseEntity.ok(new MessageResponse("Postulación aceptada"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al aceptar postulación"));
        }
    }
    
    @PutMapping("/postulaciones/{postulacionId}/rechazar")
    public ResponseEntity<?> rechazarPostulacion(
            @PathVariable Long postulacionId,
            @RequestParam Long creadorId) {
        try {
            scrimService.rechazarPostulacion(postulacionId, creadorId);
            return ResponseEntity.ok(new MessageResponse("Postulación rechazada"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al rechazar postulación"));
        }
    }
    
    @PutMapping("/{scrimId}/estado")
    public ResponseEntity<?> cambiarEstadoScrim(
            @PathVariable Long scrimId,
            @RequestBody CambiarEstadoRequest request) {
        try {
            scrimService.cambiarEstadoScrim(scrimId, request.getNuevoEstado());
            return ResponseEntity.ok(new MessageResponse("Estado del scrim actualizado"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al cambiar estado"));
        }
    }
    
    @PostMapping("/{scrimId}/formar-equipos")
    public ResponseEntity<?> formarEquiposAutomaticamente(@PathVariable Long scrimId) {
        try {
            scrimService.formarEquiposAutomaticamente(scrimId);
            return ResponseEntity.ok(new MessageResponse("Equipos formados exitosamente"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al formar equipos"));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Scrim>> obtenerScrimsDisponibles(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer mmr) {
        try {
            List<Scrim> scrims = scrimService.obtenerScrimsDisponibles(region, mmr);
            return ResponseEntity.ok(scrims);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{scrimId}")
    public ResponseEntity<?> obtenerScrimPorId(@PathVariable Long scrimId) {
        try {
            Optional<Scrim> scrim = scrimService.obtenerScrimPorId(scrimId);
            
            if (scrim.isPresent()) {
                return ResponseEntity.ok(scrim.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/postulaciones/pendientes")
    public ResponseEntity<List<Postulacion>> obtenerPostulacionesPendientes(
            @RequestParam Long creadorId) {
        try {
            List<Postulacion> postulaciones = scrimService.obtenerPostulacionesPendientes(creadorId);
            return ResponseEntity.ok(postulaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DTOs
    
    public static class CrearScrimRequest {
        private Long creadorId;
        private String nombre;
        private String descripcion;
        private Integer mmrMinimo;
        private Integer mmrMaximo;
        private String region;
        private LocalDateTime fechaHora;
        
        // Getters y setters
        public Long getCreadorId() { return creadorId; }
        public void setCreadorId(Long creadorId) { this.creadorId = creadorId; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getMmrMinimo() { return mmrMinimo; }
        public void setMmrMinimo(Integer mmrMinimo) { this.mmrMinimo = mmrMinimo; }
        public Integer getMmrMaximo() { return mmrMaximo; }
        public void setMmrMaximo(Integer mmrMaximo) { this.mmrMaximo = mmrMaximo; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public LocalDateTime getFechaHora() { return fechaHora; }
        public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    }
    
    public static class PostulacionRequest {
        private Long usuarioId;
        private String rolSolicitado;
        private String comentario;
        
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getRolSolicitado() { return rolSolicitado; }
        public void setRolSolicitado(String rolSolicitado) { this.rolSolicitado = rolSolicitado; }
        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }
    }
    
    public static class CambiarEstadoRequest {
        private Scrim.EstadoScrim nuevoEstado;
        
        public Scrim.EstadoScrim getNuevoEstado() { return nuevoEstado; }
        public void setNuevoEstado(Scrim.EstadoScrim nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    }
    
    public static class ScrimResponse {
        private String mensaje;
        private Scrim scrim;
        
        public ScrimResponse(String mensaje, Scrim scrim) {
            this.mensaje = mensaje;
            this.scrim = scrim;
        }
        
        public String getMensaje() { return mensaje; }
        public Scrim getScrim() { return scrim; }
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