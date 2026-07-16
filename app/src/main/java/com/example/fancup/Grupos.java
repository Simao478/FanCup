package com.example.fancup;

import java.util.List;

public class Grupos {
    public static class Grupo {
        private final String nome;
        private final List<String> selecoes;
        public Grupo(String nome, List<String> selecoes) {
            this.nome = nome;
            this.selecoes = selecoes;
        }
        public String getNome() {
            return nome;
        }
        public List<String> getSelecoes() {
            return selecoes;
        }
        @Override
        public String toString() {
            return "Grupo " + nome + " " + selecoes;
        }
    }
    public static final List<Grupo> GRUPOS = List.of(
        new Grupo("A", List.of("México", "África do Sul", "Coreia do Sul", "República Tcheca")),
        new Grupo("B", List.of("Canadá", "Bósnia e Herzegovina", "Catar", "Suíça")),
        new Grupo("C", List.of("Brasil", "Marrocos", "Haiti", "Escócia")),
        new Grupo("D", List.of("Estados Unidos", "Paraguai", "Austrália", "Turquia")),
        new Grupo("E", List.of("Alemanha", "Curaçao", "Costa do Marfim", "Equador")),
        new Grupo("F", List.of("Holanda", "Japão", "Suécia", "Tunísia")),
        new Grupo("G", List.of("Bélgica", "Egito", "Irã", "Nova Zelândia")),
        new Grupo("H", List.of("Espanha", "Cabo Verde", "Arábia Saudita", "Uruguai")),
        new Grupo("I", List.of("França", "Senegal", "Iraque", "Noruega")),
        new Grupo("J", List.of("Argentina", "Argélia", "Áustria", "Jordânia")),
        new Grupo("K", List.of("Portugal", "RD Congo", "Uzbequistão", "Colômbia")),
        new Grupo("L", List.of("Inglaterra", "Croácia", "Gana", "Panamá"))
    );
}
