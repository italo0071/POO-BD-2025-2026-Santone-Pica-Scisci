package gui;

import controller.AuthController;
import model.Utente;
import utils.GestoreFile;
import javax.swing.*;
import java.awt.*;

public class AuthWindow extends JFrame {

    private JPanel pannelloMainAuth, pannelloLogin, pannelloRegistrazione;
    private JLabel Email, Password;
    private JTextField campoEmail, campoUsernameReg, campoEmailReg;
    private JPasswordField campoPassword, campoPasswordReg;
    private JButton btnAccedi, btnVaiARegistrazione, btnRegistrati, btnVaiALogin;

    private String percorsoFotoScelta = null;
    private AuthController authController;

    public AuthWindow() {
        this.authController = new AuthController();

        setContentPane(pannelloMainAuth);
        setTitle("SanPiScy Music - Accesso");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        if (pannelloMainAuth != null) {
            pannelloMainAuth.add(pannelloLogin, "CardLogin");
            pannelloMainAuth.add(pannelloRegistrazione, "CardRegistrazione");
        }

        Color lillaScuro = Color.decode("#E3D3F8");
        if (btnAccedi != null) { btnAccedi.setBackground(lillaScuro); btnAccedi.setEnabled(true); }
        if (btnRegistrati != null) { btnRegistrati.setBackground(lillaScuro); btnRegistrati.setEnabled(true); }

        if (btnVaiARegistrazione != null) {
            btnVaiARegistrazione.setEnabled(true);
            btnVaiARegistrazione.setContentAreaFilled(false);
            btnVaiARegistrazione.setBorderPainted(false);
            btnVaiARegistrazione.addActionListener(e -> { svuotaCampi(); commutaPannello("CardRegistrazione"); });
        }

        if (btnVaiALogin != null) {
            btnVaiALogin.setEnabled(true);
            btnVaiALogin.setContentAreaFilled(false);
            btnVaiALogin.setBorderPainted(false);
            btnVaiALogin.addActionListener(e -> { svuotaCampi(); commutaPannello("CardLogin"); });
        }

        if (btnAccedi != null) {
            btnAccedi.addActionListener(e -> {
                String email = campoEmail.getText().trim();
                String password = new String(campoPassword.getPassword());

                Utente utenteLoggato = authController.effettuaLogin(email, password);

                if (utenteLoggato != null) {
                    new Home(utenteLoggato).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Email o password errati.");
                }
            });
        }

        if (btnRegistrati != null) {
            btnRegistrati.addActionListener(e -> {
                String username = campoUsernameReg.getText().trim();
                String email = campoEmailReg.getText().trim();
                String password = new String(campoPasswordReg.getPassword());

                int dialogResult = JOptionPane.showConfirmDialog(this, "Vuoi caricare una foto profilo?", "Foto Profilo", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    percorsoFotoScelta = GestoreFile.scegliESalvaFile(this, "IMMAGINE", "profili");
                }

                boolean ok = authController.registraNuovoUtente(username, email, password, percorsoFotoScelta);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registrazione completata!");
                    svuotaCampi();
                    commutaPannello("CardLogin");
                } else {
                    JOptionPane.showMessageDialog(this, "Errore nella compilazione o nel Database.");
                }
            });
        }
    }

    private void commutaPannello(String n) { ((CardLayout)pannelloMainAuth.getLayout()).show(pannelloMainAuth, n); }

    private void svuotaCampi() {
        if (campoEmail != null) campoEmail.setText("");
        if (campoPassword != null) campoPassword.setText("");
        if (campoUsernameReg != null) campoUsernameReg.setText("");
        if (campoEmailReg != null) campoEmailReg.setText("");
        if (campoPasswordReg != null) campoPasswordReg.setText("");
        percorsoFotoScelta = null;
    }
}