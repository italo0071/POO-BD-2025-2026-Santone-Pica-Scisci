package model;

public class UtenteFree extends Utente {
    private int skipMassimi;
    private int riproduzioneOnDemand;

    public UtenteFree(int idUtente, String nome, String cognome, String username, String email, String password, int riproduzioneOnDemand) {
        super(idUtente, nome, cognome, username, email, password);
        this.skipMassimi = 3;
        this.riproduzioneOnDemand = riproduzioneOnDemand;
    }

    @Override
    public void ricevePubblicita() {
        System.out.println("bell 'sta pubblicita' eh?");
    }

    @Override
    public boolean puoScaricareOffline() {
        return false;
    }

    public void decrementaOnDemand() {
        if (this.riproduzioneOnDemand > 0) {
            this.riproduzioneOnDemand--;
        }
    }


    public void sincronizzaSkipDaDb(int skipDalDb) {
        this.skipMassimi = skipDalDb;
    }

    public int getSkipMassimi() { return skipMassimi; }
    public void setSkipMassimi(int skipMassimi) { this.skipMassimi = skipMassimi; }

    public int getRiproduzioneOnDemand() { return riproduzioneOnDemand; }
    public void setRiproduzioneOnDemand(int riproduzioneOnDemand) { this.riproduzioneOnDemand = riproduzioneOnDemand; }
}