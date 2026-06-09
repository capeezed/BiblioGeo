package com.facens.bibliogeo;

public class LivroCadastrado {
    private String id;
    private String titulo;
    private String autores;
    private String dataPublicacao;
    private String editora;
    private String situacao;
    private String statusLeitura;
    private String observacao;
    private double latitude;
    private double longitude;

    public LivroCadastrado() {
    }

    public LivroCadastrado(String id, String titulo, String autores, String dataPublicacao, String editora,
                           String situacao, String statusLeitura, String observacao,
                           double latitude, double longitude) {
        this.id = id;
        this.titulo = titulo;
        this.autores = autores;
        this.dataPublicacao = dataPublicacao;
        this.editora = editora;
        this.situacao = situacao;
        this.statusLeitura = statusLeitura;
        this.observacao = observacao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutores() {
        return autores;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(String dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getStatusLeitura() {
        return statusLeitura;
    }

    public void setStatusLeitura(String statusLeitura) {
        this.statusLeitura = statusLeitura;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String textoParaLista() {
        return titulo + "\nAutor: " + autores
                + "\nStatus: " + statusLeitura
                + " | Local: " + situacao
                + "\nCoord.: " + latitude + ", " + longitude;
    }
}
