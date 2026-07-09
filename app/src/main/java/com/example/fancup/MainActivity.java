package com.example.fancup;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText edtPergunta;
    ImageButton btnEnviar;
    ImageButton btnExcluirHistorico;
    android.widget.TextView txtResposta;
    ArrayList<Mensagem> listaMensagens;
    MensagemAdapter adapter;
    AppDatabase database;

    // Prompt de sistema: define a "personalidade" da IA como analista de futebol.
    // Ajuste o texto como quiser.
    private static final String SYSTEM_PROMPT =
            "Você é a IA oficial do aplicativo FanCup. Seu nome é FanBot. " +
                    "Você é um analista esportivo profissional especializado em estatísticas e probabilidades de futebol. " +
                    "REGRA CRÍTICA DE CONTEXTO: Você não possui acesso em tempo real ao calendário de jogos de amanhã ou de datas futuras. Por isso, para evitar erros cronológicos ou inventar partidas que já aconteceram, você NUNCA deve tentar adivinhar quais são os jogos do dia. " +
                    "Se o usuário perguntar algo genérico como 'quais os jogos de amanhã?', 'quais os jogos de hoje?' ou pedir palpites sem especificar, responda educadamente dizendo que, para garantir a precisão matemática, você precisa que ele informe quais são os TIMES (Mandante e Visitante) e a DATA exata do confronto. " +
                    "Quando o usuário fornecer os times e a data, monte a análise estatística seguindo estas regras estritas de formatação: " +
                    "1. NÃO USE ASTERISCOS (*) em nenhuma parte do texto. " +
                    "2. Use títulos com '##' para destacar os jogos e hífens (-) para listar os tópicos. " +
                    "3. Deixe o visual extremamente limpo e scannable. " +
                    "Na análise dos times fornecidos, isole os últimos 5 jogos de cada equipe (casa/fora), média de gols esperados (xG), solidez defensiva (xGA) e confrontos diretos (H2H). Aponte tendências e sugira até 3 placares prováveis baseados estritamente na tendência matemática, destacando o mais forte. " +
                    "Mantenha um tom profissional, direto e técnico. Se o usuário fugir do tema futebol, lembre-o educadamente do seu foco.\n\n" +
                    "Pergunta do usuário: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recyclerMensagens);
        edtPergunta = findViewById(R.id.edtPergunta);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnExcluirHistorico = findViewById(R.id.btnExcluirHistorico);
        txtResposta = findViewById(R.id.txtResposta);

        listaMensagens = new ArrayList<>(database.mensagemDao().obterHistorico());
        adapter = new MensagemAdapter(listaMensagens);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Se já existe histórico salvo, a mensagem de boas-vindas não deve aparecer
        if (!listaMensagens.isEmpty()) {
            txtResposta.setVisibility(android.view.View.GONE);
        }

        btnEnviar.setOnClickListener(v -> {
            String inputUser = edtPergunta.getText().toString().trim();
            if (inputUser.isEmpty()) return;
            processarPergunta(inputUser);
        });

        btnExcluirHistorico.setOnClickListener(v -> limparHistorico());
    }

    private void limparHistorico() {
        database.mensagemDao().limparHistorico();
        int tamanhoAnterior = listaMensagens.size();
        listaMensagens.clear();
        adapter.notifyItemRangeRemoved(0, tamanhoAnterior);

        // Traz a mensagem de boas-vindas de volta
        txtResposta.setVisibility(android.view.View.VISIBLE);

        Toast.makeText(this, "Histórico apagado.", Toast.LENGTH_SHORT).show();
    }

    // Quantidade máxima de tentativas em caso de erro 503 (modelo sobrecarregado)
    private static final int MAX_TENTATIVAS = 3;

    private void processarPergunta(String userPrompt) {
        // Esconde a mensagem de boas-vindas ao enviar a primeira pergunta
        if (txtResposta.getVisibility() == android.view.View.VISIBLE) {
            txtResposta.setVisibility(android.view.View.GONE);
        }

        // 1. Salva e exibe a mensagem do usuário (agora persistindo no banco também)
        Mensagem msgUsuario = new Mensagem(userPrompt, true);
        database.mensagemDao().inserir(msgUsuario);
        listaMensagens.add(msgUsuario);
        adapter.notifyItemInserted(listaMensagens.size() - 1);
        recyclerView.scrollToPosition(listaMensagens.size() - 1);
        edtPergunta.setText("");

        String dataAtual = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"))
                .format(new java.util.Date());
        String promptCompleto = SYSTEM_PROMPT + "\n(Data de hoje: " + dataAtual + ")\n\n" + userPrompt;
        chamarGemini(promptCompleto, 1);
    }

    private void chamarGemini(String promptCompleto, int tentativaAtual) {
        // Modelo "lite" costuma ter menos fila/concorrência que o "flash" padrão
        GenerativeModel gm = new GenerativeModel("gemini-flash-lite-latest", BuildConfig.GEMINI_API_KEY);
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(gm);

        Content content = new Content("user", Collections.singletonList(new TextPart(promptCompleto)));
        ListenableFuture<GenerateContentResponse> future = modelFutures.generateContent(content);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    String resposta = result.getText();
                    if (resposta != null && !resposta.isEmpty()) {
                        Mensagem msgResposta = new Mensagem(resposta, false);
                        database.mensagemDao().inserir(msgResposta);
                        listaMensagens.add(msgResposta);
                        adapter.notifyItemInserted(listaMensagens.size() - 1);
                        recyclerView.scrollToPosition(listaMensagens.size() - 1);
                    } else {
                        Toast.makeText(MainActivity.this, "A IA não retornou resposta.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                android.util.Log.e("GeminiDebug", "Falha ao gerar conteudo (tentativa " + tentativaAtual + ")", t);

                boolean isSobrecarga = t.getMessage() != null &&
                        (t.getMessage().contains("503") || t.getMessage().contains("UNAVAILABLE")
                                || t.getMessage().contains("overloaded") || t.getMessage().contains("high demand"));

                if (isSobrecarga && tentativaAtual < MAX_TENTATIVAS) {
                    // Espera crescente entre tentativas: 2s, 4s, 6s...
                    long esperaMs = tentativaAtual * 2000L;
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Servidor ocupado, tentando novamente...", Toast.LENGTH_SHORT).show());
                    new android.os.Handler(getMainLooper()).postDelayed(
                            () -> chamarGemini(promptCompleto, tentativaAtual + 1), esperaMs);
                } else {
                    runOnUiThread(() -> {
                        // Erros comuns:
                        // - "API key not valid": key errada ou revogada
                        // - "Unable to resolve host": falta permissão INTERNET no Manifest
                        // - 429: limite de requisições gratuitas excedido
                        // - 503: modelo sobrecarregado (já tentamos novamente algumas vezes)
                        Toast.makeText(MainActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }, getMainExecutor());
    }
}