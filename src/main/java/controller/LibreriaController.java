package controller;

import dao.BranoDAO;
import dao.PlaylistDAO;
import dao.UtenteDAO;
import model.Brano;
import model.Playlist;
import model.Utente;

import java.sql.SQLException;
import java.util.List;

public class LibreriaController {

    //Gestione Brani
    public List<Brano> ottieniTuttiIBrani() throws SQLException {
        return BranoDAO.getTuttiIBrani();
    }

    public void pubblicaNuovoBrano(String titolo, int durata, String genere, String urlImg, String urlAudio, int idUtente, String usernameUtente) throws SQLException {
        BranoDAO.inserisciBrano(titolo, durata, genere, urlImg, urlAudio, idUtente, usernameUtente);
    }

    //Gestione Playlist
    public void creaNuovaPlaylist(String nome, int idUtente, String urlIcona) throws SQLException {
        PlaylistDAO.creaPlaylist(nome, idUtente, urlIcona);
    }

    public List<Playlist> ottieniPlaylistsUtente(int idUtente) throws SQLException {
        return PlaylistDAO.getPlaylistsDiUtente(idUtente);
    }

    public List<Brano> ottieniBraniPlaylist(int idPlaylist) throws SQLException {
        return PlaylistDAO.getBraniDiPlaylist(idPlaylist);
    }

    public void aggiungiBranoAPlaylist(int idPlaylist, int idBrano) throws SQLException {
        PlaylistDAO.aggiungiBranoAPlaylist(idPlaylist, idBrano);
    }

    //Ricerca
    public List<Brano> cercaBrani(String query) throws SQLException {
        return BranoDAO.cercaBrani(query);
    }

    public List<Playlist> cercaPlaylistsPubbliche(String query) throws SQLException {
        return PlaylistDAO.cercaPlaylistsPubbliche(query);
    }

    public List<Utente> cercaUtenti(String query, int idEscluso) throws SQLException {
        return UtenteDAO.cercaUtenti(query, idEscluso);
    }

    //Gestione Follower
    public boolean isUtenteSeguito(int idFollower, int idSeguito) throws SQLException {
        return UtenteDAO.isSeguito(idFollower, idSeguito);
    }

    public void seguiUtente(int idFollower, int idSeguito) throws SQLException {
        UtenteDAO.seguiUtente(idFollower, idSeguito);
    }

    public void smettiDiSeguire(int idFollower, int idSeguito) throws SQLException {
        UtenteDAO.smettiDiSeguire(idFollower, idSeguito);
    }

    public void aggiornaDatiSocial(Utente utente) throws SQLException {
        UtenteDAO.caricaSeguiti(utente);
        UtenteDAO.caricaFollower(utente);
    }

    public boolean isArtista(int idUtente) throws SQLException {
        UtenteDAO utenteDao = new UtenteDAO();
        return utenteDao.checkSeArtista(idUtente);
    }
}
