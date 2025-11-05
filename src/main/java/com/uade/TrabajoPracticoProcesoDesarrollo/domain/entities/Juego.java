package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Juego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // Campos opcionales alineados con el otro proyecto (no obligatorios en nuestros endpoints)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String desarrollador;
    private String genero;
    private Integer maxJugadores;
    private Boolean activo = Boolean.TRUE;
    private LocalDateTime fechaLanzamiento;
    private LocalDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String rolesDisponibles; // p.ej. JSON string

    @Column(columnDefinition = "TEXT")
    private String regionesSoportadas; // p.ej. JSON string

    private Integer mmrMinimo = 0;
    private Integer mmrMaximo = 5000;

    @PrePersist
    protected void onCreate(){
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDesarrollador() { return desarrollador; }
    public void setDesarrollador(String desarrollador) { this.desarrollador = desarrollador; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public Integer getMaxJugadores() { return maxJugadores; }
    public void setMaxJugadores(Integer maxJugadores) { this.maxJugadores = maxJugadores; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaLanzamiento() { return fechaLanzamiento; }
    public void setFechaLanzamiento(LocalDateTime fechaLanzamiento) { this.fechaLanzamiento = fechaLanzamiento; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getRolesDisponibles() { return rolesDisponibles; }
    public void setRolesDisponibles(String rolesDisponibles) { this.rolesDisponibles = rolesDisponibles; }
    public String getRegionesSoportadas() { return regionesSoportadas; }
    public void setRegionesSoportadas(String regionesSoportadas) { this.regionesSoportadas = regionesSoportadas; }
    public Integer getMmrMinimo() { return mmrMinimo; }
    public void setMmrMinimo(Integer mmrMinimo) { this.mmrMinimo = mmrMinimo; }
    public Integer getMmrMaximo() { return mmrMaximo; }
    public void setMmrMaximo(Integer mmrMaximo) { this.mmrMaximo = mmrMaximo; }
}
