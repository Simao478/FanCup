package com.example.fancup;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MensagemDao {

    @Insert
    void inserir(Mensagem mensagem);

    @Query("SELECT * FROM mensagens ORDER BY id ASC")
    List<Mensagem> obterHistorico();

    // ADICIONE ESTA LINHA:
    @Query("DELETE FROM mensagens")
    void limparHistorico();
}