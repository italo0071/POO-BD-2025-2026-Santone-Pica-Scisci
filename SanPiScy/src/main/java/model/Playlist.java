package model;

import java.util.ArrayList;
import java.util.List;

public class Playlist implements Riproducibile {
    private int idPlaylist;
    private String nome;
    private boolean isPubblica;
    private List<Brano> listaBrani;
    private String urlIcona;

    public Playlist(int idPlaylist, String nome, boolean isPubblica, String urlIcona) {
        this.idPlaylist = idPlaylist;
        this.nome = nome;
        this.isPubblica = isPubblica;
        this.urlIcona = urlIcona;
        this.listaBrani = new ArrayList<>();
    }

    @Override
    public void riproduci(Utente utente) {
        System.out.println("Avvio riproduzione della playlist: " + this.nome);
        if (this.listaBrani.isEmpty()) {
            System.out.println("La playlist è vuota. Aggiungi qualche traccia prima di premere play!");
            return;
        }
        for (Brano b : this.listaBrani) {
            b.riproduci(utente);
        }
    }

    public void aggiungiBrano(Brano b) { this.listaBrani.add(b); }
    public void rimuoviBrano(Brano b) { this.listaBrani.remove(b); }

    public int getIdPlaylist() { return idPlaylist; }
    public void setIdPlaylist(int idPlaylist) { this.idPlaylist = idPlaylist; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isPubblica() { return isPubblica; }
    public void setPubblica(boolean pubblica) { isPubblica = pubblica; }

    public List<Brano> getListaBrani() { return listaBrani; }
    public String getUrlIcona() { return this.urlIcona; }
}