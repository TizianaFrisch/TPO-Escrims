package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EstadisticaRequest {
    @NotNull(message = "usuarioId es obligatorio")
    public Long usuarioId;
    public boolean mvp;
    @Min(value = 0, message = "kills debe ser >= 0")
    public Integer kills;
    @Min(value = 0, message = "assists debe ser >= 0")
    public Integer assists;
    public String observaciones;
}
