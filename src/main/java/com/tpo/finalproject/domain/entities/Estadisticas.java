package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "estadisticas_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estadisticas {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
    
    // Estadísticas generales
    @Column(name = "partidas_jugadas")
    @Builder.Default
    private Integer partidasJugadas = 0;
    
    @Column(name = "partidas_ganadas")
    @Builder.Default
    private Integer partidasGanadas = 0;
    
    @Column(name = "partidas_perdidas")
    @Builder.Default
    private Integer partidasPerdidas = 0;
    
    @Column(name = "winrate")
    private Double winrate;
    
    @Column(name = "mmr_actual")
    private Integer mmrActual;
    
    @Column(name = "mmr_maximo")
    private Integer mmrMaximo;
    
    @Column(name = "mmr_promedio")
    private Double mmrPromedio;
    
    // Estadísticas por rol
    @Column(name = "partidas_top")
    @Builder.Default
    private Integer partidasTop = 0;
    
    @Column(name = "partidas_jungle")
    @Builder.Default
    private Integer partidasJungle = 0;
    
    @Column(name = "partidas_mid")
    @Builder.Default
    private Integer partidasMid = 0;
    
    @Column(name = "partidas_adc")
    @Builder.Default
    private Integer partidasAdc = 0;
    
    @Column(name = "partidas_support")
    @Builder.Default
    private Integer partidasSupport = 0;
    
    // Winrates por rol
    @Column(name = "winrate_top")
    private Double winrateTop;
    
    @Column(name = "winrate_jungle")
    private Double winrateJungle;
    
    @Column(name = "winrate_mid")
    private Double winrateMid;
    
    @Column(name = "winrate_adc")
    private Double winrateAdc;
    
    @Column(name = "winrate_support")
    private Double winrateSupport;
    
    // Estadísticas de performance
    @Column(name = "kda_promedio")
    private Double kdaPromedio;
    
    @Column(name = "kills_promedio")
    private Double killsPromedio;
    
    @Column(name = "deaths_promedio")
    private Double deathsPromedio;
    
    @Column(name = "assists_promedio")
    private Double assistsPromedio;
    
    @Column(name = "cs_promedio")
    private Double csPromedio;
    
    @Column(name = "vision_score_promedio")
    private Double visionScorePromedio;
    
    @Column(name = "damage_promedio")
    private Double damagePromedio;
    
    @Column(name = "gold_promedio")
    private Double goldPromedio;
    
    // Performance score general
    @Column(name = "performance_score_promedio")
    private Double performanceScorePromedio;
    
    @Column(name = "performance_score_maximo")
    private Double performanceScoreMaximo;
    
    // Racha actual
    @Column(name = "racha_actual")
    @Builder.Default
    private Integer rachaActual = 0; // Positivo = victorias, Negativo = derrotas
    
    @Column(name = "racha_maxima_victorias")
    @Builder.Default
    private Integer rachaMaximaVictorias = 0;
    
    @Column(name = "racha_maxima_derrotas")
    @Builder.Default
    private Integer rachaMaximaDerrotas = 0;
    
    // Estadísticas de participación
    @Column(name = "scrims_creados")
    @Builder.Default
    private Integer scrimsCreados = 0;
    
    @Column(name = "scrims_completados")
    @Builder.Default
    private Integer scrimsCompletados = 0;
    
    @Column(name = "scrims_abandonados")
    @Builder.Default
    private Integer scrimsAbandonados = 0;
    
    @Column(name = "postulaciones_realizadas")
    @Builder.Default
    private Integer postulacionesRealizadas = 0;
    
    @Column(name = "postulaciones_aceptadas")
    @Builder.Default
    private Integer postulacionesAceptadas = 0;
    
    // Penalizaciones
    @Column(name = "reportes_recibidos")
    @Builder.Default
    private Integer reportesRecibidos = 0;
    
    @Column(name = "warnings_recibidos")
    @Builder.Default
    private Integer warningsRecibidos = 0;
    
    @Column(name = "suspensiones")
    @Builder.Default
    private Integer suspensiones = 0;
    
    // Fechas de tracking
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
        calcularMetricas();
    }
    
    // Métodos de cálculo automático
    private void calcularMetricas() {
        // Calcular winrate general
        if (partidasJugadas != null && partidasJugadas > 0) {
            this.winrate = ((double) partidasGanadas / partidasJugadas) * 100;
        }
        
        // Calcular winrates por rol
        calcularWinratePorRol();
        
        // Actualizar MMR promedio
        if (mmrActual != null) {
            if (mmrPromedio == null) {
                mmrPromedio = (double) mmrActual;
            } else {
                // Promedio ponderado dando más peso a partidas recientes
                mmrPromedio = (mmrPromedio * 0.8) + (mmrActual * 0.2);
            }
        }
        
        // Actualizar MMR máximo
        if (mmrActual != null && (mmrMaximo == null || mmrActual > mmrMaximo)) {
            mmrMaximo = mmrActual;
        }
    }
    
    private void calcularWinratePorRol() {
        // Nota: Para calcular winrates por rol necesitaríamos tracking adicional
        // de victorias por rol, que requeriría modificar la lógica de actualización
    }
    
    // Métodos de actualización desde matches
    public void actualizarConMatch(EstadisticaJugadorMatch stats, boolean gano) {
        // Actualizar contadores básicos
        partidasJugadas++;
        if (gano) {
            partidasGanadas++;
            if (rachaActual < 0) rachaActual = 1;
            else rachaActual++;
            
            if (rachaActual > rachaMaximaVictorias) {
                rachaMaximaVictorias = rachaActual;
            }
        } else {
            partidasPerdidas++;
            if (rachaActual > 0) rachaActual = -1;
            else rachaActual--;
            
            if (Math.abs(rachaActual) > rachaMaximaDerrotas) {
                rachaMaximaDerrotas = Math.abs(rachaActual);
            }
        }
        
        // Actualizar contadores por rol
        String rol = stats.getRolJugado();
        if (rol != null) {
            switch (rol.toUpperCase()) {
                case "TOP" -> partidasTop++;
                case "JUNGLE" -> partidasJungle++;
                case "MID" -> partidasMid++;
                case "ADC" -> partidasAdc++;
                case "SUPPORT" -> partidasSupport++;
            }
        }
        
        // Actualizar promedios (usando promedio móvil)
        actualizarPromedios(stats);
        
        // Recalcular métricas
        calcularMetricas();
    }
    
    private void actualizarPromedios(EstadisticaJugadorMatch stats) {
        double alpha = 0.1; // Factor de suavizado para promedio móvil
        
        if (stats.getKda() != null) {
            kdaPromedio = kdaPromedio == null ? stats.getKda() : 
                (kdaPromedio * (1 - alpha)) + (stats.getKda() * alpha);
        }
        
        if (stats.getKills() != null) {
            killsPromedio = killsPromedio == null ? stats.getKills().doubleValue() :
                (killsPromedio * (1 - alpha)) + (stats.getKills() * alpha);
        }
        
        if (stats.getDeaths() != null) {
            deathsPromedio = deathsPromedio == null ? stats.getDeaths().doubleValue() :
                (deathsPromedio * (1 - alpha)) + (stats.getDeaths() * alpha);
        }
        
        if (stats.getAssists() != null) {
            assistsPromedio = assistsPromedio == null ? stats.getAssists().doubleValue() :
                (assistsPromedio * (1 - alpha)) + (stats.getAssists() * alpha);
        }
        
        if (stats.getCsTotal() != null) {
            csPromedio = csPromedio == null ? stats.getCsTotal().doubleValue() :
                (csPromedio * (1 - alpha)) + (stats.getCsTotal() * alpha);
        }
        
        if (stats.getPerformanceScore() != null) {
            performanceScorePromedio = performanceScorePromedio == null ? stats.getPerformanceScore() :
                (performanceScorePromedio * (1 - alpha)) + (stats.getPerformanceScore() * alpha);
                
            if (performanceScoreMaximo == null || stats.getPerformanceScore() > performanceScoreMaximo) {
                performanceScoreMaximo = stats.getPerformanceScore();
            }
        }
    }
    
    // Métodos de consulta
    public String getRolMasJugado() {
        int maxPartidas = Math.max(partidasTop,
            Math.max(partidasJungle,
                Math.max(partidasMid,
                    Math.max(partidasAdc, partidasSupport))));
                    
        if (maxPartidas == 0) return "NINGUNO";
        
        if (partidasTop == maxPartidas) return "TOP";
        if (partidasJungle == maxPartidas) return "JUNGLE";
        if (partidasMid == maxPartidas) return "MID";
        if (partidasAdc == maxPartidas) return "ADC";
        return "SUPPORT";
    }
    
    public boolean esJugadorActivo() {
        return ultimaActualizacion != null && 
               ultimaActualizacion.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    public boolean esJugadorExperimentado() {
        return partidasJugadas != null && partidasJugadas >= 20;
    }
    
    public String getNivelJugador() {
        if (partidasJugadas == null || partidasJugadas < 5) return "NOVATO";
        if (partidasJugadas < 20) return "PRINCIPIANTE";
        if (partidasJugadas < 50) return "INTERMEDIO";
        if (partidasJugadas < 100) return "AVANZADO";
        if (partidasJugadas < 200) return "EXPERTO";
        return "VETERANO";
    }
}