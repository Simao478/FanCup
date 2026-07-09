package com.example.fancup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder> {

    private final ArrayList<Mensagem> listaMensagens;

    // Identificadores para o tipo de mensagem
    private static final int TYPE_USUARIO = 1;
    private static final int TYPE_IA = 2;

    public MensagemAdapter(ArrayList<Mensagem> listaMensagens) {
        this.listaMensagens = listaMensagens;
    }

    @Override
    public int getItemViewType(int position) {
        // Verifica se a mensagem é do usuário ou da IA
        if (listaMensagens.get(position).isDoUsuario()) {
            return TYPE_USUARIO;
        } else {
            return TYPE_IA;
        }
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Infla o layout correspondente com base no tipo mapeado na sua pasta res/layout
        if (viewType == TYPE_USUARIO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ia, parent, false);
        }
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        Mensagem mensagem = listaMensagens.get(position);
        holder.txtTextoMensagem.setText(mensagem.getTexto());

        // AÇÃO DE COPIAR: Clique longo na mensagem
        holder.itemView.setOnLongClickListener(v -> {
            Context context = v.getContext();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Mensagem FanCup", mensagem.getTexto());

            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaMensagens.size();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView txtTextoMensagem;

        public MensagemViewHolder(@NonNull View view) {
            super(view);
            // Vincula o ID correto do TextView (o seu item_usuario usa txtMensagemUsuario)
            // Se o item_ia usar outro ID (ex: txtMensagemIa), garanta que ambos usem o mesmo ID ou ajuste aqui
            txtTextoMensagem = view.findViewById(R.id.txtMensagemUsuario);

            // Caso no seu item_ia.xml o ID do TextView seja diferente (ex: txtMensagemIa), descomente a linha abaixo:
            if (txtTextoMensagem == null) {
                txtTextoMensagem = view.findViewById(R.id.txtMensagemIA); // Ajuste com o ID real se necessário
            }
        }
    }
}
