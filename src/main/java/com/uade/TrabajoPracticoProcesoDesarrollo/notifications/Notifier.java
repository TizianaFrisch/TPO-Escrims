package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

public interface Notifier {
        /**
         * Intenta enviar el payload al destino. Devuelve true si el intento fue exitoso, false en caso contrario.
         */
        boolean send(String destino, String payload);
}
