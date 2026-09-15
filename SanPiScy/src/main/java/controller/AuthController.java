package controller;

import dao.UtenteDAO;
import model.Utente;

public class AuthController {

    public Utente effettuaLogin(String email, String password) {
        return UtenteDAO.eseguiLogin(email, password);
    }

    public boolean registraNuovoUtente(String username, String email, String password, String urlFotoProfilo) {
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }
        return UtenteDAO.registraUtenteFree(username, email, password, urlFotoProfilo);
    }
}