package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import jakarta.validation.constraints.*;

public class FeedbackRequest {
    @NotNull(message = "autorId es obligatorio")
    public Long autorId;
    @NotNull(message = "rating es obligatorio")
    @Min(value = 1) @Max(value = 5)
    public Integer rating;
    @Size(max = 200)
    public String comentario;
}
