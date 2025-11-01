package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

public interface NotifierFactory {
    Notifier createPush();
    Notifier createEmail();
    Notifier createChat();
}
