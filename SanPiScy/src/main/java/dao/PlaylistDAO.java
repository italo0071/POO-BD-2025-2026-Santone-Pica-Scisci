package dao;

import db.DatabaseConnection;
import model.Brano;
import model.Playlist;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {

    public static void creaPlaylist(String nome, int idUtente, String urlIcona) throws SQLException {
        String query = "INSERT INTO PLAYLIST (Nome, FK_Utente, UrlIcona) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nome);
            pstmt.setInt(2, idUtente);
            pstmt.setString(3, urlIcona);
            pstmt.executeUpdate();
        }
    }

    public static List<Playlist> getPlaylistsDiUtente(int idUtente) throws SQLException {
        List<Playlist> lista = new ArrayList<>();
        String query = "SELECT ID_Playlist, Nome, IsPubblica, UrlIcona FROM PLAYLIST WHERE FK_Utente = ? ORDER BY ID_Playlist DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idUtente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Playlist p = new Playlist(
                            rs.getInt("ID_Playlist"),
                            rs.getString("Nome"),
                            rs.getBoolean("IsPubblica"),
                            rs.getString("UrlIcona")
                    );
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public static List<Brano> getBraniDiPlaylist(int idPlaylist) throws SQLException {
        List<Brano> brani = new ArrayList<>();
        String query = "SELECT b.* FROM BRANO b JOIN CONTENUTO_PLAYLIST cp ON b.ID_Brano = cp.FK_Brano WHERE cp.FK_Playlist = ? ORDER BY cp.Posizione ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idPlaylist);
            try (ResultSet rs = pstmt.executeQuery()) {
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
        }
        return brani;
    }

    public static void aggiungiBranoAPlaylist(int idPlaylist, int idBrano) throws SQLException {
        String getMaxPos = "SELECT COALESCE(MAX(Posizione), 0) FROM CONTENUTO_PLAYLIST WHERE FK_Playlist = ?";
        String insert = "INSERT INTO CONTENUTO_PLAYLIST (FK_Playlist, FK_Brano, Posizione) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psMax = conn.prepareStatement(getMaxPos)) {
            psMax.setInt(1, idPlaylist);
            try (ResultSet rs = psMax.executeQuery()) {
                int pos = 1;
                if (rs.next()) { pos = rs.getInt(1) + 1; }

                try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                    psIns.setInt(1, idPlaylist);
                    psIns.setInt(2, idBrano);
                    psIns.setInt(3, pos);
                    psIns.executeUpdate();
                }
            }
        }
    }

    public static List<Playlist> cercaPlaylistsPubbliche(String queryRicerca) throws SQLException {
        List<Playlist> lista = new ArrayList<>();
        String sql = "SELECT ID_Playlist, Nome, IsPubblica, UrlIcona FROM PLAYLIST WHERE Nome ILIKE ? AND IsPubblica = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + queryRicerca + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Playlist(
                            rs.getInt("ID_Playlist"),
                            rs.getString("Nome"),
                            rs.getBoolean("IsPubblica"),
                            rs.getString("UrlIcona")
                    ));
                }
            }
        }
        return lista;
    }
}