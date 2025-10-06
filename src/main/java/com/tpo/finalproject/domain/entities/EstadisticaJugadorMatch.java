package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "estadisticas_jugador_match")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaJugadorMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "rol_jugado")
    private String rolJugado; // TOP, JUNGLE, MID, ADC, SUPPORT
    
    @Column(name = "campeon_usado")
    private String campeonUsado;
    
    // Estadísticas básicas
    @Column(name = "kills")
    @Builder.Default
    private Integer kills = 0;
    
    @Column(name = "deaths")
    @Builder.Default
    private Integer deaths = 0;
    
    @Column(name = "assists")
    @Builder.Default
    private Integer assists = 0;
    
    @Column(name = "cs_total")
    @Builder.Default
    private Integer csTotal = 0; // Creep Score
    
    @Column(name = "gold_ganado")
    @Builder.Default
    private Integer goldGanado = 0;
    
    @Column(name = "damage_total")
    @Builder.Default
    private Integer damageTotal = 0;
    
    @Column(name = "damage_a_campeones")
    @Builder.Default
    private Integer damageACampeones = 0;
    
    @Column(name = "vision_score")
    @Builder.Default
    private Integer visionScore = 0;
    
    // Estadísticas avanzadas
    @Column(name = "primer_sangre")
    @Builder.Default
    private Boolean primerSangre = false;
    
    @Column(name = "torres_destruidas")
    @Builder.Default
    private Integer torresDestruidas = 0;
    
    @Column(name = "dragones_asegurados")
    @Builder.Default
    private Integer dragonesAsegurados = 0;
    
    @Column(name = "baronés_asegurados")
    @Builder.Default
    private Integer baronesAsegurados = 0;
    
    // Performance scores
    @Column(name = "kda")
    private Double kda;
    
    @Column(name = "participacion_kills")
    private Double participacionKills; // (Kills + Assists) / Total Team Kills
    
    @Column(name = "cs_por_minuto")
    private Double csPorMinuto;
    
    @Column(name = "gold_por_minuto")
    private Double goldPorMinuto;
    
    @Column(name = "performance_score")
    private Double performanceScore; // Score calculado basado en todas las métricas
    
    // Métodos de cálculo
    @PostLoad
    @PostPersist
    @PostUpdate
    private void calcularMetricas() {
        calcularKDA();
        calcularRatios();
        calcularPerformanceScore();
    }
    
    private void calcularKDA() {
        if (deaths == null || deaths == 0) {
            this.kda = (double) ((kills != null ? kills : 0) + (assists != null ? assists : 0));
        } else {
            this.kda = ((double) (kills + assists)) / deaths;
        }
    }
    
    private void calcularRatios() {
        if (match != null && match.getDuracionMinutos() != null && match.getDuracionMinutos() > 0) {
            if (csTotal != null) {
                this.csPorMinuto = (double) csTotal / match.getDuracionMinutos();
            }
            if (goldGanado != null) {
                this.goldPorMinuto = (double) goldGanado / match.getDuracionMinutos();
            }
        }
    }
    
    private void calcularPerformanceScore() {
        // Algoritmo de scoring basado en múltiples métricas
        double score = 0.0;
        
        // Base KDA (max 40 puntos)
        if (kda != null) {
            score += Math.min(kda * 10, 40);
        }
        
        // CS por minuto (max 20 puntos)
        if (csPorMinuto != null) {
            score += Math.min(csPorMinuto * 2, 20);
        }
        
        // Vision score (max 15 puntos)
        if (visionScore != null) {
            score += Math.min(visionScore * 0.5, 15);
        }
        
        // Damage (max 15 puntos)
        if (damageACampeones != null) {
            score += Math.min(damageACampeones / 1000.0, 15);
        }
        
        // Objetivos (max 10 puntos)
        int objetivos = (torresDestruidas != null ? torresDestruidas : 0) * 2 +
                       (dragonesAsegurados != null ? dragonesAsegurados : 0) * 3 +
                       (baronesAsegurados != null ? baronesAsegurados : 0) * 5;
        score += Math.min(objetivos, 10);
        
        // Bonus por primer sangre
        if (primerSangre != null && primerSangre) {
            score += 5;
        }
        
        this.performanceScore = Math.round(score * 100.0) / 100.0; // Redondear a 2 decimales
    }
    
    public String getKDAString() {
        return String.format("%d/%d/%d", 
            kills != null ? kills : 0, 
            deaths != null ? deaths : 0, 
            assists != null ? assists : 0);
    }
    
    public boolean fueJugadorDestacado() {
        return performanceScore != null && performanceScore >= 80.0;
    }
}