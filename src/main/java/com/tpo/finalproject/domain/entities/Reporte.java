package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportador_id", nullable = false)
    private Usuario reportador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_id", nullable = false)
    private Usuario reportado;
    
    @Column(name = "motivo")
    private String motivo;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;
    
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;
    
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderador_id")
    private Usuario moderador;
    
    @Column(name = "accion_tomada")
    private String accionTomada;
    
    public enum EstadoReporte {
        PENDIENTE,
        EN_REVISION,
        RESUELTO,
        RECHAZADO
    }
    
    @PrePersist
    protected void onCreate() {
        fechaReporte = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoReporte.PENDIENTE;
        }
    }
    
    public void resolver(Usuario moderador, String accion) {
        this.moderador = moderador;
        this.accionTomada = accion;
        this.estado = EstadoReporte.RESUELTO;
        this.fechaResolucion = LocalDateTime.now();
    }
    
    public void rechazar(Usuario moderador) {
        this.moderador = moderador;
        this.estado = EstadoReporte.RECHAZADO;
        this.fechaResolucion = LocalDateTime.now();
    }
}