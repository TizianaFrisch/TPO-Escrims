package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommandRequest {
    @NotNull(message = "actorId es obligatorio")
    public Long actorId;
    @NotBlank(message = "payload es obligatorio")
    public String payload;
}
