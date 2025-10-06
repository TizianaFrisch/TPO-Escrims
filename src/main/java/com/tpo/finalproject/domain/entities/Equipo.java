package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_id", nullable = false)
    private Scrim scrim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitan_id", nullable = false)
    private Usuario capitan;
    
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MiembroEquipo> miembros = new ArrayList<>();
    
    @Column(name = "lado")
    private String lado; // "AZUL" o "ROJO"
    
    @Column(name = "promedio_mmr")
    private Double promedioMMR;
    
    public boolean estaCompleto() {
        return miembros.size() == 5;
    }
    
    public void calcularPromedioMMR() {
        if (miembros.isEmpty()) {
            this.promedioMMR = 0.0;
            return;
        }
        
        double suma = miembros.stream()
            .mapToInt(m -> m.getUsuario().getMmr() != null ? m.getUsuario().getMmr() : 0)
            .average()
            .orElse(0.0);
        
        this.promedioMMR = suma;
    }
    
    public boolean puedeAgregarMiembro() {
        return miembros.size() < 5;
    }
}