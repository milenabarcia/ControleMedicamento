package com.example.controlemedicamentos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> lista;
    private Context context;
    private FirebaseFirestore db;

    public MedicamentoAdapter(List<Medicamento> lista, Context context, FirebaseFirestore db) {
        this.lista = lista;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento m = lista.get(position);

        holder.tvNome.setText(m.getNome());
        holder.tvHorario.setText(m.getHorario());
        holder.cbTomado.setOnCheckedChangeListener(null);
        holder.cbTomado.setChecked(m.isConsumido());


        holder.itemView.setAlpha(m.isConsumido() ? 0.5f : 1.0f);

        holder.cbTomado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("medicamentos")
                    .document(m.getId())
                    .update("consumido", isChecked);
        });


        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, CadastroMedicamentoActivity.class);
            i.putExtra("id", m.getId());
            context.startActivity(i);
        });


        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.confirmar_exclusao)
                    .setMessage(R.string.deseja_excluir)
                    .setPositiveButton(R.string.sim, (dialog, which) -> {
                        db.collection("medicamentos")
                                .document(m.getId())
                                .delete();
                    })
                    .setNegativeButton(R.string.nao, null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNome, tvHorario;
        CheckBox cbTomado;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            cbTomado = itemView.findViewById(R.id.cbTomado);
        }
    }
}