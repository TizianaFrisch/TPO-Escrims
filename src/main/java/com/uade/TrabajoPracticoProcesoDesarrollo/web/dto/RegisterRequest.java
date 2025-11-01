package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

public class RegisterRequest {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.NotBlank
    public String email;
    @jakarta.validation.constraints.NotBlank
    public String password;
    @jakarta.validation.constraints.NotBlank
    public String region;
    // username ahora es opcional; si no se envía se deriva del email
    public String username;
    // Preferencias de notificación (opcionales)
    public Boolean notifyPush;
    public Boolean notifyEmail;
    public Boolean notifyDiscord;
}
