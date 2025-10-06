package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postulacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_id", nullable = false)
    private Scrim scrim;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPostulacion estado;
    
    @Column(name = "rol_solicitado")
    private String rolSolicitado;
    
    @Column(name = "fecha_postulacion")
    private LocalDateTime fechaPostulacion;
    
    @Column(name = "comentario")
    private String comentario;
    
    public enum EstadoPostulacion {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA,
        CANCELADA
    }
    
    @PrePersist
    protected void onCreate() {
        fechaPostulacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPostulacion.PENDIENTE;
        }
    }
    
    public boolean puedeSerAceptada() {
        return estado == EstadoPostulacion.PENDIENTE;
    }
    
    public void aceptar() {
        if (puedeSerAceptada()) {
            this.estado = EstadoPostulacion.ACEPTADA;
        }
    }
    
    public void rechazar() {
        if (puedeSerAceptada()) {
            this.estado = EstadoPostulacion.RECHAZADA;
        }
    }
}