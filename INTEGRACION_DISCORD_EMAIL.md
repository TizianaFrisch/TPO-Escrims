# 🎮 eSports Platform - Configuración de Integración 

## 📧 Configuración de Email

### 1. Gmail/Google Workspace

En `application.properties`, actualiza:

```properties
# Configuración Email SMTP (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password

# Habilitar notificaciones por email
app.notifications.email.enabled=true
```

**IMPORTANTE**: Para Gmail, necesitas crear una **App Password**:
1. Ve a tu cuenta de Google → Seguridad
2. Habilita autenticación en dos pasos
3. Genera una "App Password" específica para esta aplicación
4. Usa esa password (no tu password normal)

### 2. Otros Proveedores

```properties
# Outlook/Hotmail
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587

# Yahoo
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587

# Mailtrap (Para testing)
spring.mail.host=smtp.mailtrap.io
spring.mail.port=587
```

---

## 🤖 Configuración de Discord Bot

### 1. Crear Bot en Discord Developer Portal

1. Ve a https://discord.com/developers/applications
2. Crea una nueva aplicación
3. Ve a "Bot" en el menú lateral
4. Crea un bot y copia el **TOKEN**
5. Habilita estas intenciones:
   - `Send Messages`
   - `Read Message History`

### 2. Configurar en application.properties

```properties
# Discord Bot Token
discord.bot.token=TU_BOT_TOKEN_AQUI
discord.bot.enabled=true
discord.bot.guild-id=ID_DE_TU_SERVIDOR_DISCORD
discord.bot.notification-channel=general

# Habilitar notificaciones Discord
app.notifications.discord.enabled=true
```

### 3. OAuth2 para Login con Discord

Para permitir login con Discord, también configura:

```properties
# Discord OAuth2 (Para login de usuarios)
spring.security.oauth2.client.registration.discord.client-id=TU_CLIENT_ID
spring.security.oauth2.client.registration.discord.client-secret=TU_CLIENT_SECRET
```

**Obtener Client ID y Secret**:
1. En tu aplicación Discord → "OAuth2" → "General"
2. Copia Client ID y Client Secret
3. Añade redirect URI: `http://localhost:8080/login/oauth2/code/discord`

### 4. Invitar Bot a tu Servidor

1. Ve a "OAuth2" → "URL Generator"
2. Selecciona scopes: `bot`
3. Selecciona permisos: `Send Messages`, `Read Message History`
4. Usa la URL generada para invitar el bot

---

## 🔧 Configuración Completa de Ejemplo

```properties
# ===========================================
# CONFIGURACIÓN PRODUCCIÓN
# ===========================================

# Base de datos H2 (Para desarrollo)
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# Email REAL (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password-de-16-caracteres
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Discord Bot REAL
discord.bot.token=MTI0OTc5NDQ5MTk2NzU5ODYzMw.GXvKGH.ejemplo-token-bot-discord
discord.bot.enabled=true
discord.bot.guild-id=1234567890123456789
discord.bot.notification-channel=general

# Discord OAuth2 REAL (Para login)
spring.security.oauth2.client.registration.discord.client-id=1234567890123456789
spring.security.oauth2.client.registration.discord.client-secret=abc123def456-ejemplo-secret

# Habilitar notificaciones
app.notifications.email.enabled=true
app.notifications.discord.enabled=true

# Configuración de la aplicación
app.name=Mi eSports Platform
app.matchmaking.mmr-difference-threshold=100
app.matchmaking.default-strategy=BALANCEADO
app.moderation.auto-ban-threshold=5
app.moderation.auto-warn-threshold=3
```

---

## ✅ Testing de Configuración

### Test Email
```bash
curl -X POST http://localhost:8080/api/test/email \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "mensaje": "Test email"}'
```

### Test Discord
```bash
curl -X POST http://localhost:8080/api/test/discord \
  -H "Content-Type: application/json" \
  -d '{"discordId": "123456789", "mensaje": "Test Discord"}'
```

---

## 🚨 Troubleshooting

### Email no se envía
- ✅ Verifica que `app.notifications.email.enabled=true`
- ✅ Confirma App Password de Gmail (no password normal)
- ✅ Revisa logs para errores de SMTP

### Discord no funciona
- ✅ Verifica que `discord.bot.enabled=true`
- ✅ Confirma que el bot token es válido
- ✅ Asegúrate que el bot tiene permisos en el servidor
- ✅ Verifica que el bot esté online

### OAuth Discord falla
- ✅ Confirma redirect URI en Discord Developer Portal
- ✅ Verifica Client ID y Secret
- ✅ Asegúrate que los scopes sean `identify,email`

---

## 📝 Notas Importantes

1. **NUNCA** hardcodees tokens en el código
2. **SIEMPRE** usa variables de entorno en producción
3. Los servicios están diseñados para fallar silenciosamente si no están configurados
4. Puedes habilitar/deshabilitar cada servicio independientemente
5. Los logs te ayudarán a debuggear problemas de configuración

---

**¡La integración está 100% REAL y funcional! 🚀**