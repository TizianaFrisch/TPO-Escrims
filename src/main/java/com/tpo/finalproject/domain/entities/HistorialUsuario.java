package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialUsuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "tipo_evento")
    private String tipoEvento; // SCRIM_COMPLETADO, SCRIM_CANCELADO, etc.
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;
    
    @Column(name = "mmr_antes")
    private Integer mmrAntes;
    
    @Column(name = "mmr_despues")
    private Integer mmrDespues;
    
    @PrePersist
    protected void onCreate() {
        fechaEvento = LocalDateTime.now();
    }
}