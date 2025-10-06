package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_id", nullable = false)
    private Scrim scrim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_ganador_id")
    private Equipo equipoGanador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_perdedor_id")
    private Equipo equipoPerdedor;
    
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMatch estado = EstadoMatch.EN_PROGRESO;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "mvp_id")
    private Long mvpId; // ID del jugador MVP
    
    // Estadísticas del match
    @Column(name = "kills_equipo_ganador")
    private Integer killsEquipoGanador;
    
    @Column(name = "kills_equipo_perdedor")
    private Integer killsEquipoPerdedor;
    
    @Column(name = "torre_destruidas_ganador")
    private Integer torresDestruidasGanador;
    
    @Column(name = "torre_destruidas_perdedor")
    private Integer torresDestruidasPerdedor;
    
    @Column(name = "gold_diferencia")
    private Integer goldDiferencia;
    
    // Relaciones
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EstadisticaJugadorMatch> estadisticasJugadores = new ArrayList<>();
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventoMatch> eventos = new ArrayList<>();
    
    public enum EstadoMatch {
        EN_PROGRESO,
        FINALIZADO,
        ABANDONADO,
        CANCELADO_POR_MODERADOR
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }
    
    // Métodos de negocio
    public void finalizarMatch(Equipo ganador, Equipo perdedor) {
        this.equipoGanador = ganador;
        this.equipoPerdedor = perdedor;
        this.fechaFin = LocalDateTime.now();
        this.estado = EstadoMatch.FINALIZADO;
        
        if (fechaInicio != null) {
            this.duracionMinutos = (int) java.time.Duration.between(fechaInicio, fechaFin).toMinutes();
        }
    }
    
    public void abandonarMatch(String motivo) {
        this.estado = EstadoMatch.ABANDONADO;
        this.fechaFin = LocalDateTime.now();
        this.observaciones = "Match abandonado: " + motivo;
        
        if (fechaInicio != null) {
            this.duracionMinutos = (int) java.time.Duration.between(fechaInicio, fechaFin).toMinutes();
        }
    }
    
    public boolean estaEnProgreso() {
        return estado == EstadoMatch.EN_PROGRESO;
    }
    
    public boolean haFinalizado() {
        return estado == EstadoMatch.FINALIZADO || 
               estado == EstadoMatch.ABANDONADO || 
               estado == EstadoMatch.CANCELADO_POR_MODERADOR;
    }
    
    public Double getProporcionKills() {
        if (killsEquipoGanador == null || killsEquipoPerdedor == null) return null;
        int totalKills = killsEquipoGanador + killsEquipoPerdedor;
        return totalKills > 0 ? (double) killsEquipoGanador / totalKills : 0.0;
    }
    
    public boolean fueMatchCerrado() {
        return duracionMinutos != null && duracionMinutos < 25; // Match corto indica dominancia
    }
}