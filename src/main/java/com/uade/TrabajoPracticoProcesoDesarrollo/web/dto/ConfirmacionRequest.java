package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.NotNull;

public class ConfirmacionRequest {
    @NotNull(message = "usuarioId es obligatorio")
    public Long usuarioId;
    public boolean confirmado;
}
