package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;

public class PostulacionRequest {
    public Long usuarioId;
    public Rol rolDeseado;
    public String comentario;
}
