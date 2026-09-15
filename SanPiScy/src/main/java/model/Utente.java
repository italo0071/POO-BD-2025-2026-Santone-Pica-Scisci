package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Utente {
    protected int idUtente;
    protected String nome;
    protected String cognome;
    protected String username;
    protected String email;
    protected String password;
    protected String urlFotoProfilo;

    protected List<Utente> follower;
    protected List<Utente> seguiti;
    protected List<Playlist> miePlaylist;

    public Utente(int idUtente, String nome, String cognome, String username, String email, String password) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.urlFotoProfilo = "/Generic avatar.png";

        this.follower = new ArrayList<>();
        this.seguiti = new ArrayList<>();
        this.miePlaylist = new ArrayList<>();
    }

    public abstract void ricevePubblicita();
    public abstract boolean puoScaricareOffline();

    public void aggiungiFollower(Utente u) { this.follower.add(u); }
    public void seguiUtente(Utente u) { this.seguiti.add(u); }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUrlFotoProfilo() { return urlFotoProfilo; }
    public void setUrlFotoProfilo(String urlFotoProfilo) { this.urlFotoProfilo = urlFotoProfilo; }

    public List<Utente> getFollower() { return follower; }
    public List<Utente> getSeguiti() { return seguiti; }
    public List<Playlist> getMiePlaylist() { return miePlaylist; }
}