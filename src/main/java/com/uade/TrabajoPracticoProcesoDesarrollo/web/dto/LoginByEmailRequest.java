package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginByEmailRequest {
    @NotBlank @Email
    public String email;
    @NotBlank
    public String password;
}
