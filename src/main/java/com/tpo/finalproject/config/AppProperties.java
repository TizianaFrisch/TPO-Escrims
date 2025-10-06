package com.tpo.finalproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Notifications notifications = new Notifications();
    private Matchmaking matchmaking = new Matchmaking();
    private Moderation moderation = new Moderation();
    
    // Getters y Setters
    public Notifications getNotifications() {
        return notifications;
    }
    
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }
    
    public Matchmaking getMatchmaking() {
        return matchmaking;
    }
    
    public void setMatchmaking(Matchmaking matchmaking) {
        this.matchmaking = matchmaking;
    }
    
    public Moderation getModeration() {
        return moderation;
    }
    
    public void setModeration(Moderation moderation) {
        this.moderation = moderation;
    }
    
    // Clases internas para organizar propiedades
    public static class Notifications {
        private Email email = new Email();
        private Discord discord = new Discord();
        
        public Email getEmail() {
            return email;
        }
        
        public void setEmail(Email email) {
            this.email = email;
        }
        
        public Discord getDiscord() {
            return discord;
        }
        
        public void setDiscord(Discord discord) {
            this.discord = discord;
        }
        
        public static class Email {
            private boolean enabled = false;
            
            public boolean isEnabled() {
                return enabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
        
        public static class Discord {
            private boolean enabled = false;
            
            public boolean isEnabled() {
                return enabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }
    
    public static class Matchmaking {
        private int mmrDifferenceThreshold = 100;
        private String defaultStrategy = "BALANCEADO";
        
        public int getMmrDifferenceThreshold() {
            return mmrDifferenceThreshold;
        }
        
        public void setMmrDifferenceThreshold(int mmrDifferenceThreshold) {
            this.mmrDifferenceThreshold = mmrDifferenceThreshold;
        }
        
        public String getDefaultStrategy() {
            return defaultStrategy;
        }
        
        public void setDefaultStrategy(String defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }
    }
    
    public static class Moderation {
        private int autoBanThreshold = 5;
        private int autoWarnThreshold = 3;
        
        public int getAutoBanThreshold() {
            return autoBanThreshold;
        }
        
        public void setAutoBanThreshold(int autoBanThreshold) {
            this.autoBanThreshold = autoBanThreshold;
        }
        
        public int getAutoWarnThreshold() {
            return autoWarnThreshold;
        }
        
        public void setAutoWarnThreshold(int autoWarnThreshold) {
            this.autoWarnThreshold = autoWarnThreshold;
        }
    }
}