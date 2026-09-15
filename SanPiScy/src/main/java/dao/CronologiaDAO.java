package dao;

import db.DatabaseConnection;
import exception.SkipEsauritiException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CronologiaDAO {

    public static void inserisciAscolto(int idUtente, int idBrano, int secondiAscoltati, boolean isOnDemand) throws SkipEsauritiException {
        String sql = "INSERT INTO CRONOLOGIA (SecondiAscoltati, IsOnDemand, FK_Utente, FK_Brano) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, secondiAscoltati);
            stmt.setBoolean(2, isOnDemand);
            stmt.setInt(3, idUtente);
            stmt.setInt(4, idBrano);

            stmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("Impossibile registrare lo skip")) {
                throw new SkipEsauritiException("Limite raggiunto: l'utente ha esaurito gli skip disponibili.");
            } else {
                System.err.println("Errore di inserimento nel DB: " + e.getMessage());
            }
        }
    }
}