package dao;

import db.DatabaseConnection;
import model.Brano;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranoDAO {

    public static List<Brano> getTuttiIBrani() throws SQLException {
        List<Brano> brani = new ArrayList<>();
        String sql = "SELECT * FROM BRANO";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Brano b = new Brano(
                        rs.getInt("ID_Brano"),
                        rs.getString("Titolo"),
                        rs.getInt("DurataSecondi"),
                        rs.getString("Genere"),
                        rs.getString("UrlImmagine"),
                        rs.getString("UrlFileAudio")
                );
                brani.add(b);
            }
        }
        return brani;
    }

    public static List<Brano> cercaBrani(String queryRicerca) throws SQLException {
        List<Brano> risultati = new ArrayList<>();
        String sql = "SELECT b.* FROM BRANO b " +
                "LEFT JOIN ARTISTA a ON b.FK_Artista = a.ID_Artista " +
                "WHERE b.Titolo ILIKE ? OR a.NomeDArte ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            String parametro = "%" + queryRicerca + "%";
            stmt.setString(1, parametro);
            stmt.setString(2, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Brano b = new Brano(
                            rs.getInt("ID_Brano"),
                            rs.getString("Titolo"),
                            rs.getInt("DurataSecondi"),
                            rs.getString("Genere"),
                            rs.getString("UrlImmagine"),
                            rs.getString("UrlFileAudio")
                    );
                    risultati.add(b);
                }
            }
        }
        return risultati;
    }

    public static void inserisciBrano(String titolo, int durata, String genere, String urlImg, String urlAudio, int idUtente, String usernameUtente) throws SQLException {
        String checkArtista = "SELECT ID_Artista FROM ARTISTA WHERE FK_Utente = ?";
        String insertArtista = "INSERT INTO ARTISTA (NomeDArte, FK_Utente) VALUES (?, ?)";
        String insertBrano = "INSERT INTO BRANO (Titolo, DurataSecondi, Genere, UrlImmagine, UrlFileAudio, FK_Artista) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int idArtista = -1;

            try (PreparedStatement stmtCheck = conn.prepareStatement(checkArtista)) {
                stmtCheck.setInt(1, idUtente);
                try (ResultSet rs = stmtCheck.executeQuery()) {
                    if (rs.next()) idArtista = rs.getInt("ID_Artista");
                }
            }

            if (idArtista == -1) {
                try (PreparedStatement stmtInsertArt = conn.prepareStatement(insertArtista, Statement.RETURN_GENERATED_KEYS)) {
                    stmtInsertArt.setString(1, usernameUtente + " (Artist)");
                    stmtInsertArt.setInt(2, idUtente);
                    stmtInsertArt.executeUpdate();
                    try (ResultSet rsArt = stmtInsertArt.getGeneratedKeys()) {
                        if (rsArt.next()) idArtista = rsArt.getInt(1);
                    }
                }
            }

            if (idArtista != -1) {
                try (PreparedStatement stmtInsertBrano = conn.prepareStatement(insertBrano)) {
                    stmtInsertBrano.setString(1, titolo);
                    stmtInsertBrano.setInt(2, durata);
                    stmtInsertBrano.setString(3, genere);
                    stmtInsertBrano.setString(4, urlImg);
                    stmtInsertBrano.setString(5, urlAudio);
                    stmtInsertBrano.setInt(6, idArtista);

                    stmtInsertBrano.executeUpdate();
                }
            } else {
                throw new SQLException("Impossibile determinare o creare l'ID Artista per questo utente.");
            }
        }
    }
}