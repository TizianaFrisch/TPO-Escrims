package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.NotNull;

public class BusquedaFavoritaDTO {
    @NotNull private Long usuarioId;
    private String juego;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;
    private Boolean alertasActivas = true;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getJuego() { return juego; }
    public void setJuego(String juego) { this.juego = juego; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Integer getRangoMin() { return rangoMin; }
    public void setRangoMin(Integer rangoMin) { this.rangoMin = rangoMin; }

    public Integer getRangoMax() { return rangoMax; }
    public void setRangoMax(Integer rangoMax) { this.rangoMax = rangoMax; }

    public Integer getLatenciaMax() { return latenciaMax; }
    public void setLatenciaMax(Integer latenciaMax) { this.latenciaMax = latenciaMax; }

    public Boolean getAlertasActivas() { return alertasActivas; }
    public void setAlertasActivas(Boolean alertasActivas) { this.alertasActivas = alertasActivas; }
}
