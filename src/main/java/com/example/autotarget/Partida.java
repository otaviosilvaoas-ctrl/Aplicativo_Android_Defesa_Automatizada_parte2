package com.example.autotarget;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Modelo de dados para representar uma partida no Firebase Firestore.
 */
@IgnoreExtraProperties
public class Partida {
    private String id;
    private long timestamp;
    private int pontuacao;
    private int alvosAbatidos;
    private int quantidadeCanhoes;
    private int energiaRestante;
    private String usuarioId;

    // Construtor vazio obrigatório para o Firebase
    public Partida() {
    }

    // Construtor completo
    public Partida(String id, long timestamp, int pontuacao, int alvosAbatidos, int quantidadeCanhoes, int energiaRestante, String usuarioId) {
        this.id = id;
        this.timestamp = timestamp;
        this.pontuacao = pontuacao;
        this.alvosAbatidos = alvosAbatidos;
        this.quantidadeCanhoes = quantidadeCanhoes;
        this.energiaRestante = energiaRestante;
        this.usuarioId = usuarioId;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }

    public int getAlvosAbatidos() {
        return alvosAbatidos;
    }

    public void setAlvosAbatidos(int alvosAbatidos) {
        this.alvosAbatidos = alvosAbatidos;
    }

    public int getQuantidadeCanhoes() {
        return quantidadeCanhoes;
    }

    public void setQuantidadeCanhoes(int quantidadeCanhoes) {
        this.quantidadeCanhoes = quantidadeCanhoes;
    }

    public int getEnergiaRestante() {
        return energiaRestante;
    }

    public void setEnergiaRestante(int energiaRestante) {
        this.energiaRestante = energiaRestante;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
