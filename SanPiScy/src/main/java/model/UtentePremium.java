package model;

import java.time.LocalDate;

public class UtentePremium extends Utente {
    private LocalDate dataScadenzaAbbonamento;
    private String metodoPagamento;

    public UtentePremium(int idUtente, String nome, String cognome, String username, String email, String password, LocalDate dataScadenzaAbbonamento, String metodoPagamento) {
        super(idUtente, nome, cognome, username, email, password);
        this.dataScadenzaAbbonamento = dataScadenzaAbbonamento;
        this.metodoPagamento = metodoPagamento;
    }

    @Override
    public void ricevePubblicita() {
        System.out.println("check del poliformismo, bravo amo paghi e non hai l'ad");
    }

    @Override
    public boolean puoScaricareOffline() {
        return true;
    }

    public boolean isAbbonamentoValido() {
        if (this.dataScadenzaAbbonamento != null) {
            return LocalDate.now().isBefore(this.dataScadenzaAbbonamento);
        }
        return false;
    }

    public LocalDate getDataScadenzaAbbonamento() { return dataScadenzaAbbonamento; }
    public void setDataScadenzaAbbonamento(LocalDate dataScadenzaAbbonamento) { this.dataScadenzaAbbonamento = dataScadenzaAbbonamento; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
}