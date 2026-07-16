package com.example.fancup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GruposActivity extends AppCompatActivity {

    public static final String EXTRA_SELECAO = "extra_selecao";

    private RecyclerView recyclerGrupos;
    private TextView txtVazioGrupos;
    private GrupoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos);

        ImageButton btnVoltar = findViewById(R.id.btnVoltarGrupos);
        EditText edtBusca = findViewById(R.id.edtBuscaGrupo);
        recyclerGrupos = findViewById(R.id.recyclerGrupos);
        txtVazioGrupos = findViewById(R.id.txtVazioGrupos);

        btnVoltar.setOnClickListener(v -> finish());

        // Número de colunas se adapta ao tamanho da tela (ver res/values*/integers.xml)
        int spanCount = getResources().getInteger(R.integer.grid_span_grupos);
        recyclerGrupos.setLayoutManager(new GridLayoutManager(this, spanCount));

        adapter = new GrupoAdapter(Grupos.GRUPOS, selecao -> {
            Intent intent = new Intent(GruposActivity.this, JogadoresActivity.class);
            intent.putExtra(EXTRA_SELECAO, selecao);
            startActivity(intent);
        });
        recyclerGrupos.setAdapter(adapter);

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
                txtVazioGrupos.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
