package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(nullable = false)
    private String mensaje;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;
    
    @Column(name = "leida")
    @Builder.Default
    private Boolean leida = false;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
    
    public enum TipoNotificacion {
        POSTULACION_ACEPTADA,
        POSTULACION_RECHAZADA,
        SCRIM_CANCELADO,
        NUEVA_POSTULACION,
        EQUIPO_COMPLETO,
        REPORTE_RESUELTO,
        MATCH_INICIADO,
        MATCH_FINALIZADO,
        EVENTO_MATCH,
        SCRIM_INICIADO,
        JUGADOR_MVP
    }
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
    
    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }
}