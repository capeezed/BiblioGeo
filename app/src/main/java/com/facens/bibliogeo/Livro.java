package com.facens.bibliogeo;

public class Livro {
    private String titulo;
    private String autores;
    private String dataPublicacao;
    private String editora;

    public Livro(String titulo, String autores, String dataPublicacao, String editora) {
        this.titulo = titulo;
        this.autores = autores;
        this.dataPublicacao = dataPublicacao;
        this.editora = editora;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutores() {
        return autores;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public String getEditora() {
        return editora;
    }

    public String textoParaLista() {
        return titulo + "\nAutor(es): " + autores + "\nPublicacao: " + dataPublicacao + "\nEditora: " + editora;
    }
}
