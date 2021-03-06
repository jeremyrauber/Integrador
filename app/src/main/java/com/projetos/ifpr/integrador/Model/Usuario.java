package com.projetos.ifpr.integrador.Model;

import java.util.Date;

/**
 * Created by Crash on 23/02/2017.
 */

public class Usuario {

    private Integer Id;
    private String nome;
    private String login;
    private String senha;
    private String telefone;
    private Date dataUltimoLogin;
    private Integer likes;
    private Integer dislikes;

    public Date getDataUltimoLogin() {
        return dataUltimoLogin;
    }

    public void setDataUltimoLogin(Date dataUltimoLogin) {
        this.dataUltimoLogin = dataUltimoLogin;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Integer getLikes() {
        if(likes!=null)
            return likes;
        else
            return 0;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        if(dislikes!=null)
            return dislikes;
        else
            return 0;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }
}
