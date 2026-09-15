package com.aula.tiktoktech.model;

import com.google.firebase.firestore.DocumentId;

public class Post {
    @DocumentId private String id;
    private String url;
    private String descricao;
    private long likes;
    private long dislikes;
    private long comentarios;
    private Long criadoEm;

    public Post() { }
    public Post(String url, String descricao) {
        this.url = url;
        this.descricao = descricao;
        this.likes = 0L;
        this.dislikes = 0L;
        this.comentarios = 0L;
        this.criadoEm = System.currentTimeMillis();
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public long getLikes() { return likes; }
    public void setLikes(long likes) { this.likes = likes; }
    public long getDislikes() { return dislikes; }
    public void setDislikes(long dislikes) { this.dislikes = dislikes; }
    public long getComentarios() { return comentarios; }
    public void setComentarios(long comentarios) { this.comentarios = comentarios; }
    public Long getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Long criadoEm) { this.criadoEm = criadoEm; }
}
