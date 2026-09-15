package model;

public class Brano implements Riproducibile {
    private int idBrano;
    private String titolo;
    private int durataSecondi;
    private String genere;
    private String urlImmagine;
    private String urlFileAudio;

    public Brano(int idBrano, String titolo, int durataSecondi, String genere, String urlImmagine, String urlFileAudio) {
        this.idBrano = idBrano;
        this.titolo = titolo;
        this.durataSecondi = durataSecondi;
        this.genere = genere;
        this.urlImmagine = urlImmagine;
        this.urlFileAudio = urlFileAudio;
    }

    @Override
    public void riproduci(Utente utente) {
        System.out.println("▶ In ascolto: " + this.titolo);
    }

    public int getIdBrano() { return idBrano; }
    public String getTitolo() { return titolo; }
    public int getDurataSecondi() { return durataSecondi; }
    public String getGenere() { return genere; }
    public String getUrlImmagine() { return urlImmagine; }
    public String getUrlFileAudio() { return urlFileAudio; }
}