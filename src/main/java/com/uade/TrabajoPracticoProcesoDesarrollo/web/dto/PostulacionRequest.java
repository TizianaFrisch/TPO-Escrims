package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;
import jakarta.validation.constraints.*;

public class PostulacionRequest {
    @NotNull(message = "usuarioId es obligatorio")
    public Long usuarioId;
    @NotNull(message = "rolDeseado es obligatorio")
    public Rol rolDeseado;
    @Size(max = 200, message = "comentario debe tener hasta 200 caracteres")
    public String comentario;
}
