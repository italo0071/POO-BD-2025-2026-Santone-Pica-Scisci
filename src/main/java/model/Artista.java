package model;

import java.util.ArrayList;
import java.util.List;

public class Artista extends Utente {
    private int idArtista;
    private String nomeDArte;
    private String biografia;
    private boolean verificato;
    private List<Album> albumPubblicati;

    public Artista(int idUtente, int idArtista, String nome, String cognome, String username, String email, String password, String nomeDArte, String biografia) {
        super(idUtente, nome, cognome, username, email, password);
        this.idArtista = idArtista;
        this.nomeDArte = nomeDArte;
        this.biografia = biografia;
        this.verificato = false;
        this.albumPubblicati = new ArrayList<>();
    }

    @Override
    public void ricevePubblicita() {
        System.out.println("check del poliformismo, bravo amo tu addirittura fatturi e non hai l'ad");
    }

    @Override
    public boolean puoScaricareOffline() { return true; }

    public void pubblicaAlbum(Album album) {
        this.albumPubblicati.add(album);
    }

    public int getIdArtista() { return idArtista; }
    public void setIdArtista(int idArtista) { this.idArtista = idArtista; }

    public String getNomeDArte() { return nomeDArte; }
    public void setNomeDArte(String nomeDArte) { this.nomeDArte = nomeDArte; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public boolean isVerificato() { return verificato; }
    public void setVerificato(boolean verificato) { this.verificato = verificato; }

    public List<Album> getAlbumPubblicati() { return albumPubblicati; }
}