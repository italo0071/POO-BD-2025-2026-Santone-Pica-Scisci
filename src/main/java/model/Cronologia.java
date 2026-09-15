package model;

import java.time.LocalDateTime;

public class Cronologia {
    private int idAscolto;
    private LocalDateTime dataOra;
    private int secondiAscoltati;
    private boolean isOnDemand;
    private Utente utente;
    private Brano branoAscoltato;

    public Cronologia(int idAscolto, LocalDateTime dataOra, int secondiAscoltati, boolean isOnDemand, Utente utente, Brano branoAscoltato) {
        this.idAscolto = idAscolto;
        this.dataOra = dataOra;
        this.secondiAscoltati = secondiAscoltati;
        this.isOnDemand = isOnDemand;
        this.utente = utente;
        this.branoAscoltato = branoAscoltato;
    }

    public boolean isCompletato() {
        if (this.branoAscoltato != null) {
            return this.secondiAscoltati >= this.branoAscoltato.getDurataSecondi();
        }
        return false;
    }

    public int getIdAscolto() { return idAscolto; }
    public void setIdAscolto(int idAscolto) { this.idAscolto = idAscolto; }

    public LocalDateTime getDataOra() { return dataOra; }
    public void setDataOra(LocalDateTime dataOra) { this.dataOra = dataOra; }

    public int getSecondiAscoltati() { return secondiAscoltati; }
    public void setSecondiAscoltati(int secondiAscoltati) { this.secondiAscoltati = secondiAscoltati; }

    public boolean isOnDemand() { return isOnDemand; }
    public void setOnDemand(boolean isOnDemand) { this.isOnDemand = isOnDemand; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    public Brano getBranoAscoltato() { return branoAscoltato; }
    public void setBranoAscoltato(Brano branoAscoltato) { this.branoAscoltato = branoAscoltato; }
}