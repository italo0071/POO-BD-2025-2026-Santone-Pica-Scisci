package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Album implements Riproducibile {
    private int idAlbum;
    private String titolo;
    private LocalDate dataUscita;
    private String urlCopertina;
    private List<Brano> listaBrani;

    public Album(int idAlbum, String titolo, LocalDate dataUscita, String urlCopertina) {
        this.idAlbum = idAlbum;
        this.titolo = titolo;
        this.dataUscita = dataUscita;
        this.urlCopertina = urlCopertina;
        this.listaBrani = new ArrayList<>();
    }

    @Override
    public void riproduci(Utente utente) {
        System.out.println(" Avvio riproduzione dell'album: " + this.titolo);
        for (Brano b : this.listaBrani) {
            b.riproduci(utente);
        }
        System.out.println("⏹ Riproduzione album terminata.");
    }

    public void aggiungiBrano(Brano b) { this.listaBrani.add(b); }

    public int calcolaDurataTotale() {
        int totale = 0;
        for (Brano b : listaBrani) {
            totale += b.getDurataSecondi();
        }
        return totale;
    }

    public int getIdAlbum() { return idAlbum; }
    public void setIdAlbum(int idAlbum) { this.idAlbum = idAlbum; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public LocalDate getDataUscita() { return dataUscita; }
    public void setDataUscita(LocalDate dataUscita) { this.dataUscita = dataUscita; }

    public List<Brano> getListaBrani() { return listaBrani;}
    public String getUrlCopertina() { return this.urlCopertina; }
}