package com.tpo.finalproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "discord.bot")
public class DiscordProperties {
    
    private String token;
    private boolean enabled = false;
    private String guildId;
    private String notificationChannel = "general";
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
    
    public String getNotificationChannel() {
        return notificationChannel;
    }
    
    public void setNotificationChannel(String notificationChannel) {
        this.notificationChannel = notificationChannel;
    }
}