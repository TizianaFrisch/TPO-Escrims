package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

public class LoginRequest {
    @jakarta.validation.constraints.NotBlank
    public String username;
    @jakarta.validation.constraints.NotBlank
    public String password;
}
