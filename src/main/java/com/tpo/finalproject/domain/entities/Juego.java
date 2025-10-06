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
@Table(name = "juegos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Juego {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nombre;
    
    @Column(nullable = false)
    private String version;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "desarrollador")
    private String desarrollador;
    
    @Column(name = "genero")
    private String genero;
    
    @Column(name = "max_jugadores")
    private Integer maxJugadores; // 10 para LoL (5v5)
    
    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "fecha_lanzamiento")
    private LocalDateTime fechaLanzamiento;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    // Configuraciones específicas del juego
    @Column(name = "roles_disponibles", columnDefinition = "TEXT")
    private String rolesDisponibles; // JSON string: ["TOP","JUNGLE","MID","ADC","SUPPORT"]
    
    @Column(name = "regiones_soportadas", columnDefinition = "TEXT") 
    private String regionesSoportadas; // JSON string: ["LAS","LAN","NA","EUW","EUNE","KR","JP"]
    
    @Column(name = "mmr_minimo")
    @Builder.Default
    private Integer mmrMinimo = 0;
    
    @Column(name = "mmr_maximo")
    @Builder.Default
    private Integer mmrMaximo = 5000;
    
    // Relaciones
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Scrim> scrims = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
    
    // Métodos de negocio
    public boolean soportaRegion(String region) {
        if (regionesSoportadas == null) return false;
        return regionesSoportadas.contains(region);
    }
    
    public boolean esRolValido(String rol) {
        if (rolesDisponibles == null) return false;
        return rolesDisponibles.contains(rol);
    }
    
    public boolean esMmrValido(Integer mmr) {
        if (mmr == null) return false;
        return mmr >= mmrMinimo && mmr <= mmrMaximo;
    }
    
    public int getJugadoresPorEquipo() {
        return maxJugadores / 2; // 5 para LoL
    }
}