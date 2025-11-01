package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationsAdminController {
    private final NotificationsScheduler scheduler;
    private final com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionIntentRepository intentRepository;
    private final NotifierFactory notifierFactory;

    public NotificationsAdminController(NotificationsScheduler scheduler,
                                        com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionIntentRepository intentRepository,
                                        NotifierFactory notifierFactory) {
        this.scheduler = scheduler;
        this.intentRepository = intentRepository;
        this.notifierFactory = notifierFactory;
    }

    @PostMapping("/run-reminders")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MODERADOR')")
    public ResponseEntity<?> runReminders() {
        scheduler.triggerNow();
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }

    @GetMapping(value = "/reminded", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MODERADOR')")
    public ResponseEntity<?> reminded() {
        return ResponseEntity.ok(Map.of("reminded", scheduler.getReminded()));
    }

    @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MODERADOR')")
    public ResponseEntity<?> config() {
        return ResponseEntity.ok(Map.of(
                "reminderHours", scheduler.getReminderHours(),
                "windowMinutes", scheduler.getWindowMinutes(),
                "intervalMs", scheduler.getIntervalMs()
        ));
    }

    @GetMapping(value = "/intents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MODERADOR')")
    public ResponseEntity<?> listIntents(@RequestParam(required = false, defaultValue = "false") boolean failedOnly,
                                         @RequestParam(required = false, defaultValue = "100") int limit) {
        var all = intentRepository.findAll();
        var stream = all.stream();
        if (failedOnly) stream = stream.filter(i -> !i.isSuccess());
        var list = stream.limit(limit).toList();
        return ResponseEntity.ok(Map.of("intents", list, "count", list.size()));
    }

    @PostMapping(value = "/intents/{id}/retry", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MODERADOR')")
    public ResponseEntity<?> retryIntent(@PathVariable Long id) {
        var intent = intentRepository.findById(id).orElseThrow();
        Notifier notifier = switch (intent.getCanal()) {
            case EMAIL -> notifierFactory.createEmail();
            case PUSH -> notifierFactory.createPush();
            case DISCORD -> notifierFactory.createChat();
            default -> notifierFactory.createPush();
        };
        boolean ok = notifier.send(intent.getDestinatario(), intent.getPayload());
        var newIntent = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.NotificacionIntent();
        newIntent.setNotificacionId(intent.getNotificacionId());
        newIntent.setCanal(intent.getCanal());
        newIntent.setDestinatario(intent.getDestinatario());
        newIntent.setPayload(intent.getPayload());
        newIntent.setSuccess(ok);
        if (!ok) newIntent.setErrorMessage("Retry attempt failed");
        intentRepository.save(newIntent);
        return ResponseEntity.ok(Map.of("retrySuccess", ok, "newIntentId", newIntent.getId()));
    }
}
