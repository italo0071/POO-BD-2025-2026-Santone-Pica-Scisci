package dao;

import db.DatabaseConnection;
import model.Utente;
import model.UtenteFree;
import model.UtentePremium;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {

    public static boolean registraUtenteFree(String username, String email, String password, String urlFotoProfilo) {
        String sql = "INSERT INTO UTENTE (Username, Email, Password, TipoUtente, SkipRimanenti, UrlFotoProfilo) VALUES (?, ?, ?, 'FREE', 3, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, urlFotoProfilo != null ? urlFotoProfilo : "/Generic avatar.png");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ERRORE SQL DURANTE LA REGISTRAZIONE");
            System.err.println("Messaggio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Utente eseguiLogin(String email, String password) {
        String sql = "SELECT * FROM UTENTE WHERE Email = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("TipoUtente");
                    int id = rs.getInt("ID_Utente");
                    String user = rs.getString("Username");
                    String mail = rs.getString("Email");
                    String pass = rs.getString("Password");

                    String foto = "/Generic avatar.png";
                    try {
                        foto = rs.getString("UrlFotoProfilo");
                    } catch (SQLException ex) { }

                    Utente utenteEstratto;
                    if ("FREE".equals(tipo)) {
                        UtenteFree uf = new UtenteFree(id, "", "", user, mail, pass, 0);
                        uf.setSkipMassimi(rs.getInt("SkipRimanenti"));
                        utenteEstratto = uf;
                    } else {
                        utenteEstratto = new UtentePremium(id, "", "", user, mail, pass, null, "");
                    }
                    utenteEstratto.setUrlFotoProfilo(foto);
                    return utenteEstratto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Utente> cercaUtenti(String queryRicerca, int idEscluso) throws SQLException {
        List<Utente> lista = new ArrayList<>();
        String sql = "SELECT u.ID_Utente, u.Username, u.UrlFotoProfilo, a.NomeDArte " +
                "FROM UTENTE u LEFT JOIN ARTISTA a ON u.ID_Utente = a.FK_Utente " +
                "WHERE (u.Username ILIKE ? OR a.NomeDArte ILIKE ?) AND u.ID_Utente != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String param = "%" + queryRicerca + "%";
            pstmt.setString(1, param);
            pstmt.setString(2, param);
            pstmt.setInt(3, idEscluso);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UtenteFree u = new UtenteFree(rs.getInt("ID_Utente"), "", "", rs.getString("Username"), "", "", 0);
                    u.setUrlFotoProfilo(rs.getString("UrlFotoProfilo"));

                    if (rs.getString("NomeDArte") != null) {
                        u.setUsername(rs.getString("NomeDArte") + " (" + rs.getString("Username") + ")");
                    }
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    public static boolean isSeguito(int idFollower, int idSeguito) throws SQLException {
        String sql = "SELECT 1 FROM FOLLOW WHERE FK_Follower = ? AND FK_Seguito = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFollower);
            pstmt.setInt(2, idSeguito);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void seguiUtente(int idFollower, int idSeguito) throws SQLException {
        String sql = "INSERT INTO FOLLOW (FK_Follower, FK_Seguito) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFollower);
            pstmt.setInt(2, idSeguito);
            pstmt.executeUpdate();
        }
    }

    public static void smettiDiSeguire(int idFollower, int idSeguito) throws SQLException {
        String sql = "DELETE FROM FOLLOW WHERE FK_Follower = ? AND FK_Seguito = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFollower);
            pstmt.setInt(2, idSeguito);
            pstmt.executeUpdate();
        }
    }

    public static void caricaSeguiti(Utente utente) throws SQLException {
        utente.getSeguiti().clear();
        String sql = "SELECT u.ID_Utente, u.Username, u.UrlFotoProfilo " +
                "FROM FOLLOW f JOIN UTENTE u ON f.FK_Seguito = u.ID_Utente " +
                "WHERE f.FK_Follower = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utente.getIdUtente());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UtenteFree u = new UtenteFree(rs.getInt("ID_Utente"), "", "", rs.getString("Username"), "", "", 0);
                    u.setUrlFotoProfilo(rs.getString("UrlFotoProfilo"));
                    utente.seguiUtente(u);
                }
            }
        }
    }

    public static void caricaFollower(Utente utente) throws SQLException {
        utente.getFollower().clear();
        String sql = "SELECT u.ID_Utente, u.Username, u.UrlFotoProfilo " +
                "FROM FOLLOW f JOIN UTENTE u ON f.FK_Follower = u.ID_Utente " +
                "WHERE f.FK_Seguito = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utente.getIdUtente());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UtenteFree u = new UtenteFree(rs.getInt("ID_Utente"), "", "", rs.getString("Username"), "", "", 0);
                    u.setUrlFotoProfilo(rs.getString("UrlFotoProfilo"));
                    utente.aggiungiFollower(u);
                }
            }
        }
    }
    public boolean checkSeArtista(int idUtente) throws SQLException {
        String query = "SELECT ID_Artista FROM ARTISTA WHERE FK_Utente = ?";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtente);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}