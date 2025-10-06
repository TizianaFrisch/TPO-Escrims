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
@Table(name = "scrims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scrim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(name = "mmr_minimo")
    private Integer mmrMinimo;
    
    @Column(name = "mmr_maximo")
    private Integer mmrMaximo;
    
    @Column(nullable = false)
    private String region;
    
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoScrim estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;
    
    @Column(name = "formato")
    private String tipoFormato; // "5v5", "3v3", "1v1"
    
    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Postulacion> postulaciones = new ArrayList<>();
    
    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Equipo> equipos = new ArrayList<>();
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
    
    // Patrón State - Estados del Scrim
    public enum EstadoScrim {
        BUSCANDO_JUGADORES,
        LOBBY_ARMADO,
        EN_PROGRESO,
        FINALIZADO,
        CANCELADO
    }
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoScrim.BUSCANDO_JUGADORES;
        }
    }
    
    // Métodos del patrón State
    public void cambiarEstado(EstadoScrim nuevoEstado) {
        this.estado = nuevoEstado;
    }
    
    public boolean puedeAceptarJugadores() {
        return estado == EstadoScrim.BUSCANDO_JUGADORES && activo;
    }
    
    public boolean cumpleRequisitosMMR(Integer mmrJugador) {
        if (mmrJugador == null) return false;
        return mmrJugador >= mmrMinimo && mmrJugador <= mmrMaximo;
    }
    
    public int getJugadoresNecesarios() {
        // Determinar jugadores necesarios basado en formato
        if ("5v5".equals(tipoFormato)) return 10;
        if ("3v3".equals(tipoFormato)) return 6;
        if ("1v1".equals(tipoFormato)) return 2;
        return 10; // Por defecto 5v5
    }
    
    public int getJugadoresRequeridos() {
        return getJugadoresNecesarios();
    }
}