package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GestoreFile {

    // 1. Lo cambi qui, nei parametri in ingresso
    public static String scegliESalvaFile(Component finestraDaddy, String tipoFile, String sottoCartella) {
        JFileChooser fileChooser = new JFileChooser();

        if (tipoFile.equals("IMMAGINE")) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("Immagini (JPG, PNG)", "jpg", "jpeg", "png"));
        } else if (tipoFile.equals("AUDIO")) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("File Audio (MP3)", "mp3"));
        }

        // 2. Lo cambi qui, quando apri la finestra
        int risultato = fileChooser.showOpenDialog(finestraDaddy);

        if (risultato == JFileChooser.APPROVE_OPTION) {
            File fileSelezionato = fileChooser.getSelectedFile();

            String percorsoDestinazione = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + sottoCartella;
            File cartellaDest = new File(percorsoDestinazione);
            if (!cartellaDest.exists()) {
                cartellaDest.mkdirs();
            }

            String nomeFileUnico = System.currentTimeMillis() + "_" + fileSelezionato.getName();
            File fileDestinazione = new File(cartellaDest, nomeFileUnico);

            try {
                Files.copy(fileSelezionato.toPath(), fileDestinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return fileDestinazione.getAbsolutePath();
            } catch (Exception e) {
                // 3. Lo cambi qui, nel messaggio di errore
                JOptionPane.showMessageDialog(finestraDaddy, "Errore durante il salvataggio del file: " + e.getMessage());
            }
        }
        return null;
    }
}