package gui;

import controller.PlayerController;
import controller.LibreriaController;
import exception.ElementoVuotoException;
import model.Brano;
import model.Playlist;
import model.Utente;
import utils.GestoreFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Home extends JFrame {

    private JPanel pannelloPrincipale;
    private JPanel pannelloSidebar;
    private JPanel pannelloPagine;
    private JButton btnHome;
    private JButton btnPlaylist;
    private JButton cercaButton;
    private JButton btnCanzoni;
    private JPanel vistaHome;
    private JPanel vistaPlaylist;
    private JPanel vistaCerca;
    private JPanel vistaProfilo;
    private JPanel vistaPlayer;
    private JPanel topBar;
    private JButton btnAlbum;
    private JButton btnPlaylist2;
    private JButton btnBraniPreferiti;
    private JButton btnProfilo;
    private JPanel contenutoScorribile;
    private JPanel grigliaRecenti;
    private JPanel cardAlbum1;
    private JPanel cardAlbum2;
    private JPanel cardAlbum3;
    private JPanel cardAlbum4;
    private JPanel sezioneCorrelati;
    private JPanel cardCorrelato1;
    private JPanel cardCorrelato2;
    private JPanel cardCorrelato3;
    private JPanel cardCorrelato4;
    private JPanel cardCorrelato5;
    private JPanel cardCorrelato6;
    private JPanel topBarCerca;
    private JTextField campoRicerca;
    private JScrollPane scrollProfilo;
    private JPanel contenutoProfilo;
    private JPanel headerProfilo;
    private JLabel fotoProfiloGrande;
    private JLabel lblUsernameProfilo;
    private JButton btnImpostazioni;
    private JButton btnMenu;
    private JPanel pannelloPlayer;
    private JLabel lblCopertinaPiccola;
    private JLabel lblTitoloBrano;
    private JLabel lblArtistaBrano;
    private JLabel lblTempoAttuale;
    private JSlider sliderProgresso;
    private JLabel lblTempoTotale;
    private JButton iconaVolume;
    private JSlider sliderVolume;
    private JLabel lblCopertinaGrande;
    private JPanel headerPlaylist;
    private JButton btnNuovaPlaylist;
    private JPanel scrollPlaylist;
    private JPanel grigliaPlaylist;

    private JButton btnCaricaBrano;
    private JButton btnIndietro;
    private JButton btnAvanti;
    private JButton btnPausa;
    private JButton btnLogo;

    private Utente utenteLoggato;
    private PlayerController playerController;
    private LibreriaController libreriaController;

    private Timer timerRiproduzione;
    private int secondiCorrenti;
    private int durataTotaleCorrente;
    private boolean isPlaying = false;

    private JPanel pannelloRisultatiRicerca;

    private static class SliderUI extends BasicSliderUI {
        public SliderUI(JSlider slider) {
            super(slider);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(200, 190, 210));
            g2d.fillRoundRect(trackRect.x, trackRect.y + (trackRect.height / 2) - 1, trackRect.width, 4, 4, 4);
            int width = thumbRect.x - trackRect.x + (thumbRect.width / 2);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRoundRect(trackRect.x, trackRect.y + (trackRect.height / 2) - 1, width, 4, 4, 4);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(thumbRect.x + 2, thumbRect.y + 4, 12, 12);
        }
    }

    private void caricaBraniDb() {
        try {
            List<Brano> braniNelDb = libreriaController.ottieniTuttiIBrani();
            if (braniNelDb.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessun brano nel Database!", "Vuoto", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Playlist codaAttuale = new Playlist(999, "Coda", false, "");
            for (Brano b : braniNelDb) codaAttuale.aggiungiBrano(b);

            playerController.avviaRiproduzione(codaAttuale, utenteLoggato);
            isPlaying = true;
            if (btnPausa != null) assegnaIconaStandard(btnPausa, "/pause.png", 20, 20);

            pannelloPlayer.setVisible(true);
            aggiornaUIBranoCorrente();

        } catch (SQLException sqlEx) {
            JOptionPane.showMessageDialog(this, "Errore di lettura dal DB:\n" + sqlEx.getMessage(), "Errore SQL", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException premiumEx) {
            JOptionPane.showMessageDialog(this, premiumEx.getMessage(), "Passa a Premium", JOptionPane.WARNING_MESSAGE);
        } catch (ElementoVuotoException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void aggiornaUIBranoCorrente() {
        Brano branoDaSuonare = playerController.getBranoCorrente();
        if (branoDaSuonare == null) {
            if (timerRiproduzione != null) timerRiproduzione.stop();
            if (lblTitoloBrano != null) lblTitoloBrano.setText("Nessun brano in coda");
            if (sliderProgresso != null) sliderProgresso.setValue(0);
            return;
        }

        if (lblTitoloBrano != null) lblTitoloBrano.setText(branoDaSuonare.getTitolo());
        if (lblArtistaBrano != null) lblArtistaBrano.setText(branoDaSuonare.getGenere());

        if (lblCopertinaGrande != null) {
            String pathImg = branoDaSuonare.getUrlImmagine();
            impostaCopertina(lblCopertinaGrande, pathImg != null ? pathImg : "/album.png", 600, 600, 20);
        }
        if (lblCopertinaPiccola != null) {
            String pathImg = branoDaSuonare.getUrlImmagine();
            impostaCopertina(lblCopertinaPiccola, pathImg != null ? pathImg : "/album.png", 60, 60, 10);
        }

        durataTotaleCorrente = branoDaSuonare.getDurataSecondi();
        secondiCorrenti = 0;

        if (lblTempoTotale != null) lblTempoTotale.setText(formattaTempo(durataTotaleCorrente));
        if (lblTempoAttuale != null) lblTempoAttuale.setText("0:00");
        if (sliderProgresso != null) {
            sliderProgresso.setMaximum(durataTotaleCorrente);
            sliderProgresso.setValue(0);
        }

        if (timerRiproduzione != null) timerRiproduzione.stop();
        timerRiproduzione = new Timer(1000, e -> {
            secondiCorrenti++;
            if (sliderProgresso != null) sliderProgresso.setValue(secondiCorrenti);
            if (lblTempoAttuale != null) lblTempoAttuale.setText(formattaTempo(secondiCorrenti));
            if (secondiCorrenti >= durataTotaleCorrente) {
                timerRiproduzione.stop();
                playerController.canzoneFinitaNaturale();
                aggiornaUIBranoCorrente();
            }
        });
        timerRiproduzione.start();
    }

    private void mostraCreazionePlaylist() {
        JDialog dialog = new JDialog(this, "Crea Nuova Playlist", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.getContentPane().setBackground(Color.decode("#F3E8FF"));

        JTextField txtNome = new JTextField();
        JButton btnImg = new JButton("Carica Copertina (Opzionale)");
        String[] percorsoImg = new String[]{null};

        btnImg.addActionListener(e -> {
            percorsoImg[0] = GestoreFile.scegliESalvaFile(dialog, "IMMAGINE", "playlist");
            if (percorsoImg[0] != null) btnImg.setText("Immagine Caricata!");
        });

        JButton btnAnnulla = new JButton("Annulla");
        JButton btnSalva = new JButton("Crea Playlist");

        dialog.add(new JLabel("  Nome Playlist:"));
        dialog.add(txtNome);
        dialog.add(new JLabel("  Immagine:"));
        dialog.add(btnImg);
        dialog.add(btnAnnulla);
        dialog.add(btnSalva);

        btnAnnulla.addActionListener(e -> dialog.dispose());
        btnSalva.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Il nome non può essere vuoto.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                libreriaController.creaNuovaPlaylist(nome, utenteLoggato.getIdUtente(), percorsoImg[0]);
                popolaVistaPlaylist();
                JOptionPane.showMessageDialog(dialog, "Playlist creata con successo!", "Completato", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Errore DB: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    private void mostraDialogCaricamentoBrano() {
        JDialog dialog = new JDialog(this, "Pubblica una Nuova Canzone", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.getContentPane().setBackground(Color.decode("#F3E8FF"));

        JTextField txtTitolo = new JTextField();
        JTextField txtDurata = new JTextField();
        JTextField txtGenere = new JTextField();

        JButton btnImg = new JButton("Carica Copertina");
        JButton btnAudio = new JButton("Carica MP3");
        String[] percorsiScelti = new String[]{null, null};

        btnImg.addActionListener(e -> {
            percorsiScelti[0] = GestoreFile.scegliESalvaFile(dialog, "IMMAGINE", "copertine");
            if (percorsiScelti[0] != null) btnImg.setText("Img OK!");
        });
        btnAudio.addActionListener(e -> {
            percorsiScelti[1] = GestoreFile.scegliESalvaFile(dialog, "AUDIO", "musica");
            if (percorsiScelti[1] != null) btnAudio.setText("MP3 OK!");
        });

        JButton btnAnnulla = new JButton("Annulla");
        JButton btnSalva = new JButton("Pubblica");

        dialog.add(new JLabel("  Titolo:"));
        dialog.add(txtTitolo);
        dialog.add(new JLabel("  Durata (sec):"));
        dialog.add(txtDurata);
        dialog.add(new JLabel("  Genere:"));
        dialog.add(txtGenere);
        dialog.add(btnImg);
        dialog.add(btnAudio);
        dialog.add(btnAnnulla);
        dialog.add(btnSalva);

        btnAnnulla.addActionListener(e -> dialog.dispose());
        btnSalva.addActionListener(e -> {
            try {
                int durata = Integer.parseInt(txtDurata.getText().trim());
                libreriaController.pubblicaNuovoBrano(txtTitolo.getText(), durata, txtGenere.getText(), percorsiScelti[0], percorsiScelti[1], utenteLoggato.getIdUtente(), utenteLoggato.getUsername());
                JOptionPane.showMessageDialog(dialog, "Brano caricato con successo!", "Completato", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La durata deve essere un numero valido.", "Errore Formato", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException sqlEx) {
                JOptionPane.showMessageDialog(dialog, "Errore Database:\n" + sqlEx.getMessage(), "Errore SQL", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Errore generico:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    private String formattaTempo(int sec) {
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    private void commutaPannelloVisibile(String c) {
        if (c.equals("PROFILO")) mostraProfilo();
        ((CardLayout) pannelloPagine.getLayout()).show(pannelloPagine, c);
    }

    private ImageIcon renderizzaImmagineVettoriale(String percorso, int risX, int risY, Shape perimetro) {
        try {
            BufferedImage originale;
            if (percorso == null || percorso.isEmpty()) {
                return generaPlaceholderDiEmergenza(risX, risY, perimetro);
            }
            if (percorso.startsWith("/")) {
                URL ref = getClass().getResource(percorso);
                if (ref == null) return generaPlaceholderDiEmergenza(risX, risY, perimetro);
                originale = ImageIO.read(ref);
            } else {
                File fileDisco = new File(percorso);
                if (!fileDisco.exists()) return generaPlaceholderDiEmergenza(risX, risY, perimetro);
                originale = ImageIO.read(fileDisco);
            }
            int min = Math.min(originale.getWidth(), originale.getHeight());
            Image scalata = originale.getSubimage((originale.getWidth() - min) / 2, (originale.getHeight() - min) / 2, min, min).getScaledInstance(risX, risY, Image.SCALE_SMOOTH);
            BufferedImage canvas = new BufferedImage(risX, risY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.drawImage(scalata, 0, 0, null);
            g2.dispose();
            BufferedImage finale = new BufferedImage(risX, risY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g3 = finale.createGraphics();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setClip(perimetro);
            g3.drawImage(canvas, 0, 0, null);
            g3.dispose();
            return new ImageIcon(finale);
        } catch (Exception e) {
            return generaPlaceholderDiEmergenza(risX, risY, perimetro);
        }
    }

    private ImageIcon generaPlaceholderDiEmergenza(int w, int h, Shape s) {
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.setClip(s);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return new ImageIcon(b);
    }

    private void impostaCopertina(JLabel l, String p, int w, int h, int r) {
        if (l != null) {
            l.setIcon(renderizzaImmagineVettoriale(p, w, h, new RoundRectangle2D.Float(0, 0, w, h, r, r)));
            l.setPreferredSize(new Dimension(w, h));
            l.setText("");
        }
    }

    private void impostaFotoProfilo(AbstractButton b, String p, int d) {
        if (b != null) {
            b.setIcon(renderizzaImmagineVettoriale(p, d, d, new Ellipse2D.Float(0, 0, d, d)));
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setText("");
        }
    }

    private void impostaFotoProfilo(JLabel l, String p, int d) {
        if (l != null) {
            l.setIcon(renderizzaImmagineVettoriale(p, d, d, new Ellipse2D.Float(0, 0, d, d)));
            l.setText("");
        }
    }

    private void assegnaIconaStandard(AbstractButton b, String p, int w, int h) {
        if (b != null && getClass().getResource(p) != null)
            b.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(p)).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)));
    }

    private void inizializzaVistaCerca() {
        if (vistaCerca == null) return;
        vistaCerca.removeAll();
        vistaCerca.setLayout(new BorderLayout());
        vistaCerca.setBackground(Color.decode("#F3E8FF"));

        JPanel searchHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 30));
        searchHeader.setBackground(Color.decode("#F3E8FF"));

        if (campoRicerca == null) campoRicerca = new JTextField();
        campoRicerca.setPreferredSize(new Dimension(400, 40));
        campoRicerca.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JButton btnEseguiRicerca = new JButton("Cerca");
        btnEseguiRicerca.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEseguiRicerca.setBackground(Color.WHITE);
        btnEseguiRicerca.setFocusPainted(false);

        searchHeader.add(campoRicerca);
        searchHeader.add(btnEseguiRicerca);

        pannelloRisultatiRicerca = new JPanel();
        pannelloRisultatiRicerca.setLayout(new BoxLayout(pannelloRisultatiRicerca, BoxLayout.Y_AXIS));
        pannelloRisultatiRicerca.setBackground(Color.decode("#F3E8FF"));

        JScrollPane scrollRicerca = new JScrollPane(pannelloRisultatiRicerca);
        scrollRicerca.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        scrollRicerca.getVerticalScrollBar().setUnitIncrement(16);

        vistaCerca.add(searchHeader, BorderLayout.NORTH);
        vistaCerca.add(scrollRicerca, BorderLayout.CENTER);

        campoRicerca.addActionListener(e -> eseguiRicerca(campoRicerca.getText()));
        btnEseguiRicerca.addActionListener(e -> eseguiRicerca(campoRicerca.getText()));
    }

    private void eseguiRicerca(String query) {
        pannelloRisultatiRicerca.removeAll();
        if (query == null || query.trim().isEmpty()) {
            pannelloRisultatiRicerca.revalidate();
            pannelloRisultatiRicerca.repaint();
            return;
        }

        try {
            List<Brano> resBrani = libreriaController.cercaBrani(query.trim());
            List<Playlist> resPlaylists = libreriaController.cercaPlaylistsPubbliche(query.trim());
            List<Utente> resUtenti = libreriaController.cercaUtenti(query.trim(), utenteLoggato.getIdUtente());

            if (resBrani.isEmpty() && resPlaylists.isEmpty() && resUtenti.isEmpty()) {
                JLabel lblVuoto = new JLabel("Nessun risultato trovato per: \"" + query + "\"");
                lblVuoto.setFont(new Font("SansSerif", Font.ITALIC, 16));
                pannelloRisultatiRicerca.add(lblVuoto);
            } else {
                if (!resBrani.isEmpty()) {
                    JLabel lblTit = new JLabel("Canzoni");
                    lblTit.setFont(new Font("SansSerif", Font.BOLD, 20));
                    lblTit.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                    pannelloRisultatiRicerca.add(lblTit);

                    for (Brano b : resBrani) {
                        JPanel row = creaRigaRicercaBase(
                                b.getUrlImmagine(), b.getTitolo(), b.getGenere() + " • " + formattaTempo(b.getDurataSecondi()), false
                        );
                        JButton btnPlay = new JButton("Ascolta");
                        btnPlay.setBackground(Color.WHITE);
                        btnPlay.addActionListener(e -> {
                            Playlist temp = new Playlist(0, "Ricerca", false, "");
                            temp.aggiungiBrano(b);
                            try {
                                playerController.avviaRiproduzione(temp, utenteLoggato);
                                isPlaying = true;
                                if (btnPausa != null) assegnaIconaStandard(btnPausa, "/pause.png", 20, 20);
                                pannelloPlayer.setVisible(true);
                                aggiornaUIBranoCorrente();
                            } catch (IllegalStateException premiumEx) {
                                JOptionPane.showMessageDialog(this, premiumEx.getMessage(), "Passa a Premium", JOptionPane.WARNING_MESSAGE);
                            } catch (Exception ex) {
                            }
                        });
                        row.add(btnPlay, BorderLayout.EAST);
                        pannelloRisultatiRicerca.add(row);
                    }
                }

                if (!resPlaylists.isEmpty()) {
                    JLabel lblTit = new JLabel("Playlist");
                    lblTit.setFont(new Font("SansSerif", Font.BOLD, 20));
                    lblTit.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
                    pannelloRisultatiRicerca.add(lblTit);

                    for (Playlist p : resPlaylists) {
                        JPanel row = creaRigaRicercaBase(
                                p.getUrlIcona(), p.getNome(), "Playlist Pubblica", false
                        );
                        JButton btnApri = new JButton("Apri");
                        btnApri.setBackground(Color.WHITE);
                        btnApri.addActionListener(e -> apriDettaglioPlaylist(p));
                        row.add(btnApri, BorderLayout.EAST);
                        pannelloRisultatiRicerca.add(row);
                    }
                }

                if (!resUtenti.isEmpty()) {
                    JLabel lblTit = new JLabel("Profili e Artisti");
                    lblTit.setFont(new Font("SansSerif", Font.BOLD, 20));
                    lblTit.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
                    pannelloRisultatiRicerca.add(lblTit);

                    for (Utente u : resUtenti) {
                        JPanel row = creaRigaRicercaBase(
                                u.getUrlFotoProfilo(), u.getUsername(), "Utente SanPiScy", true
                        );

                        JButton btnSegui = new JButton();
                        boolean giaSeguito = libreriaController.isUtenteSeguito(utenteLoggato.getIdUtente(), u.getIdUtente());
                        btnSegui.setText(giaSeguito ? "Smetti di seguire" : "Segui");
                        btnSegui.setBackground(Color.WHITE);
                        btnSegui.setFocusPainted(false);

                        btnSegui.addActionListener(e -> {
                            try {
                                if (libreriaController.isUtenteSeguito(utenteLoggato.getIdUtente(), u.getIdUtente())) {
                                    libreriaController.smettiDiSeguire(utenteLoggato.getIdUtente(), u.getIdUtente());
                                    btnSegui.setText("Segui");
                                } else {
                                    libreriaController.seguiUtente(utenteLoggato.getIdUtente(), u.getIdUtente());
                                    btnSegui.setText("Smetti di seguire");
                                }
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(this, "Errore DB: " + ex.getMessage());
                            }
                        });

                        row.add(btnSegui, BorderLayout.EAST);
                        pannelloRisultatiRicerca.add(row);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore DB: " + ex.getMessage());
        }

        pannelloRisultatiRicerca.revalidate();
        pannelloRisultatiRicerca.repaint();
    }

    private JPanel creaRigaRicercaBase(String imgPath, String title, String sub, boolean circolare) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.decode("#F3E8FF"));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblImg = new JLabel();
        String path = (imgPath != null) ? imgPath : "/album.png";
        if (circolare) {
            path = (imgPath != null) ? imgPath : "/Generic avatar.png";
            impostaFotoProfilo(lblImg, path, 50);
        } else {
            impostaCopertina(lblImg, path, 50, 50, 8);
        }

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.decode("#F3E8FF"));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel lblS = new JLabel(sub);
        lblS.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblS.setForeground(Color.GRAY);
        infoPanel.add(lblT);
        infoPanel.add(lblS);

        row.add(lblImg, BorderLayout.WEST);
        row.add(infoPanel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(Color.decode("#E3D3F8"));
                infoPanel.setBackground(Color.decode("#E3D3F8"));
            }

            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.decode("#F3E8FF"));
                infoPanel.setBackground(Color.decode("#F3E8FF"));
            }
        });
        return row;
    }

    private void apriVistaAlbum() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.decode("#F3E8FF"));
        JLabel l = new JLabel("La tua libreria Album (In Arrivo)", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(Color.DARK_GRAY);
        p.add(l, BorderLayout.CENTER);
        pannelloPagine.add(p, "ALBUM_DINAMICA");
        commutaPannelloVisibile("ALBUM_DINAMICA");
    }

    private void apriBraniPreferiti() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.decode("#F3E8FF"));
        JLabel l = new JLabel("I tuoi Brani Preferiti (In Arrivo)", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(Color.DARK_GRAY);
        p.add(l, BorderLayout.CENTER);
        pannelloPagine.add(p, "PREFERITI_DINAMICA");
        commutaPannelloVisibile("PREFERITI_DINAMICA");
    }

    private JPanel creaCardPlaylist(Playlist p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#F3E8FF"));
        card.setOpaque(true);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblCopertina = new JLabel();
        String icona = (p.getUrlIcona() != null) ? p.getUrlIcona() : "/album.png";
        impostaCopertina(lblCopertina, icona, 180, 180, 20);
        lblCopertina.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitolo = new JLabel(p.getNome());
        lblTitolo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitolo.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                apriDettaglioPlaylist(p);
            }
        });

        card.add(lblCopertina);
        card.add(lblTitolo);
        return card;
    }

    private JPanel creaCardUtente(Utente u) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#F3E8FF"));
        card.setOpaque(true);

        JLabel lblAvatar = new JLabel();
        String icona = (u.getUrlFotoProfilo() != null) ? u.getUrlFotoProfilo() : "/Generic avatar.png";
        impostaFotoProfilo(lblAvatar, icona, 180);
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNome = new JLabel(u.getUsername());
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNome.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        card.add(lblAvatar);
        card.add(lblNome);
        return card;
    }

    private void aggiornaVistaHome() {
        if (vistaHome == null) return;
        vistaHome.removeAll();
        vistaHome.setLayout(new BorderLayout(0, 20));
        vistaHome.setBackground(Color.decode("#F3E8FF"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#F3E8FF"));

        JLabel lblBenvenuto = new JLabel("Ciao, " + utenteLoggato.getUsername() + "!", SwingConstants.LEFT);
        lblBenvenuto.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblBenvenuto.setForeground(Color.DARK_GRAY);
        lblBenvenuto.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));
        headerPanel.add(lblBenvenuto, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Le tue Playlist");
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblSub.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 0));
        headerPanel.add(lblSub, BorderLayout.SOUTH);

        vistaHome.add(headerPanel, BorderLayout.NORTH);

        JPanel griglia = new JPanel(new GridLayout(0, 4, 20, 20));
        griglia.setBackground(Color.decode("#F3E8FF"));
        griglia.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        try {
            List<Playlist> leMiePlaylist = libreriaController.ottieniPlaylistsUtente(utenteLoggato.getIdUtente());
            if (leMiePlaylist.isEmpty()) {
                JLabel lblVuoto = new JLabel("Nessuna playlist. Creane una dalla sezione Playlist!");
                lblVuoto.setFont(new Font("SansSerif", Font.ITALIC, 14));
                griglia.add(lblVuoto);
            } else {
                for (Playlist p : leMiePlaylist) {
                    griglia.add(creaCardPlaylist(p));
                }
            }
        } catch (SQLException ex) {
        }

        JScrollPane scroll = new JScrollPane(griglia);
        scroll.setBorder(null);
        vistaHome.add(scroll, BorderLayout.CENTER);

        vistaHome.revalidate();
        vistaHome.repaint();
    }

    private void mostraProfilo() {
        if (vistaProfilo == null) return;

        try {
            libreriaController.aggiornaDatiSocial(utenteLoggato);
        } catch (SQLException e) {
        }

        vistaProfilo.removeAll();
        vistaProfilo.setLayout(new BorderLayout());
        vistaProfilo.setBackground(Color.decode("#F3E8FF"));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        headerPanel.setBackground(Color.decode("#F3E8FF"));

        JLabel lblFoto = new JLabel();
        String pathFoto = utenteLoggato.getUrlFotoProfilo() != null ? utenteLoggato.getUrlFotoProfilo() : "/Generic avatar.png";
        impostaFotoProfilo(lblFoto, pathFoto, 150);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.decode("#F3E8FF"));

        JLabel lblTop = new JLabel("Profilo");
        lblTop.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblNome = new JLabel(utenteLoggato.getUsername());
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 48));

        List<Playlist> pubbliche = new ArrayList<>();
        try {
            List<Playlist> tutte = libreriaController.ottieniPlaylistsUtente(utenteLoggato.getIdUtente());
            for (Playlist p : tutte) {
                if (p.isPubblica()) pubbliche.add(p);
            }
        } catch (SQLException ex) {
        }

        JLabel lblStats = new JLabel(pubbliche.size() + " playlist pubbliche • " + utenteLoggato.getFollower().size() + " follower • " + utenteLoggato.getSeguiti().size() + " seguiti");
        lblStats.setFont(new Font("SansSerif", Font.BOLD, 14));

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(lblTop);
        infoPanel.add(lblNome);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblStats);

        headerPanel.add(lblFoto);
        headerPanel.add(infoPanel);

        vistaProfilo.add(headerPanel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.decode("#F3E8FF"));

        if (!pubbliche.isEmpty()) {
            JLabel lblTitoloPl = new JLabel("Playlist Pubbliche");
            lblTitoloPl.setFont(new Font("SansSerif", Font.BOLD, 24));
            lblTitoloPl.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 0));
            lblTitoloPl.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(lblTitoloPl);

            JPanel grigliaPl = new JPanel(new GridLayout(0, 4, 20, 20));
            grigliaPl.setBackground(Color.decode("#F3E8FF"));
            grigliaPl.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
            for (Playlist p : pubbliche) {
                grigliaPl.add(creaCardPlaylist(p));
            }
            bodyPanel.add(grigliaPl);
        }

        if (!utenteLoggato.getFollower().isEmpty()) {
            JLabel lblTitoloFollower = new JLabel("I tuoi follower");
            lblTitoloFollower.setFont(new Font("SansSerif", Font.BOLD, 24));
            lblTitoloFollower.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 0));
            lblTitoloFollower.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(lblTitoloFollower);

            JPanel grigliaFollower = new JPanel(new GridLayout(0, 4, 20, 20));
            grigliaFollower.setBackground(Color.decode("#F3E8FF"));
            grigliaFollower.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
            for (Utente fol : utenteLoggato.getFollower()) {
                grigliaFollower.add(creaCardUtente(fol));
            }
            bodyPanel.add(grigliaFollower);
        }

        if (!utenteLoggato.getSeguiti().isEmpty()) {
            JLabel lblTitoloSeguiti = new JLabel("I tuoi seguiti");
            lblTitoloSeguiti.setFont(new Font("SansSerif", Font.BOLD, 24));
            lblTitoloSeguiti.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 0));
            lblTitoloSeguiti.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(lblTitoloSeguiti);

            JPanel grigliaSeguiti = new JPanel(new GridLayout(0, 4, 20, 20));
            grigliaSeguiti.setBackground(Color.decode("#F3E8FF"));
            grigliaSeguiti.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
            for (Utente seg : utenteLoggato.getSeguiti()) {
                grigliaSeguiti.add(creaCardUtente(seg));
            }
            bodyPanel.add(grigliaSeguiti);
        }

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        vistaProfilo.add(scroll, BorderLayout.CENTER);

        vistaProfilo.revalidate();
        vistaProfilo.repaint();
    }

    private void popolaVistaPlaylist() {
        if (grigliaPlaylist == null) {
            grigliaPlaylist = new JPanel();
            if (scrollPlaylist != null) {
                scrollPlaylist.setLayout(new BorderLayout());
                scrollPlaylist.add(grigliaPlaylist, BorderLayout.CENTER);
            }
        }
        grigliaPlaylist.removeAll();
        grigliaPlaylist.setLayout(new GridLayout(0, 4, 20, 20));
        grigliaPlaylist.setBackground(Color.decode("#F3E8FF"));

        try {
            List<Playlist> leMiePlaylist = libreriaController.ottieniPlaylistsUtente(utenteLoggato.getIdUtente());
            if (leMiePlaylist.isEmpty()) {
                JLabel lblVuoto = new JLabel("Nessuna playlist trovata. Creane una nuova!");
                lblVuoto.setFont(new Font("SansSerif", Font.ITALIC, 14));
                grigliaPlaylist.add(lblVuoto);
            } else {
                for (Playlist p : leMiePlaylist) {
                    grigliaPlaylist.add(creaCardPlaylist(p));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Errore caricamento playlist: " + ex.getMessage());
        }

        grigliaPlaylist.revalidate();
        grigliaPlaylist.repaint();

        aggiornaVistaHome();
        mostraProfilo();
    }

    private void apriDettaglioPlaylist(Playlist p) {
        JPanel pannelloDettaglio = new JPanel(new BorderLayout());
        pannelloDettaglio.setBackground(Color.decode("#F3E8FF"));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        headerPanel.setBackground(Color.decode("#E3D3F8"));

        JLabel lblCover = new JLabel();
        String icona = (p.getUrlIcona() != null) ? p.getUrlIcona() : "/album.png";
        impostaCopertina(lblCover, icona, 200, 200, 15);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.decode("#E3D3F8"));

        JLabel lblTipo = new JLabel(p.isPubblica() ? "Playlist pubblica" : "Playlist privata");
        lblTipo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblTitolo = new JLabel(p.getNome());
        lblTitolo.setFont(new Font("SansSerif", Font.BOLD, 48));

        JLabel lblCreatore = new JLabel("Creato da: " + utenteLoggato.getUsername());
        lblCreatore.setFont(new Font("SansSerif", Font.BOLD, 14));

        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(lblTipo);
        infoPanel.add(lblTitolo);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblCreatore);

        headerPanel.add(lblCover);
        headerPanel.add(infoPanel);

        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(Color.decode("#F3E8FF"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        actionPanel.setBackground(Color.decode("#F3E8FF"));

        JButton btnPlayPlaylist = new JButton("PLAY");
        btnPlayPlaylist.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPlayPlaylist.setBackground(new Color(30, 215, 96));
        btnPlayPlaylist.setForeground(Color.WHITE);
        btnPlayPlaylist.setFocusPainted(false);
        btnPlayPlaylist.setPreferredSize(new Dimension(120, 50));
        actionPanel.add(btnPlayPlaylist);

        JButton btnAggiungi = new JButton("Aggiungi Brano");
        btnAggiungi.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAggiungi.setBackground(Color.WHITE);
        btnAggiungi.setFocusPainted(false);
        btnAggiungi.setPreferredSize(new Dimension(150, 40));

        btnAggiungi.addActionListener(e -> {
            try {
                List<Brano> tutti = libreriaController.ottieniTuttiIBrani();
                if (tutti.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nessun brano disponibile nel Database!");
                    return;
                }

                String[] opzioni = tutti.stream().map(br -> br.getIdBrano() + " - " + br.getTitolo() + " (" + br.getGenere() + ")").toArray(String[]::new);
                String scelta = (String) JOptionPane.showInputDialog(this, "Seleziona il brano da inserire:", "Aggiungi Brano", JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);

                if (scelta != null) {
                    int idScelto = Integer.parseInt(scelta.split(" - ")[0]);
                    libreriaController.aggiungiBranoAPlaylist(p.getIdPlaylist(), idScelto);
                    JOptionPane.showMessageDialog(this, "Brano aggiunto con successo!");
                    apriDettaglioPlaylist(p);
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("23505") || ex.getMessage().contains("duplicate key")) {
                    JOptionPane.showMessageDialog(this, "Il brano è già presente nella playlist!", "Attenzione", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Errore DB: " + ex.getMessage());
                }
            }
        });
        actionPanel.add(btnAggiungi);
        bodyPanel.add(actionPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.decode("#F3E8FF"));

        try {
            List<Brano> brani = libreriaController.ottieniBraniPlaylist(p.getIdPlaylist());

            p.getListaBrani().clear();
            for (Brano b : brani) {
                p.aggiungiBrano(b);
            }

            btnPlayPlaylist.addActionListener(e -> {
                if (brani.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La playlist è vuota! Aggiungi dei brani prima di ascoltarla.");
                    return;
                }
                try {
                    playerController.avviaRiproduzione(p, utenteLoggato);
                    isPlaying = true;
                    if (btnPausa != null) assegnaIconaStandard(btnPausa, "/pause.png", 20, 20);
                    pannelloPlayer.setVisible(true);
                    aggiornaUIBranoCorrente();
                } catch (IllegalStateException premiumEx) {
                    JOptionPane.showMessageDialog(this, premiumEx.getMessage(), "Passa a Premium", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                }
            });

            if (brani.isEmpty()) {
                JLabel lblVuoto = new JLabel("   Nessun brano salvato in questa playlist.");
                lblVuoto.setFont(new Font("SansSerif", Font.ITALIC, 14));
                listPanel.add(lblVuoto);
            } else {
                int i = 1;
                for (Brano b : brani) {
                    JPanel row = new JPanel(new BorderLayout());
                    row.setBackground(Color.decode("#F3E8FF"));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                    row.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

                    JLabel lblNum = new JLabel(String.valueOf(i++) + "   ");
                    JLabel lblBranoInfo = new JLabel("<html><b>" + b.getTitolo() + "</b><br><span style='color:gray; font-size:10px;'>" + b.getGenere() + "</span></html>");
                    JLabel lblDurata = new JLabel(formattaTempo(b.getDurataSecondi()));

                    row.add(lblNum, BorderLayout.WEST);
                    row.add(lblBranoInfo, BorderLayout.CENTER);
                    row.add(lblDurata, BorderLayout.EAST);

                    row.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            row.setBackground(Color.decode("#E3D3F8"));
                        }

                        public void mouseExited(MouseEvent e) {
                            row.setBackground(Color.decode("#F3E8FF"));
                        }
                    });
                    listPanel.add(row);
                }
            }
        } catch (SQLException ex) {
            listPanel.add(new JLabel(" Errore di caricamento brani dal Database."));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        pannelloDettaglio.add(headerPanel, BorderLayout.NORTH);
        pannelloDettaglio.add(bodyPanel, BorderLayout.CENTER);

        pannelloPagine.add(pannelloDettaglio, "DETTAGLIO_PLAYLIST");
        commutaPannelloVisibile("DETTAGLIO_PLAYLIST");
    }

    private void createUIComponents() {
    }

    public Home(Utente u) {
        this.utenteLoggato = u;

        this.playerController = new PlayerController();
        this.libreriaController = new LibreriaController();

        setContentPane(pannelloPrincipale);
        setTitle("SanPiScy Music - " + utenteLoggato.getUsername());
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (lblUsernameProfilo != null) {
            lblUsernameProfilo.setText(utenteLoggato.getUsername());
        }

        pannelloPrincipale.removeAll();
        pannelloPrincipale.setLayout(new BorderLayout());

        pannelloPrincipale.add(pannelloSidebar, BorderLayout.WEST);

        JPanel pannelloCentraleConTopBar = new JPanel(new BorderLayout());
        if (topBar != null) pannelloCentraleConTopBar.add(topBar, BorderLayout.NORTH);

        if (pannelloPagine != null) {
            pannelloPagine.removeAll();
            if (vistaHome != null) pannelloPagine.add(vistaHome, "HOME");
            if (vistaPlaylist != null) pannelloPagine.add(vistaPlaylist, "PLAYLIST");
            if (vistaCerca != null) pannelloPagine.add(vistaCerca, "CERCA");
            if (vistaProfilo != null) pannelloPagine.add(vistaProfilo, "PROFILO");
            if (vistaPlayer != null) pannelloPagine.add(vistaPlayer, "PLAYER");
            pannelloCentraleConTopBar.add(pannelloPagine, BorderLayout.CENTER);
        }

        pannelloPrincipale.add(pannelloCentraleConTopBar, BorderLayout.CENTER);

        if (pannelloPlayer != null) {
            pannelloPlayer.setBackground(Color.decode("#E3D3F8"));
            pannelloPlayer.setPreferredSize(new Dimension(0, 90));
            pannelloPrincipale.add(pannelloPlayer, BorderLayout.SOUTH);
            pannelloPlayer.setVisible(false);
        }

        if (vistaPlayer != null) {
            vistaPlayer.removeAll();
            vistaPlayer.setBackground(Color.decode("#F3E8FF"));
            vistaPlayer.setLayout(new BorderLayout());
            if (lblCopertinaGrande != null) {
                lblCopertinaGrande.setHorizontalAlignment(SwingConstants.CENTER);
                lblCopertinaGrande.setVerticalAlignment(SwingConstants.CENTER);
                vistaPlayer.add(lblCopertinaGrande, BorderLayout.CENTER);
            }
        }

        inizializzaVistaCerca();

        pannelloPrincipale.revalidate();
        pannelloPrincipale.repaint();

        if (vistaHome != null) vistaHome.setBackground(Color.decode("#F3E8FF"));
        if (vistaPlaylist != null) vistaPlaylist.setBackground(Color.decode("#F3E8FF"));
        if (vistaProfilo != null) vistaProfilo.setBackground(Color.decode("#F3E8FF"));
        if (vistaCerca != null) vistaCerca.setBackground(Color.decode("#F3E8FF"));
        if (pannelloSidebar != null) pannelloSidebar.setBackground(Color.decode("#E3D3F8"));

        if (sliderProgresso != null) {
            sliderProgresso.setOpaque(false);
            sliderProgresso.setValue(0);
            sliderProgresso.setUI(new SliderUI(sliderProgresso));
        }

        if (sliderVolume != null) {
            sliderVolume.setOpaque(false);
            sliderVolume.setMinimum(0);
            sliderVolume.setMaximum(100);
            sliderVolume.setValue(50);
            sliderVolume.setUI(new SliderUI(sliderVolume));

            sliderVolume.addChangeListener(e -> {
                int val = sliderVolume.getValue();
                playerController.impostaVolume(val);
            });
        }

        assegnaIconaStandard(btnHome, "/home.png", 20, 20);
        assegnaIconaStandard(cercaButton, "/search.png", 20, 20);
        assegnaIconaStandard(btnPlaylist, "/list.png", 20, 20);
        assegnaIconaStandard(btnCanzoni, "/music.png", 20, 20);

        if (btnLogo != null) {
            btnLogo.setContentAreaFilled(false);
            btnLogo.setBorderPainted(false);
            btnLogo.setFocusPainted(false);
            btnLogo.setOpaque(false);
            assegnaIconaStandard(btnLogo, "/logo.png", 80, 50);
        }

        if (iconaVolume != null) {
            iconaVolume.setContentAreaFilled(false);
            iconaVolume.setBorderPainted(false);
            assegnaIconaStandard(iconaVolume, "/volume.png", 20, 20);
        }

        String fotoUtente = utenteLoggato.getUrlFotoProfilo() != null ? utenteLoggato.getUrlFotoProfilo() : "/Generic avatar.png";
        impostaFotoProfilo(btnProfilo, fotoUtente, 35);
        impostaFotoProfilo(fotoProfiloGrande, fotoUtente, 150);

        popolaVistaPlaylist();

        if (btnNuovaPlaylist != null) {
            btnNuovaPlaylist.addActionListener(e -> mostraCreazionePlaylist());
        }

        if (btnPausa != null) {
            assegnaIconaStandard(btnPausa, "/play.png", 20, 20);
            btnPausa.addActionListener(e -> {
                if (isPlaying) {
                    if (timerRiproduzione != null) timerRiproduzione.stop();
                    playerController.mettiInPausa();
                    assegnaIconaStandard(btnPausa, "/play.png", 20, 20);
                } else {
                    if (timerRiproduzione != null) timerRiproduzione.start();
                    playerController.riprendiRiproduzione();
                    assegnaIconaStandard(btnPausa, "/pause.png", 20, 20);
                }
                isPlaying = !isPlaying;
            });
        }

        if (sliderProgresso != null) {
            sliderProgresso.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    int nuovoSecondo = sliderProgresso.getValue();
                    secondiCorrenti = nuovoSecondo;
                    lblTempoAttuale.setText(formattaTempo(secondiCorrenti));
                    playerController.saltaA(nuovoSecondo);
                }
            });
        }

        if (btnCaricaBrano != null) {
            assegnaIconaStandard(btnCaricaBrano, "/music.png", 20, 20);

            try {
                if (libreriaController.isArtista(utenteLoggato.getIdUtente())) {
                    btnCaricaBrano.setVisible(true);
                    btnCaricaBrano.addActionListener(e -> mostraDialogCaricamentoBrano());
                } else {
                    btnCaricaBrano.setVisible(false);
                }
            } catch (SQLException ex) {
                System.err.println("Errore nel controllo artista: " + ex.getMessage());
                btnCaricaBrano.setVisible(false);
            }
        }


        if (btnHome != null) btnHome.addActionListener(e -> commutaPannelloVisibile("HOME"));
        if (btnPlaylist != null) btnPlaylist.addActionListener(e -> commutaPannelloVisibile("PLAYLIST"));
        if (cercaButton != null) cercaButton.addActionListener(e -> commutaPannelloVisibile("CERCA"));
        if (btnProfilo != null) btnProfilo.addActionListener(e -> commutaPannelloVisibile("PROFILO"));

        if (btnCanzoni != null) {
            btnCanzoni.addActionListener(e -> {
                caricaBraniDb();
                commutaPannelloVisibile("PLAYER");
            });
        }

        if (btnPlaylist2 != null) btnPlaylist2.addActionListener(e -> commutaPannelloVisibile("PLAYLIST"));
        if (btnAlbum != null) btnAlbum.addActionListener(e -> apriVistaAlbum());
        if (btnBraniPreferiti != null) btnBraniPreferiti.addActionListener(e -> apriBraniPreferiti());

        if (btnAvanti != null) {
            btnAvanti.addActionListener(e -> {
                try {
                    playerController.cliccatoAvantiOSkip();
                    aggiornaUIBranoCorrente();
                } catch (Exception ex) {
                }
            });
        }

        if (btnIndietro != null) {
            btnIndietro.addActionListener(e -> {
                playerController.cliccatoIndietro();
                aggiornaUIBranoCorrente();
            });
        }
    }
}