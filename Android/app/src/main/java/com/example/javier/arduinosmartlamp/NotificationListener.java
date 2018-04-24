package com.example.javier.arduinosmartlamp;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {
/* Esta clase hereda de NotificationListenerService, un servicio de Android que permite escuchar las notificaciones
   que se generan en el dispositivo. Con ella, se ponen a disposición de LampControl todas las notificaciones
   detectadas, donde son filtradas.
*/

    final static String MY_ACTION = "MY_ACTION";

    private void sendMessageToActivity(String msg) {

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("key", msg);
        sendBroadcast(intent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Listener creado.");

    }

    @Override
    public void onListenerConnected() {
        System.out.println("Escuchando.");

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        this.sendMessageToActivity(sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        System.out.println("Notificación borrada.");

    }

}