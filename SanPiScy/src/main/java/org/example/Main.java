package org.example;

import db.DatabaseConnection;
import gui.AuthWindow;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Avvio di SanPiScy Music...");

        // Test della connessione al DB all'avvio
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Connessione al database PostgreSQL avvenuta con successo!");
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione al DB. Verifica URL, utente e password.");
            e.printStackTrace();
        }

        // Avvio dell'interfaccia grafica
        SwingUtilities.invokeLater(() -> new AuthWindow().setVisible(true));
    }
}