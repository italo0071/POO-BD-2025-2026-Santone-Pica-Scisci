package controller;

import model.*;
import dao.CronologiaDAO;
import exception.ElementoVuotoException;
import exception.SkipEsauritiException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class PlayerController {

    private List<Brano> codaRiproduzioneAttuale;
    private int indiceBranoCorrente;
    private Utente utenteAttivo;
    private long millisecondiInizioRiproduzione;

    private int secondiSalvatiPausa = 0;
    private boolean inPausa = false;
    private int volumeAttuale = 50;

    private javazoom.jl.player.Player lettoreFisicoMp3;
    private VolumeControlAudioDevice dispositivoAudio;
    private Thread threadRiproduzione;
    //Controller Audio
    private static class VolumeControlAudioDevice extends javazoom.jl.player.JavaSoundAudioDevice {
        private float volumeMultiplier = 0.5f;

        public void impostaVolumeReale(int percentuale) {
            float p = Math.max(0, Math.min(100, percentuale)) / 100.0f;
            this.volumeMultiplier = p * p * p;
        }

        @Override
        protected void writeImpl(short[] samples, int offs, int len) throws javazoom.jl.decoder.JavaLayerException {
            if (volumeMultiplier != 1.0f) {
                for (int i = offs; i < offs + len; i++) {
                    int sampleRitoccato = (int) (samples[i] * volumeMultiplier);
                    if (sampleRitoccato > Short.MAX_VALUE) sampleRitoccato = Short.MAX_VALUE;
                    if (sampleRitoccato < Short.MIN_VALUE) sampleRitoccato = Short.MIN_VALUE;
                    samples[i] = (short) sampleRitoccato;
                }
            }
            super.writeImpl(samples, offs, len);
        }
    }

    public PlayerController() {
        this.codaRiproduzioneAttuale = new ArrayList<>();
        this.indiceBranoCorrente = -1;
    }

    public void avviaRiproduzione(Riproducibile elemento, Utente utente) throws ElementoVuotoException, IllegalStateException {
        //Controllo account Free (On Demand)
        if (this.indiceBranoCorrente != -1) {
            if (utente instanceof model.UtenteFree) {
                throw new IllegalStateException("Funzione riservata agli account Premium. Attendi la fine del brano in riproduzione per cambiare traccia.");
            }
        }
        fermaAudioFisico();

        this.utenteAttivo = utente;
        this.codaRiproduzioneAttuale.clear();
        this.inPausa = false;
        this.secondiSalvatiPausa = 0;

        if (elemento instanceof Brano) this.codaRiproduzioneAttuale.add((Brano) elemento);
        else if (elemento instanceof Album) this.codaRiproduzioneAttuale.addAll(((Album)elemento).getListaBrani());
        else if (elemento instanceof Playlist) this.codaRiproduzioneAttuale.addAll(((Playlist)elemento).getListaBrani());

        if (!codaRiproduzioneAttuale.isEmpty()) {
            this.indiceBranoCorrente = 0;
            suonaBranoCorrente();
        } else {
            throw new ElementoVuotoException("Impossibile riprodurre elemento privo di brani!");
        }
    }

    public Brano getBranoCorrente() {
        if (indiceBranoCorrente >= 0 && indiceBranoCorrente < codaRiproduzioneAttuale.size()) {
            return codaRiproduzioneAttuale.get(indiceBranoCorrente);
        }
        return null;
    }

    public void canzoneFinitaNaturale() {
        if (indiceBranoCorrente == -1 || codaRiproduzioneAttuale.isEmpty()) return;
        Brano branoFinito = codaRiproduzioneAttuale.get(indiceBranoCorrente);
        fermaAudioFisico();
        try {
            registraAscoltoNelDB(utenteAttivo, branoFinito, branoFinito.getDurataSecondi(), false);
        } catch (SkipEsauritiException e) { System.err.println(e.getMessage()); }
        procediAlProssimo();
    }

    public void cliccatoAvantiOSkip() throws SkipEsauritiException {
        if (indiceBranoCorrente == -1 || codaRiproduzioneAttuale.isEmpty()) return;
        Brano branoSkippato = codaRiproduzioneAttuale.get(indiceBranoCorrente);

        long millisecondiFine = System.currentTimeMillis();
        int secAscoltati = inPausa ? secondiSalvatiPausa : (int) ((millisecondiFine - millisecondiInizioRiproduzione) / 1000);
        if (secAscoltati > branoSkippato.getDurataSecondi()) secAscoltati = branoSkippato.getDurataSecondi();

        fermaAudioFisico();
        this.inPausa = false;
        try {
            registraAscoltoNelDB(utenteAttivo, branoSkippato, secAscoltati, false);
            procediAlProssimo();
        } catch (SkipEsauritiException e) {
            this.indiceBranoCorrente = -1;
            throw e;
        }
    }

    public void cliccatoIndietro() {
        if (indiceBranoCorrente == -1 || codaRiproduzioneAttuale.isEmpty()) return;
        long millisecondiFine = System.currentTimeMillis();
        int secAscoltati = inPausa ? secondiSalvatiPausa : (int) ((millisecondiFine - millisecondiInizioRiproduzione) / 1000);

        fermaAudioFisico();
        this.inPausa = false;

        if (secAscoltati > 3 || indiceBranoCorrente == 0) {
            suonaBranoCorrente();
        } else {
            indiceBranoCorrente--;
            suonaBranoCorrente();
        }
    }

    public void mettiInPausa() {
        if (indiceBranoCorrente == -1 || codaRiproduzioneAttuale.isEmpty() || inPausa) return;
        long ora = System.currentTimeMillis();
        secondiSalvatiPausa = (int) ((ora - millisecondiInizioRiproduzione) / 1000);
        inPausa = true;
        fermaAudioFisico();
    }

    public void riprendiRiproduzione() {
        if (inPausa) {
            inPausa = false;
            saltaA(secondiSalvatiPausa);
        }
    }

    public void saltaA(int secondiTarget) {
        if (indiceBranoCorrente == -1 || codaRiproduzioneAttuale.isEmpty()) return;
        Brano inRiproduzione = codaRiproduzioneAttuale.get(indiceBranoCorrente);

        fermaAudioFisico();
        this.inPausa = false;
        this.millisecondiInizioRiproduzione = System.currentTimeMillis() - (secondiTarget * 1000L);

        if (inRiproduzione.getUrlFileAudio() != null && !inRiproduzione.getUrlFileAudio().isEmpty()) {
            threadRiproduzione = new Thread(() -> {
                //Siccome solitamente i file MP3 hanno un flusso di 128KBPS per poter skippare 1 secondo di audio dobbiamo avanzare di circa 16000 byte
                try {
                    FileInputStream fis = new FileInputStream(inRiproduzione.getUrlFileAudio());
                    long bytesToSkip = (long) secondiTarget * 16000;
                    fis.skip(bytesToSkip);

                    dispositivoAudio = new VolumeControlAudioDevice();
                    dispositivoAudio.impostaVolumeReale(this.volumeAttuale);

                    lettoreFisicoMp3 = new javazoom.jl.player.Player(fis, dispositivoAudio);
                    lettoreFisicoMp3.play();
                } catch (Exception e) {
                    System.err.println("Errore seek: " + e.getMessage());
                }
            });
            threadRiproduzione.start();
        }
    }

    public void impostaVolume(int percentuale) {
        this.volumeAttuale = Math.max(0, Math.min(100, percentuale));
        if (this.dispositivoAudio != null) {
            this.dispositivoAudio.impostaVolumeReale(this.volumeAttuale);
        }
    }

    private void procediAlProssimo() {
        this.inPausa = false;
        if (indiceBranoCorrente + 1 < codaRiproduzioneAttuale.size()) {
            indiceBranoCorrente++;
            suonaBranoCorrente();
        } else {
            this.indiceBranoCorrente = -1;
        }
    }

    public void premiStop() {
        fermaAudioFisico();
        this.indiceBranoCorrente = -1;
        this.inPausa = false;
    }

    private void fermaAudioFisico() {
        if (lettoreFisicoMp3 != null) {
            lettoreFisicoMp3.close();
            lettoreFisicoMp3 = null;
        }
        if (threadRiproduzione != null) {
            threadRiproduzione.interrupt();
            threadRiproduzione = null;
        }
    }

    private void suonaBranoCorrente() {
        if (indiceBranoCorrente >= 0 && indiceBranoCorrente < codaRiproduzioneAttuale.size()) {
            Brano inRiproduzione = codaRiproduzioneAttuale.get(indiceBranoCorrente);
            this.millisecondiInizioRiproduzione = System.currentTimeMillis();
            this.inPausa = false;

            if (inRiproduzione.getUrlFileAudio() != null && !inRiproduzione.getUrlFileAudio().isEmpty()) {
                threadRiproduzione = new Thread(() -> {
                    try {
                        FileInputStream fis = new FileInputStream(inRiproduzione.getUrlFileAudio());

                        dispositivoAudio = new VolumeControlAudioDevice();
                        dispositivoAudio.impostaVolumeReale(this.volumeAttuale);

                        lettoreFisicoMp3 = new javazoom.jl.player.Player(fis, dispositivoAudio);
                        lettoreFisicoMp3.play();
                    } catch (Exception e) {
                        System.err.println("Audio non trovato o non supportato: " + e.getMessage());
                    }
                });
                threadRiproduzione.start();
            }
        }
    }

    private void registraAscoltoNelDB(Utente utente, Brano brano, int secondiEffettivi, boolean isOnDemand) throws SkipEsauritiException {
        CronologiaDAO.inserisciAscolto(utente.getIdUtente(), brano.getIdBrano(), secondiEffettivi, isOnDemand);
    }
}