package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import java.time.LocalDateTime;

public class CrearScrimRequest {
    public Long juegoId;
    public String region;
    public String formato; // "1v1", "5v5"
    public Integer cuposTotal;
    public Integer rangoMin;
    public Integer rangoMax;
    public Integer latenciaMax;
    public LocalDateTime fechaHora;
    public Integer duracionMinutos;
}
