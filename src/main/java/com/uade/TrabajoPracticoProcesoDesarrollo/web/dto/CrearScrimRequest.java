package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public class CrearScrimRequest {
    @NotNull(message = "juegoId es obligatorio")
    public Long juegoId;
    // Opcional: si se envía, se asociará como creador del scrim y se habilitarán notificaciones al creador
    public Long creadorId;
    @NotBlank(message = "region es obligatoria")
    public String region;
    @NotBlank(message = "formato es obligatorio") // "1v1", "5v5"
    public String formato;
    public Integer cuposTotal;
    @Min(value = 0, message = "rangoMin debe ser >= 0")
    public Integer rangoMin;
    @Min(value = 0, message = "rangoMax debe ser >= 0")
    public Integer rangoMax;
    @Min(value = 0, message = "latenciaMax debe ser >= 0")
    public Integer latenciaMax;
    public LocalDateTime fechaHora;
    @Min(value = 1, message = "duracionMinutos debe ser >= 1")
    public Integer duracionMinutos;
}
