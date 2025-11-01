package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;

import java.time.LocalDateTime;

public class AuditLogDTO {
    public Long id;
    public String entidad;
    public Long entidadId;
    public String accion;
    public String usuario;
    public LocalDateTime timestamp;
    public JsonNode detalles;

    public static AuditLogDTO from(LogAuditoria l, ObjectMapper om){
        var d = new AuditLogDTO();
        d.id = l.getId();
        d.entidad = l.getEntidad();
        d.entidadId = l.getEntidadId();
        d.accion = l.getAccion();
        d.usuario = l.getUsuario();
        d.timestamp = l.getTimestamp();
        try{
            if (l.getDetalles() == null) d.detalles = NullNode.getInstance();
            else d.detalles = om.readTree(l.getDetalles());
        } catch(Exception ex){
            // If parsing fails, return the raw string as a JSON string node
            try{ d.detalles = om.readTree("\"" + l.getDetalles().replace("\"","\\\"") + "\""); }catch(Exception e){ d.detalles = NullNode.getInstance(); }
        }
        return d;
    }
}
