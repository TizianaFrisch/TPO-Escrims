package com.tpo.finalproject.domain.builders;

import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.Juego;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Builder específico para Scrim con validaciones complejas
 * y interface fluent para crear Scrims de manera controlada
 */
public class ScrimBuilder {
    
    private String nombre;
    private String descripcion;
    private Integer mmrMinimo;
    private Integer mmrMaximo;
    private String region;
    private LocalDateTime fechaHora;
    private Usuario creador;
    private Juego juego;
    private Scrim.EstadoScrim estado = Scrim.EstadoScrim.BUSCANDO_JUGADORES;
    private Boolean activo = true;
    
    // Lista para acumular errores de validación
    private List<String> erroresValidacion = new ArrayList<>();
    
    public static ScrimBuilder crear() {
        return new ScrimBuilder();
    }
    
    public ScrimBuilder conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public ScrimBuilder conDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }
    
    public ScrimBuilder conRangoMMR(Integer mmrMinimo, Integer mmrMaximo) {
        this.mmrMinimo = mmrMinimo;
        this.mmrMaximo = mmrMaximo;
        return this;
    }
    
    public ScrimBuilder enRegion(String region) {
        this.region = region;
        return this;
    }
    
    public ScrimBuilder programadoPara(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        return this;
    }
    
    public ScrimBuilder creadoPor(Usuario creador) {
        this.creador = creador;
        return this;
    }
    
    public ScrimBuilder paraJuego(Juego juego) {
        this.juego = juego;
        return this;
    }
    
    public ScrimBuilder conEstado(Scrim.EstadoScrim estado) {
        this.estado = estado;
        return this;
    }
    
    public ScrimBuilder activo(Boolean activo) {
        this.activo = activo;
        return this;
    }
    
    /**
     * Validaciones específicas de negocio para Scrims
     */
    private void validar() {
        erroresValidacion.clear();
        
        // Validar nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            erroresValidacion.add("El nombre del scrim es obligatorio");
        } else if (nombre.length() < 5 || nombre.length() > 100) {
            erroresValidacion.add("El nombre debe tener entre 5 y 100 caracteres");
        }
        
        // Validar creador
        if (creador == null) {
            erroresValidacion.add("El creador del scrim es obligatorio");
        } else if (!creador.getActivo()) {
            erroresValidacion.add("El creador debe estar activo");
        }
        
        // Validar juego
        if (juego == null) {
            erroresValidacion.add("El juego es obligatorio");
        } else if (!juego.getActivo()) {
            erroresValidacion.add("El juego debe estar activo");
        }
        
        // Validar MMR
        if (mmrMinimo == null || mmrMaximo == null) {
            erroresValidacion.add("Los rangos de MMR son obligatorios");
        } else {
            if (mmrMinimo < 0 || mmrMaximo < 0) {
                erroresValidacion.add("El MMR no puede ser negativo");
            }
            if (mmrMinimo > mmrMaximo) {
                erroresValidacion.add("El MMR mínimo no puede ser mayor al máximo");
            }
            if (juego != null && !juego.esMmrValido(mmrMinimo)) {
                erroresValidacion.add("El MMR mínimo está fuera del rango válido para " + juego.getNombre());
            }
            if (juego != null && !juego.esMmrValido(mmrMaximo)) {
                erroresValidacion.add("El MMR máximo está fuera del rango válido para " + juego.getNombre());
            }
        }
        
        // Validar región
        if (region == null || region.trim().isEmpty()) {
            erroresValidacion.add("La región es obligatoria");
        } else if (juego != null && !juego.soportaRegion(region)) {
            erroresValidacion.add("La región " + region + " no es soportada por " + juego.getNombre());
        }
        
        // Validar fecha
        if (fechaHora == null) {
            erroresValidacion.add("La fecha y hora son obligatorias");
        } else if (fechaHora.isBefore(LocalDateTime.now())) {
            erroresValidacion.add("La fecha del scrim no puede ser en el pasado");
        } else if (fechaHora.isAfter(LocalDateTime.now().plusDays(30))) {
            erroresValidacion.add("No se pueden crear scrims con más de 30 días de anticipación");
        }
        
        // Validar compatibilidad creador-juego
        if (creador != null && juego != null) {
            if (!juego.soportaRegion(creador.getRegion())) {
                erroresValidacion.add("La región del creador no es compatible con el juego seleccionado");
            }
            if (!juego.esMmrValido(creador.getMmr())) {
                erroresValidacion.add("El MMR del creador no está en el rango válido del juego");
            }
        }
    }
    
    /**
     * Construye el Scrim después de validar todos los campos
     */
    public Scrim build() {
        validar();
        
        if (!erroresValidacion.isEmpty()) {
            throw new IllegalArgumentException("Errores de validación: " + String.join(", ", erroresValidacion));
        }
        
        return Scrim.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .mmrMinimo(mmrMinimo)
                .mmrMaximo(mmrMaximo)
                .region(region)
                .fechaHora(fechaHora)
                .creador(creador)
                .juego(juego)
                .estado(estado)
                .activo(activo)
                .build();
    }
    
    /**
     * Construye sin validaciones (para casos especiales como testing)
     */
    public Scrim buildSinValidacion() {
        return Scrim.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .mmrMinimo(mmrMinimo)
                .mmrMaximo(mmrMaximo)
                .region(region)
                .fechaHora(fechaHora)
                .creador(creador)
                .juego(juego)
                .estado(estado)
                .activo(activo)
                .build();
    }
    
    /**
     * Retorna errores de validación sin lanzar excepción
     */
    public List<String> validarSinExcepcion() {
        validar();
        return new ArrayList<>(erroresValidacion);
    }
    
    /**
     * Verifica si el builder está listo para construir
     */
    public boolean esValido() {
        validar();
        return erroresValidacion.isEmpty();
    }
}