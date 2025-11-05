package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import java.util.List;

public class PerfilUsuarioDTO {
    private Long juegoPrincipalId;
    private List<String> rolesPreferidos;
    private String disponibilidadHoraria;
    private String region;

    // Getters y setters
    public Long getJuegoPrincipalId() { return juegoPrincipalId; }
    public void setJuegoPrincipalId(Long juegoPrincipalId) { this.juegoPrincipalId = juegoPrincipalId; }
    public List<String> getRolesPreferidos() { return rolesPreferidos; }
    public void setRolesPreferidos(List<String> rolesPreferidos) { this.rolesPreferidos = rolesPreferidos; }
    public String getDisponibilidadHoraria() { return disponibilidadHoraria; }
    public void setDisponibilidadHoraria(String disponibilidadHoraria) { this.disponibilidadHoraria = disponibilidadHoraria; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
