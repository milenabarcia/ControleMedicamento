package com.example.controlemedicamentos;

public class Medicamento {

    private String id;
    private String nome;
    private String descricao;
    private String horario;  // formato simples: "HH:mm"
    private boolean consumido;

    public Medicamento() {
        // Construtor vazio exigido pelo Firestore
    }

    public Medicamento(String id, String nome, String descricao, String horario, boolean consumido) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
        this.consumido = consumido;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public boolean isConsumido() {
        return consumido;
    }

    public void setConsumido(boolean consumido) {
        this.consumido = consumido;
    }
}