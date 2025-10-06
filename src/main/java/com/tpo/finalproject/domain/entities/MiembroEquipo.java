package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "miembros_equipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroEquipo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "rol")
    private String rol; // TOP, JUNGLE, MID, ADC, SUPPORT
    
    @Column(name = "confirmado")
    @Builder.Default
    private Boolean confirmado = false;
}