package com.example.controlemedicamentos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICACOES = 1001;

    private RecyclerView rvMedicamentos;
    private FloatingActionButton fabAdicionar;
    private TextView tvFrase;

    private FirebaseFirestore db;
    private MedicamentoAdapter adapter;
    private List<Medicamento> lista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        rvMedicamentos = findViewById(R.id.rvMedicamentos);
        fabAdicionar = findViewById(R.id.fabAdicionar);
        tvFrase = findViewById(R.id.tvFrase);

        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentoAdapter(lista, this, db);
        rvMedicamentos.setAdapter(adapter);

        fabAdicionar.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CadastroMedicamentoActivity.class);
            startActivity(i);
        });


        pedirPermissaoNotificacao();


        ouvirMedicamentos();


        carregarFraseMotivacional();





    }


    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICACOES
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFICACOES) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.notificacoes_ativadas), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.notificacoes_negadas), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void ouvirMedicamentos() {
        db.collection("medicamentos")
                .orderBy("horario")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null || value == null) {
                            return;
                        }

                        lista.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Medicamento m = doc.toObject(Medicamento.class);
                            if (m != null) {
                                m.setId(doc.getId());
                                lista.add(m);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }



    private void carregarFraseMotivacional() {

        tvFrase.setText(getString(R.string.frase_carregando));

        new Thread(() -> {
            HttpURLConnection conexao = null;
            try {
                URL url = new URL("https://api.adviceslip.com/advice");
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(5000);
                conexao.setReadTimeout(5000);

                int codigoResposta = conexao.getResponseCode();
                Log.i("API", "Código de resposta: " + codigoResposta);

                if (codigoResposta == HttpURLConnection.HTTP_OK) {
                    InputStream is = conexao.getInputStream();

                    String json = converterStreamParaString(is);
                    Log.i("API", "JSON recebido: " + json);

                    JSONObject root = new JSONObject(json);
                    JSONObject slip = root.getJSONObject("slip");
                    String advice = slip.getString("advice");

                    runOnUiThread(() -> {
                        String texto = getString(R.string.frase_prefixo, advice);
                        tvFrase.setText(texto);
                    });
                } else {
                    runOnUiThread(() -> tvFrase.setText(R.string.frase_erro));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("API", "Erro ao buscar frase: " + e.getMessage());
                runOnUiThread(() -> tvFrase.setText(R.string.frase_erro));
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private String converterStreamParaString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha);
        }
        reader.close();
        return sb.toString();
    }

    private void testarNotificacaoDireta() {
        String titulo = "Teste de notificação";
        String texto = "Se você está vendo isso, o canal e a permissão estão OK.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CANAL_REM")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Sem permissão de notificação", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}