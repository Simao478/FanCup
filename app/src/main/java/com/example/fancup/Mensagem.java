package com.example.fancup;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

@Entity(tableName = "mensagens")
public class Mensagem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String texto;
    private boolean doUsuario;

    // Construtor
    public Mensagem(String texto, boolean doUsuario) {
        this.texto = texto;
        this.doUsuario = doUsuario;
    }

    // Getters e Setters (O Room precisa deles)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isDoUsuario() { return doUsuario; }
    public void setDoUsuario(boolean doUsuario) { this.doUsuario = doUsuario; }

    @Database(entities = {Mensagem.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {

        private static AppDatabase instance;
        public abstract MensagemDao mensagemDao();

        public static synchronized AppDatabase getInstance(Context context) {
            if (instance == null) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "fancup_database")
                        .allowMainThreadQueries() // Permite rodar na MainThread para simplificar seu app atual
                        .build();
            }
            return instance;
        }
    }

    @Dao
    public static interface MensagemDao {

        @Insert
        void inserir(Mensagem mensagem);

        @Query("SELECT * FROM mensagens ORDER BY id ASC")
        List<Mensagem> obterHistorico();
    }
}