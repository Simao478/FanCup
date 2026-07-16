package com.example.fancup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fancup.Jogadores.Selecao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JogadoresActivity extends AppCompatActivity {

    private Spinner spinnerSelecao;
    private TextView txtVazioJogadores;
    private JogadorListaAdapter adapter;
    private List<Selecao> selecoesOrdenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogadores);

        ImageButton btnVoltar = findViewById(R.id.btnVoltarJogadores);
        spinnerSelecao = findViewById(R.id.spinnerSelecao);
        EditText edtBusca = findViewById(R.id.edtBuscaJogador);
        RecyclerView recyclerJogadores = findViewById(R.id.recyclerJogadores);
        txtVazioJogadores = findViewById(R.id.txtVazioJogadores);

        btnVoltar.setOnClickListener(v -> finish());

        // Seleções ordenadas alfabeticamente para o Spinner
        selecoesOrdenadas = new ArrayList<>(Jogadores.SELECOES);
        Collections.sort(selecoesOrdenadas, Comparator.comparing(Selecao::getNome));

        List<String> nomes = new ArrayList<>();
        for (Selecao selecao : selecoesOrdenadas) {
            nomes.add(selecao.getNome());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nomes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecao.setAdapter(spinnerAdapter);

        // Grid responsivo: número de colunas vem de res/values*/integers.xml
        int spanCount = getResources().getInteger(R.integer.grid_span_jogadores);
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        adapter = new JogadorListaAdapter();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Cabeçalhos de posição sempre ocupam a linha inteira
                return adapter.isCabecalho(position) ? spanCount : 1;
            }
        });
        recyclerJogadores.setLayoutManager(layoutManager);
        recyclerJogadores.setAdapter(adapter);

        spinnerSelecao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelecao(selecoesOrdenadas.get(position));
                atualizarEstadoVazio();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
                atualizarEstadoVazio();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Se veio da tela de Grupos com uma seleção pré-escolhida, seleciona ela no Spinner
        String selecaoInicial = getIntent().getStringExtra(GruposActivity.EXTRA_SELECAO);
        int indiceInicial = 0;
        if (selecaoInicial != null) {
            int indiceEncontrado = nomes.indexOf(selecaoInicial);
            if (indiceEncontrado >= 0) {
                indiceInicial = indiceEncontrado;
            }
        }
        spinnerSelecao.setSelection(indiceInicial);

        // Não depende do disparo automático do listener do Spinner: define o estado inicial direto
        if (!selecoesOrdenadas.isEmpty()) {
            adapter.setSelecao(selecoesOrdenadas.get(indiceInicial));
            atualizarEstadoVazio();
        }
    }

    private void atualizarEstadoVazio() {
        txtVazioJogadores.setVisibility(adapter.estaVazio() ? View.VISIBLE : View.GONE);
    }
}
