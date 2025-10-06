package com.tpo.finalproject.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    
    public enum Rol {
        USUARIO,
        MODERADOR,
        ADMINISTRADOR
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "discord_id", unique = true)
    private String discordId;
    
    @Column(name = "summoner_name")
    private String summoner;
    
    @Column(name = "mmr")
    private Integer mmr;
    
    @Column(name = "region")
    private String region;
    
    @Column(name = "rol_preferido")
    private String rolPreferido;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Rol rol = Rol.USUARIO;
    
    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Postulacion> postulaciones = new ArrayList<>();
    
    @OneToMany(mappedBy = "creador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Scrim> scrimsCreados = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HistorialUsuario> historial = new ArrayList<>();
    
    @OneToMany(mappedBy = "reportador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reporte> reportesHechos = new ArrayList<>();
    
    @OneToMany(mappedBy = "reportado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reporte> reportesRecibidos = new ArrayList<>();
    
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Estadisticas estadisticas;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        ultimaConexion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        ultimaConexion = LocalDateTime.now();
    }
    
    // ============== MÉTODOS DE ROLES INTEGRADOS ==============
    // Métodos de conveniencia para verificar roles
    
    public boolean esUsuario() {
        return this.rol == Rol.USUARIO;
    }
    
    public boolean esModerador() {
        return this.rol == Rol.MODERADOR;
    }
    
    public boolean esAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }
    
    public boolean tienePermisosDeModerador() {
        return this.rol == Rol.MODERADOR || this.rol == Rol.ADMINISTRADOR;
    }
    
    public boolean tienePermisosDeAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }
    
    public boolean puedeModerar(Usuario otroUsuario) {
        // Los moderadores pueden moderar usuarios normales
        // Los admins pueden moderar todos excepto otros admins
        if (this.rol == Rol.ADMINISTRADOR) {
            return otroUsuario.rol != Rol.ADMINISTRADOR || this.equals(otroUsuario);
        }
        
        if (this.rol == Rol.MODERADOR) {
            return otroUsuario.rol == Rol.USUARIO;
        }
        
        return false;
    }
    
    public boolean puedeCrearScrim() {
        return this.activo && this.rol != null;
    }
    
    public boolean puedeReportar(Usuario reportado) {
        return this.activo && 
               !this.equals(reportado) && 
               reportado.activo;
    }
    
    public String getRolDisplay() {
        switch (this.rol) {
            case ADMINISTRADOR:
                return "🛡️ Administrador";
            case MODERADOR:
                return "⚖️ Moderador";
            case USUARIO:
            default:
                return "👤 Usuario";
        }
    }

    public boolean puedeUnirseAScrim(Scrim scrim) {
        return activo && 
               scrim.cumpleRequisitosMMR(this.mmr) && 
               scrim.getRegion().equals(this.region);
    }
}