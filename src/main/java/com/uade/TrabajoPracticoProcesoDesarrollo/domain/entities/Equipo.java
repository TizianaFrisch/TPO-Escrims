package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id")
    private Scrim scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capitan_id")
    private Usuario capitan;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MiembroEquipo> miembros = new ArrayList<>();

    // "AZUL" o "ROJO" (texto libre por ahora)
    private String lado;

    @Column(name = "promedio_mmr")
    private Double promedioMMR;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Usuario getCapitan() { return capitan; }
    public void setCapitan(Usuario capitan) { this.capitan = capitan; }
    public List<MiembroEquipo> getMiembros() { return miembros; }
    public String getLado() { return lado; }
    public void setLado(String lado) { this.lado = lado; }
    public Double getPromedioMMR() { return promedioMMR; }
    public void setPromedioMMR(Double promedioMMR) { this.promedioMMR = promedioMMR; }
}
