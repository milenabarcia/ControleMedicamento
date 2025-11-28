package com.example.controlemedicamentos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;


import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class CadastroMedicamentoActivity extends AppCompatActivity {

    private EditText edtNome, edtDescricao;
    private TextView tvHorario;
    private Button btnEscolherHorario, btnSalvar;

    private FirebaseFirestore db;
    private String idMedicamento; // null = novo, não null = edição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_medicamento);

        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNome);
        edtDescricao = findViewById(R.id.edtDescricao);
        tvHorario = findViewById(R.id.tvHorario);
        btnEscolherHorario = findViewById(R.id.btnEscolherHorario);
        btnSalvar = findViewById(R.id.btnSalvar);

        criarCanalNotificacao(); // importante para Android 8+

        Intent intent = getIntent();
        idMedicamento = intent.getStringExtra("id");

        if (idMedicamento != null) {
            carregarMedicamento();
        }

        btnEscolherHorario.setOnClickListener(v -> abrirTimePicker());

        btnSalvar.setOnClickListener(v -> salvarMedicamento());
    }

    private void abrirTimePicker() {
        Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String h = String.format("%02d:%02d", hourOfDay, minute);
            tvHorario.setText(h);
        }, hora, minuto, true).show();
    }

    private void carregarMedicamento() {
        db.collection("medicamentos")
                .document(idMedicamento)
                .get()
                .addOnSuccessListener(doc -> {
                    Medicamento m = doc.toObject(Medicamento.class);
                    if (m != null) {
                        edtNome.setText(m.getNome());
                        edtDescricao.setText(m.getDescricao());
                        tvHorario.setText(m.getHorario());
                    }
                });
    }

    private void salvarMedicamento() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String horario = tvHorario.getText().toString().trim();

        if (nome.isEmpty() || horario.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos nome e horário", Toast.LENGTH_SHORT).show();
            return;
        }


        Medicamento m = new Medicamento(idMedicamento, nome, descricao, horario, false);

        if (idMedicamento == null) {
            db.collection("medicamentos")
                    .add(m)
                    .addOnSuccessListener(docRef -> {
                        m.setId(docRef.getId());
                        agendarNotificacao(m);
                        Toast.makeText(this, "Medicamento salvo!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            db.collection("medicamentos")
                    .document(idMedicamento)
                    .set(m)
                    .addOnSuccessListener(aVoid -> {
                        agendarNotificacao(m);
                        Toast.makeText(this, "Medicamento atualizado!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void agendarNotificacao(@NonNull Medicamento m) {

        String horario = m.getHorario();
        if (horario == null || !horario.contains(":")) return;

        String[] partes = horario.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto = Integer.parseInt(partes[1]);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hora);
        c.set(Calendar.MINUTE, minuto);
        c.set(Calendar.SECOND, 0);

        // Se o horário já passou hoje → agenda para amanhã
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.putExtra("nome", m.getNome());
        intent.putExtra("descricao", m.getDescricao());

        int requestCode = m.getNome().hashCode();

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }
    }
    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CANAL_REM";
            String nome = "Lembretes de medicamentos";
            String descricao = "Notificações para lembrar o uso dos medicamentos";
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, nome, importancia);
            channel.setDescription(descricao);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}