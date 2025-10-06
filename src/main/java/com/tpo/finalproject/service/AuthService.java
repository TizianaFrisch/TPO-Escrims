package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    
    @Value("${oauth.steam.enabled:false}")
    private boolean steamOAuthEnabled;
    
    @Value("${oauth.riot.enabled:false}")
    private boolean riotOAuthEnabled;
    
    @Value("${oauth.discord.enabled:false}")
    private boolean discordOAuthEnabled;
    
    // ============== PATRÓN STRATEGY INTEGRADO PARA OAUTH ==============
    // Estrategias OAuth para diferentes proveedores
    
    public interface OAuthProvider {
        String getProviderName();
        String getAuthorizationUrl(String redirectUri, String state);
        OAuthUserInfo getUserInfo(String accessToken);
        boolean isEnabled();
    }
    
    // OAuth user info común
    public static class OAuthUserInfo {
        private final String providerId;
        private final String email;
        private final String username;
        private final String displayName;
        private final String avatarUrl;
        
        public OAuthUserInfo(String providerId, String email, String username, String displayName, String avatarUrl) {
            this.providerId = providerId;
            this.email = email;
            this.username = username;
            this.displayName = displayName;
            this.avatarUrl = avatarUrl;
        }
        
        // Getters
        public String getProviderId() { return providerId; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getAvatarUrl() { return avatarUrl; }
    }
    
    // Steam OAuth Provider
    private class SteamOAuthProvider implements OAuthProvider {
        @Override
        public String getProviderName() {
            return "STEAM";
        }
        
        @Override
        public String getAuthorizationUrl(String redirectUri, String state) {
            return String.format(
                "https://steamcommunity.com/openid/login?" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.mode=checkid_setup&" +
                "openid.return_to=%s&" +
                "openid.realm=%s&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select",
                redirectUri, redirectUri);
        }
        
        @Override
        public OAuthUserInfo getUserInfo(String accessToken) {
            // Simulación - en implementación real se llamaría a Steam Web API
            String steamId = extractSteamIdFromToken(accessToken);
            
            return new OAuthUserInfo(
                steamId,
                null, // Steam no proporciona email directamente
                "steam_user_" + steamId.substring(steamId.length() - 6),
                "Steam User",
                "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg"
            );
        }
        
        private String extractSteamIdFromToken(String token) {
            // Simulación de extracción de Steam ID
            return "76561198" + System.currentTimeMillis() % 1000000000L;
        }
        
        @Override
        public boolean isEnabled() {
            return steamOAuthEnabled;
        }
    }
    
    // Riot OAuth Provider
    private class RiotOAuthProvider implements OAuthProvider {
        @Override
        public String getProviderName() {
            return "RIOT";
        }
        
        @Override
        public String getAuthorizationUrl(String redirectUri, String state) {
            return String.format(
                "https://auth.riotgames.com/oauth2/authorize?" +
                "client_id=YOUR_CLIENT_ID&" +
                "response_type=code&" +
                "redirect_uri=%s&" +
                "scope=openid&" +
                "state=%s",
                redirectUri, state);
        }
        
        @Override
        public OAuthUserInfo getUserInfo(String accessToken) {
            // Simulación - en implementación real se llamaría a Riot API
            String puuid = generateRandomPuuid();
            
            return new OAuthUserInfo(
                puuid,
                "riot_user@riotgames.com",
                "RiotPlayer" + puuid.substring(0, 6),
                "Riot Player",
                "https://ddragon.leagueoflegends.com/cdn/14.19.1/img/profileicon/1.png"
            );
        }
        
        private String generateRandomPuuid() {
            return UUID.randomUUID().toString().replace("-", "");
        }
        
        @Override
        public boolean isEnabled() {
            return riotOAuthEnabled;
        }
    }
    
    // Discord OAuth Provider
    private class DiscordOAuthProvider implements OAuthProvider {
        @Override
        public String getProviderName() {
            return "DISCORD";
        }
        
        @Override
        public String getAuthorizationUrl(String redirectUri, String state) {
            return String.format(
                "https://discord.com/api/oauth2/authorize?" +
                "client_id=YOUR_CLIENT_ID&" +
                "redirect_uri=%s&" +
                "response_type=code&" +
                "scope=identify%%20email&" +
                "state=%s",
                redirectUri, state);
        }
        
        @Override
        public OAuthUserInfo getUserInfo(String accessToken) {
            // Simulación - en implementación real se llamaría a Discord API
            String discordId = String.valueOf(System.currentTimeMillis());
            
            return new OAuthUserInfo(
                discordId,
                "discord_user@discord.com",
                "DiscordUser#" + (1000 + (int)(Math.random() * 9000)),
                "Discord User",
                "https://cdn.discordapp.com/embed/avatars/0.png"
            );
        }
        
        @Override
        public boolean isEnabled() {
            return discordOAuthEnabled;
        }
    }
    
    // Factory para proveedores OAuth
    public OAuthProvider getOAuthProvider(String providerName) {
        switch (providerName.toUpperCase()) {
            case "STEAM":
                return new SteamOAuthProvider();
            case "RIOT":
                return new RiotOAuthProvider();
            case "DISCORD":
                return new DiscordOAuthProvider();
            default:
                throw new IllegalArgumentException("Proveedor OAuth no soportado: " + providerName);
        }
    }
    
    // Métodos OAuth públicos
    
    public String generarUrlAutorizacion(String provider, String redirectUri) {
        OAuthProvider oauthProvider = getOAuthProvider(provider);
        if (!oauthProvider.isEnabled()) {
            throw new IllegalStateException("OAuth para " + provider + " no está habilitado");
        }
        
        String state = UUID.randomUUID().toString();
        return oauthProvider.getAuthorizationUrl(redirectUri, state);
    }
    
    @Transactional
    public Usuario autenticarConOAuth(String provider, String accessToken) {
        OAuthProvider oauthProvider = getOAuthProvider(provider);
        if (!oauthProvider.isEnabled()) {
            throw new IllegalStateException("OAuth para " + provider + " no está habilitado");
        }
        
        // Obtener información del usuario desde el proveedor
        OAuthUserInfo userInfo = oauthProvider.getUserInfo(accessToken);
        
        // Buscar usuario existente o crear uno nuevo
        Usuario usuario = buscarOCrearUsuarioOAuth(userInfo, provider);
        
        // Actualizar última conexión
        usuario.setUltimaConexion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        return usuario;
    }
    
    private Usuario buscarOCrearUsuarioOAuth(OAuthUserInfo userInfo, String provider) {
        // Buscar por el ID del proveedor
        Optional<Usuario> usuarioExistente = buscarPorProveedor(userInfo, provider);
        
        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }
        
        // Crear nuevo usuario usando el método existente
        return crearUsuarioDesdeOAuth(userInfo, provider);
    }
    
    private Optional<Usuario> buscarPorProveedor(OAuthUserInfo userInfo, String provider) {
        switch (provider.toUpperCase()) {
            case "DISCORD":
                return usuarioRepository.findByDiscordId(userInfo.getProviderId());
            case "STEAM":
            case "RIOT":
                // Para Steam y Riot, buscar por email si está disponible
                if (userInfo.getEmail() != null) {
                    return usuarioRepository.findByEmail(userInfo.getEmail());
                }
                break;
        }
        return Optional.empty();
    }
    
    private Usuario crearUsuarioDesdeOAuth(OAuthUserInfo userInfo, String provider) {
        return registrarUsuario(
            generarUsernameUnico(userInfo.getUsername()),
            userInfo.getEmail() != null ? userInfo.getEmail() : 
                generarEmailTemporal(userInfo.getUsername(), provider),
            provider.equals("DISCORD") ? userInfo.getProviderId() : null,
            userInfo.getDisplayName(),
            "LAS", // Región por defecto
            1200 // MMR inicial
        );
    }
    
    private String generarUsernameUnico(String baseUsername) {
        String username = baseUsername;
        int contador = 1;
        
        while (usuarioRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + contador;
            contador++;
        }
        
        return username;
    }
    
    private String generarEmailTemporal(String username, String provider) {
        return username + "_" + provider.toLowerCase() + "@temp.esports.com";
    }
    
    @Transactional
    public Usuario registrarUsuario(String username, String email, String discordId, 
                                  String summoner, String region, Integer mmr) {
        
        // Validaciones
        validarDatosRegistro(username, email, discordId, summoner);
        
        // Patrón Builder para crear usuario
        Usuario usuario = Usuario.builder()
                .username(username)
                .email(email)
                .discordId(discordId)
                .summoner(summoner)
                .region(region)
                .mmr(mmr)
                .rol(Usuario.Rol.USUARIO)
                .fechaRegistro(LocalDateTime.now())
                .ultimaConexion(LocalDateTime.now())
                .build();
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Enviar notificación de bienvenida
        notificacionService.enviarNotificacionBienvenida(usuarioGuardado);
        
        return usuarioGuardado;
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticarUsuario(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorDiscordId(String discordId) {
        return usuarioRepository.findByDiscordId(discordId);
    }
    
    @Transactional
    public void actualizarUltimaConexion(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setUltimaConexion(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }
    
    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }
    
    private void validarDatosRegistro(String username, String email, String discordId, String summoner) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        if (usuarioRepository.existsByDiscordId(discordId)) {
            throw new IllegalArgumentException("El Discord ID ya está registrado");
        }
        
        // Validaciones adicionales
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }
        
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }
}