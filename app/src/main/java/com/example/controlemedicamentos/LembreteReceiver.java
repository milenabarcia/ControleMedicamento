package com.example.controlemedicamentos;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LembreteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ALARM", "onReceive chamado!");
        Toast.makeText(context, "LembreteReceiver disparou!", Toast.LENGTH_LONG).show();

        String nome = intent.getStringExtra("nome");
        String descricao = intent.getStringExtra("descricao");

        String titulo = context.getString(R.string.noti_titulo);
        String texto = nome;
        if (descricao != null && !descricao.isEmpty()) {
            texto = nome + " - " + descricao;
        }

        // Android 13+: checa permissão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                Log.w("ALARM", "Sem permissão POST_NOTIFICATIONS, não vai notificar");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CANAL_REM")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        int idNotificacao = (int) System.currentTimeMillis();
        nm.notify(idNotificacao, builder.build());
    }
}