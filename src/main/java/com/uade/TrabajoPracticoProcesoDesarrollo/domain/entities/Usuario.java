
package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String passwordHash;

    private String region;

    // Perfil editable
    @ManyToOne(fetch = FetchType.LAZY)
    private Juego juegoPrincipal;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> rolesPreferidos = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String disponibilidadHoraria; // formato libre: texto, JSON, etc.

    // Preferencias de notificación simples
    private Boolean notifyPush;
    private Boolean notifyEmail;
    private Boolean notifyDiscord;

    @Enumerated(EnumType.STRING)
    private VerificacionEstado verificacionEstado;
    
    // Latencia en ms (opcional, usada por estrategias de matchmaking)
    private Integer latencia;

    // === Campos adicionales tomados del otro proyecto (opcionales) ===
    private Integer mmr;
    // Deprecado: usar rolesPreferidos
    @Deprecated
    private String rolPreferido;
    private String discordId;
    private String summoner;

    private Boolean activo = Boolean.TRUE;

    // Sistema de penalizaciones
    private Integer strikes = 0;
    private LocalDateTime cooldownHasta;

    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaConexion;

    public enum Rol { USUARIO, MODERADOR, ADMINISTRADOR }

    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.USUARIO;

    // Relaciones opcionales (compatibilidad con TPO)
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Postulacion> postulaciones = new ArrayList<>();

    @OneToMany(mappedBy = "creador", fetch = FetchType.LAZY)
    private List<Scrim> scrimsCreados = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        ultimaConexion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaConexion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
        public Juego getJuegoPrincipal() { return juegoPrincipal; }
        public void setJuegoPrincipal(Juego juegoPrincipal) { this.juegoPrincipal = juegoPrincipal; }
        public List<String> getRolesPreferidos() { return rolesPreferidos; }
        public void setRolesPreferidos(List<String> rolesPreferidos) { this.rolesPreferidos = rolesPreferidos; }
        public String getDisponibilidadHoraria() { return disponibilidadHoraria; }
        public void setDisponibilidadHoraria(String disponibilidadHoraria) { this.disponibilidadHoraria = disponibilidadHoraria; }
    public Boolean getNotifyPush() { return notifyPush; }
    public void setNotifyPush(Boolean notifyPush) { this.notifyPush = notifyPush; }
    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public Boolean getNotifyDiscord() { return notifyDiscord; }
    public void setNotifyDiscord(Boolean notifyDiscord) { this.notifyDiscord = notifyDiscord; }
    public VerificacionEstado getVerificacionEstado() { return verificacionEstado; }
    public void setVerificacionEstado(VerificacionEstado verificacionEstado) { this.verificacionEstado = verificacionEstado; }
    public Integer getLatencia() { return latencia; }
    public void setLatencia(Integer latencia) { this.latencia = latencia; }
    public Integer getMmr() { return mmr; }
    public void setMmr(Integer mmr) { this.mmr = mmr; }
    public String getRolPreferido() { return rolPreferido; }
    public void setRolPreferido(String rolPreferido) { this.rolPreferido = rolPreferido; }
    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }
    public String getSummoner() { return summoner; }
    public void setSummoner(String summoner) { this.summoner = summoner; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Integer getStrikes() { return strikes; }
    public void setStrikes(Integer strikes) { this.strikes = strikes; }
    public LocalDateTime getCooldownHasta() { return cooldownHasta; }
    public void setCooldownHasta(LocalDateTime cooldownHasta) { this.cooldownHasta = cooldownHasta; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getUltimaConexion() { return ultimaConexion; }
    public void setUltimaConexion(LocalDateTime ultimaConexion) { this.ultimaConexion = ultimaConexion; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public List<Postulacion> getPostulaciones() { return postulaciones; }
    public List<Scrim> getScrimsCreados() { return scrimsCreados; }
}
