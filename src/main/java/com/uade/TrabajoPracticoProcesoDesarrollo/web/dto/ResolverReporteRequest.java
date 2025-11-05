package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;

public class ResolverReporteRequest {
    public EstadoReporte estado; // APROBADO o RECHAZADO
    public String sancion; // opcional
}
