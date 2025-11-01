package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Scrim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Juego juego;

    // Nombre opcional (alineado con el otro proyecto)
    private String nombre;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String formato; // 1v1, 5v5

    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;

    private LocalDateTime fechaHora;
    // cantidad de jugadores necesarios total (simplificado)
    private Integer cuposTotal;
    // usar minutos para evitar conversiones de Duration
    private Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    private ScrimEstado estado = ScrimEstado.BUSCANDO;

    @OneToMany(mappedBy = "scrim", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("scrim-postulaciones")
    private List<Postulacion> postulaciones = new ArrayList<>();

    // Campos extra (descripción y fecha de creación)
    private String descripcion;
    private LocalDateTime fechaCreacion;

    // Alias/compatibilidad con TPO-Escrims-main (expuestos como derivados/transient)
    @Transient
    public Integer getMmrMinimo() { return this.rangoMin; }

    @Transient
    public Integer getMmrMaximo() { return this.rangoMax; }

    @Transient
    public String getTipoFormato() { return this.formato; }

    // Campo activo (por defecto true)
    private Boolean activo = Boolean.TRUE;

    // Creador opcional, para compatibilidad con TPO
    @ManyToOne(optional = true)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @PrePersist
    protected void onCreate(){
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = ScrimEstado.BUSCANDO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
    public Integer getRangoMin() { return rangoMin; }
    public void setRangoMin(Integer rangoMin) { this.rangoMin = rangoMin; }
    public Integer getRangoMax() { return rangoMax; }
    public void setRangoMax(Integer rangoMax) { this.rangoMax = rangoMax; }
    public Integer getLatenciaMax() { return latenciaMax; }
    public void setLatenciaMax(Integer latenciaMax) { this.latenciaMax = latenciaMax; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Integer getCuposTotal() { return cuposTotal; }
    public void setCuposTotal(Integer cuposTotal) { this.cuposTotal = cuposTotal; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public ScrimEstado getEstado() { return estado; }
    public void setEstado(ScrimEstado estado) { this.estado = estado; }
    public List<Postulacion> getPostulaciones() { return postulaciones; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    // getters transient definidos arriba
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
}
