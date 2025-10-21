package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;

public record LoginResponse(Long id, String username, VerificacionEstado verificacionEstado) {}
