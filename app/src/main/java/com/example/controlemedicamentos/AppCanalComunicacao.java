package com.example.controlemedicamentos;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AppCanalComunicacao extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CANAL_REM";
            String nome = "Lembretes de medicamentos";
            String descricao = "Notificações para lembrar o uso dos medicamentos";

            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(channelId, nome, importancia);
            channel.setDescription(descricao);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}