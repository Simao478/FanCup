package com.example.fancup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fancup.Jogadores.Jogador;
import com.example.fancup.Jogadores.Selecao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JogadorListaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_CABECALHO = 0;
    private static final int TIPO_JOGADOR = 1;

    // Ordem fixa de exibição das posições
    private static final List<String> ORDEM_POSICOES =
            Arrays.asList("Goleiro", "Defensor", "Meio-campista", "Atacante");

    private List<Jogador> jogadoresDaSelecao = new ArrayList<>();
    private final List<Object> linhas = new ArrayList<>(); // String (cabeçalho) ou Jogador
    private String termoBusca = "";

    /** Troca a seleção exibida (chamado ao escolher no Spinner). */
    public void setSelecao(Selecao selecao) {
        jogadoresDaSelecao = selecao != null ? selecao.getJogadores() : new ArrayList<>();
        reconstruirLinhas();
    }

    /** Filtra os jogadores da seleção atual por nome ou clube. */
    public void filtrar(String termo) {
        this.termoBusca = termo == null ? "" : termo.trim().toLowerCase(Locale.getDefault());
        reconstruirLinhas();
    }

    private void reconstruirLinhas() {
        linhas.clear();
        for (String posicao : ORDEM_POSICOES) {
            List<Jogador> daPosicao = new ArrayList<>();
            for (Jogador jogador : jogadoresDaSelecao) {
                boolean posicaoCombina = posicao.equals(jogador.getPosicao());
                boolean buscaCombina = termoBusca.isEmpty()
                        || jogador.getNome().toLowerCase(Locale.getDefault()).contains(termoBusca)
                        || jogador.getClube().toLowerCase(Locale.getDefault()).contains(termoBusca);
                if (posicaoCombina && buscaCombina) {
                    daPosicao.add(jogador);
                }
            }
            if (!daPosicao.isEmpty()) {
                linhas.add(tituloPosicao(posicao));
                linhas.addAll(daPosicao);
            }
        }
        notifyDataSetChanged();
    }

    private String tituloPosicao(String posicao) {
        switch (posicao) {
            case "Goleiro": return "Goleiros";
            case "Defensor": return "Defensores";
            case "Meio-campista": return "Meio-campistas";
            case "Atacante": return "Atacantes";
            default: return posicao;
        }
    }

    public boolean isCabecalho(int position) {
        return linhas.get(position) instanceof String;
    }

    @Override
    public int getItemViewType(int position) {
        return isCabecalho(position) ? TIPO_CABECALHO : TIPO_JOGADOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_CABECALHO) {
            return new CabecalhoViewHolder(inflater.inflate(R.layout.item_cabecalho_posicao, parent, false));
        }
        return new JogadorViewHolder(inflater.inflate(R.layout.item_jogador, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object linha = linhas.get(position);
        if (holder instanceof CabecalhoViewHolder) {
            ((CabecalhoViewHolder) holder).txtTitulo.setText((String) linha);
        } else if (holder instanceof JogadorViewHolder) {
            Jogador jogador = (Jogador) linha;
            JogadorViewHolder vh = (JogadorViewHolder) holder;
            vh.txtNome.setText(jogador.getNome());
            vh.txtClube.setText(jogador.getClube());
        }
    }

    @Override
    public int getItemCount() {
        return linhas.size();
    }

    /** true quando não há nenhum jogador a exibir (útil para a mensagem de "vazio"). */
    public boolean estaVazio() {
        return linhas.isEmpty();
    }

    static class CabecalhoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;

        CabecalhoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = (TextView) itemView;
        }
    }

    static class JogadorViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtClube;

        JogadorViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomeJogador);
            txtClube = itemView.findViewById(R.id.txtClubeJogador);
        }
    }
}
