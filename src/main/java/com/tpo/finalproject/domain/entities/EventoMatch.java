package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_match")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;
    
    @Column(name = "minuto_juego")
    private Integer minutoJuego;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_principal_id")
    private Usuario jugadorPrincipal; // Jugador que ejecutó el evento
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_objetivo_id")
    private Usuario jugadorObjetivo; // Jugador afectado (en caso de kill)
    
    @Column(name = "equipo_responsable")
    private String equipoResponsable; // "AZUL" o "ROJO"
    
    @Column(name = "coordenada_x")
    private Integer coordenadaX; // Posición en el mapa
    
    @Column(name = "coordenada_y")
    private Integer coordenadaY;
    
    @Column(name = "detalles_adicionales", columnDefinition = "TEXT")
    private String detallesAdicionales; // JSON con información extra
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    public enum TipoEvento {
        // Eventos de kills
        FIRST_BLOOD("Primer Sangre"),
        KILL("Kill"),
        DOUBLE_KILL("Doble Kill"),
        TRIPLE_KILL("Triple Kill"),
        QUADRA_KILL("Quadra Kill"),
        PENTA_KILL("Penta Kill"),
        
        // Eventos de objetivos
        DRAGON_KILL("Dragon Eliminado"),
        BARON_KILL("Baron Eliminado"),
        HERALD_KILL("Herald Eliminado"),
        TOWER_DESTROY("Torre Destruida"),
        INHIBITOR_DESTROY("Inhibidor Destruido"),
        
        // Eventos de juego
        JUNGLE_INVADE("Invasión de Jungla"),
        GANK_SUCCESSFUL("Gank Exitoso"),
        TEAMFIGHT("Teamfight"),
        ACE("Ace - Equipo Eliminado"),
        
        // Eventos administrativos
        PAUSE("Pausa"),
        RESUME("Reanudación"),
        REMAKE("Remake"),
        SURRENDER("Rendición"),
        
        // Eventos especiales
        PENTAKILL_DENIED("Penta Negado"),
        BARON_STEAL("Baron Robado"),
        DRAGON_STEAL("Dragon Robado"),
        BACKDOOR("Backdoor"),
        
        // Eventos de penalizaciones
        DISCONNECT("Desconexión"),
        AFK("AFK"),
        TOXIC_BEHAVIOR("Comportamiento Tóxico");
        
        private final String descripcion;
        
        TipoEvento(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Métodos de negocio
    public boolean esEventoImportante() {
        return switch (tipoEvento) {
            case FIRST_BLOOD, PENTA_KILL, BARON_KILL, ACE, SURRENDER -> true;
            default -> false;
        };
    }
    
    public boolean esEventoDeKill() {
        return switch (tipoEvento) {
            case FIRST_BLOOD, KILL, DOUBLE_KILL, TRIPLE_KILL, QUADRA_KILL, PENTA_KILL -> true;
            default -> false;
        };
    }
    
    public boolean esEventoDeObjetivo() {
        return switch (tipoEvento) {
            case DRAGON_KILL, BARON_KILL, HERALD_KILL, TOWER_DESTROY, INHIBITOR_DESTROY -> true;
            default -> false;
        };
    }
    
    public int getPuntosImportancia() {
        return switch (tipoEvento) {
            case PENTA_KILL -> 50;
            case BARON_KILL -> 30;
            case QUADRA_KILL, ACE -> 25;
            case FIRST_BLOOD, DRAGON_KILL -> 20;
            case TRIPLE_KILL, TOWER_DESTROY -> 15;
            case DOUBLE_KILL, HERALD_KILL -> 10;
            case KILL, INHIBITOR_DESTROY -> 5;
            default -> 1;
        };
    }
    
    public String generarNotificacion() {
        String base = String.format("[%s min] %s", 
            minutoJuego != null ? minutoJuego : "?", 
            tipoEvento.getDescripcion());
            
        if (jugadorPrincipal != null) {
            base += " por " + jugadorPrincipal.getUsername();
        }
        
        if (jugadorObjetivo != null && esEventoDeKill()) {
            base += " sobre " + jugadorObjetivo.getUsername();
        }
        
        return base;
    }
}