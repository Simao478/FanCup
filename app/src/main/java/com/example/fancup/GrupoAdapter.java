package com.example.fancup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fancup.Grupos.Grupo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    public interface OnSelecaoClickListener {
        void onSelecaoClick(String nomeSelecao);
    }

    private final List<Grupo> gruposOriginais;
    private List<Grupo> gruposFiltrados;
    private final OnSelecaoClickListener listener;

    public GrupoAdapter(List<Grupo> grupos, OnSelecaoClickListener listener) {
        this.gruposOriginais = grupos;
        this.gruposFiltrados = grupos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        Grupo grupo = gruposFiltrados.get(position);
        holder.txtNomeGrupo.setText("Grupo " + grupo.getNome());

        holder.containerSelecoes.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        for (String selecao : grupo.getSelecoes()) {
            View linha = inflater.inflate(R.layout.item_selecao_linha, holder.containerSelecoes, false);
            TextView txtNome = linha.findViewById(R.id.txtNomeSelecao);
            txtNome.setText(selecao);
            linha.setOnClickListener(v -> {
                if (listener != null) listener.onSelecaoClick(selecao);
            });
            holder.containerSelecoes.addView(linha);
        }
    }

    @Override
    public int getItemCount() {
        return gruposFiltrados.size();
    }

    /** Filtra por nome de seleção ou de grupo (ex: "brasil" ou "grupo c"). */
    public void filtrar(String termoBusca) {
        String termo = termoBusca == null ? "" : termoBusca.trim().toLowerCase(Locale.getDefault());
        if (termo.isEmpty()) {
            gruposFiltrados = gruposOriginais;
        } else {
            List<Grupo> resultado = new ArrayList<>();
            for (Grupo grupo : gruposOriginais) {
                boolean grupoCombina = ("grupo " + grupo.getNome()).toLowerCase(Locale.getDefault()).contains(termo);
                boolean algumaSelecaoCombina = false;
                for (String selecao : grupo.getSelecoes()) {
                    if (selecao.toLowerCase(Locale.getDefault()).contains(termo)) {
                        algumaSelecaoCombina = true;
                        break;
                    }
                }
                if (grupoCombina || algumaSelecaoCombina) {
                    resultado.add(grupo);
                }
            }
            gruposFiltrados = resultado;
        }
        notifyDataSetChanged();
    }

    static class GrupoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeGrupo;
        LinearLayout containerSelecoes;

        GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeGrupo = itemView.findViewById(R.id.txtNomeGrupo);
            containerSelecoes = itemView.findViewById(R.id.containerSelecoes);
        }
    }
}
