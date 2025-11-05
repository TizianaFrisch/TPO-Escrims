package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

public class RegisterRequest {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Email
    public String email;
    @jakarta.validation.constraints.NotBlank
    public String password;
    @jakarta.validation.constraints.NotBlank
    public String region;
    // username es obligatorio y debe ser único
    @jakarta.validation.constraints.NotBlank
    public String username;
    // Preferencias de notificación (opcionales)
    public Boolean notifyPush;
    public Boolean notifyEmail;
    public Boolean notifyDiscord;
}
