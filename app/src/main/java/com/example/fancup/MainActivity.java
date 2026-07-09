package com.example.fancup;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// IMPORTAÇÕES DA API DO GEMINI
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText edtPergunta;
    ImageButton btnEnviar;
    ImageButton btnExcluirHistorico;
    TextView txtResposta;

    ArrayList<Mensagem> listaMensagens;
    MensagemAdapter adapter;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerMensagens);
        edtPergunta = findViewById(R.id.edtPergunta);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnExcluirHistorico = findViewById(R.id.btnExcluirHistorico);
        txtResposta = findViewById(R.id.txtResposta);

        List<Mensagem> historicoSalvo = database.mensagemDao().obterHistorico();
        listaMensagens = new ArrayList<>(historicoSalvo);

        if (!listaMensagens.isEmpty()) {
            txtResposta.setVisibility(View.GONE);
        }

        adapter = new MensagemAdapter(listaMensagens);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (!listaMensagens.isEmpty()) {
            recyclerView.scrollToPosition(listaMensagens.size() - 1);
        }

        // SOLUÇÃO PARA O TECLADO NÃO COBRIR A MENSAGEM:
        // Força a lista a rolar para o fim sempre que o campo de texto receber o foco do teclado
        edtPergunta.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !listaMensagens.isEmpty()) {
                recyclerView.postDelayed(() -> recyclerView.scrollToPosition(listaMensagens.size() - 1), 200);
            }
        });

        // AÇÃO DO BOTÃO EXCLUIR HISTÓRICO
        btnExcluirHistorico.setOnClickListener(v -> {
            if (listaMensagens.isEmpty()) {
                Toast.makeText(this, "O histórico já está vazio!", Toast.LENGTH_SHORT).show();
                return;
            }

            database.mensagemDao().limparHistorico();
            listaMensagens.clear();
            adapter.notifyDataSetChanged();
            txtResposta.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Histórico apagado com sucesso!", Toast.LENGTH_SHORT).show();
        });

        // AÇÃO DO BOTÃO ENVIAR (INTEGRADO COM O GEMINI)
        btnEnviar.setOnClickListener(v -> {
            String pergunta = edtPergunta.getText().toString().trim();

            if (pergunta.isEmpty())
                return;

            if (txtResposta.getVisibility() == View.VISIBLE) {
                txtResposta.setVisibility(View.GONE);
            }

            // 1. Cria a mensagem do Usuário, salva no Room e atualiza a tela
            Mensagem novaMensagem = new Mensagem(pergunta, true);
            database.mensagemDao().inserir(novaMensagem);

            listaMensagens.add(novaMensagem);
            adapter.notifyItemInserted(listaMensagens.size() - 1);

            // Opcional: Limpa o campo e mantém a lista visível acima do teclado
            edtPergunta.setText("");

            // 2. ADICIONA A MENSAGEM DE CARREGAMENTO ("FanBot está pensando...")
            Mensagem mensagemPensando = new Mensagem("FanBot está pensando...", false);
            listaMensagens.add(mensagemPensando);
            int posicaoPensando = listaMensagens.size() - 1;
            adapter.notifyItemInserted(posicaoPensando);
            recyclerView.scrollToPosition(posicaoPensando);

            // --- CONFIGURAÇÃO DO GEMINI ---
            String apiKey = "AQ.Ab8RN6KmpcDcI_J7l54Nuitlv3SW9dLxcPenJijNdPI72OYFgQ";

            // Usando o modelo exato validado no seu comando curl
            GenerativeModel model = new GenerativeModel("gemini-flash-latest", apiKey);
            GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);

            String promptComContexto = "Você é a IA oficial do aplicativo FanCup. Seu nome é FanBot. " +
                    "Você é um analista esportivo profissional especializado em estatísticas e probabilidades de futebol. " +
                    "REGRA CRÍTICA DE CONTEXTO: Você não possui acesso em tempo real ao calendário de jogos de amanhã ou de datas futuras. Por isso, para evitar erros cronológicos ou inventar partidas que já aconteceram, você NUNCA deve tentar adivinhar quais são os jogos do dia. " +
                    "Se o usuário perguntar algo genérico como 'quais os jogos de amanhã?', 'quais os jogos de hoje?' ou pedir palpites sem especificar, responda educadamente dizendo que, para garantir a precisão matemática, você precisa que ele informe quais são os TIMES (Mandante e Visitante) e a DATA exata do confronto. " +
                    "Quando o usuário fornecer os times e a data, monte a análise estatística seguindo estas regras estritas de formatação: " +
                    "1. NÃO USE ASTERISCOS (*) em nenhuma parte do texto. " +
                    "2. Use títulos com '##' para destacar os jogos e hífens (-) para listar os tópicos. " +
                    "3. Deixe o visual extremamente limpo e scannable. " +
                    "Na análise dos times fornecidos, isole os últimos 5 jogos de cada equipe (casa/fora), média de gols esperados (xG), solidez defensiva (xGA) e confrontos diretos (H2H). Aponte tendências e sugira até 3 placares prováveis baseados estritamente na tendência matemática, destacando o mais forte. " +
                    "Mantenha um tom profissional, direto e técnico. Se o usuário fugir do tema futebol, lembre-o educadamente do seu foco.\n\n" +
                    "Pergunta do Usuário: " + pergunta;

            ListenableFuture<GenerateContentResponse> responseFuture = modelFutures.generateContent(
                    new Content.Builder().addText(promptComContexto).build()
            );

            // 3. Escuta a resposta dos servidores do Google
            Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    runOnUiThread(() -> {
                        String respostaIA = result.getText();

                        // Substitui o texto "Pensando..." pela resposta real no Banco Room
                        Mensagem respostaMensagem = new Mensagem(respostaIA, false);
                        database.mensagemDao().inserir(respostaMensagem);

                        // Substitui na lista visual da tela para manter a mesma posição
                        if (posicaoPensando < listaMensagens.size()) {
                            listaMensagens.set(posicaoPensando, respostaMensagem);
                            adapter.notifyItemChanged(posicaoPensando);
                        }
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    runOnUiThread(() -> {
                        // Se der erro, remove o balão de carregamento para não poluir
                        if (posicaoPensando < listaMensagens.size()) {
                            listaMensagens.remove(posicaoPensando);
                            adapter.notifyItemRemoved(posicaoPensando);
                        }

                        String erroDetalhado = t.getMessage();
                        if (erroDetalhado == null) erroDetalhado = t.toString();
                        Toast.makeText(MainActivity.this, "Erro Real: " + erroDetalhado, Toast.LENGTH_LONG).show();
                    });
                }
            }, this.getMainExecutor());
        });
    }
}