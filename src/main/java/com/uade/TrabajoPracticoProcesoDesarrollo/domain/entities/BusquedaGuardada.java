package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;

@Entity
public class BusquedaGuardada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
    private String juego;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;
    private Boolean alertasActivas = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getJuego() {
        return juego;
    }

    public void setJuego(String juego) {
        this.juego = juego;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getRangoMin() {
        return rangoMin;
    }

    public void setRangoMin(Integer rangoMin) {
        this.rangoMin = rangoMin;
    }

    public Integer getRangoMax() {
        return rangoMax;
    }

    public void setRangoMax(Integer rangoMax) {
        this.rangoMax = rangoMax;
    }

    public Integer getLatenciaMax() {
        return latenciaMax;
    }

    public void setLatenciaMax(Integer latenciaMax) {
        this.latenciaMax = latenciaMax;
    }

    public Boolean getAlertasActivas() {
        return alertasActivas;
    }

    public void setAlertasActivas(Boolean alertasActivas) {
        this.alertasActivas = alertasActivas;
    }
}
