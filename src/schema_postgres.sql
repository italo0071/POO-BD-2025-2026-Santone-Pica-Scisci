DROP TABLE IF EXISTS CRONOLOGIA CASCADE;
DROP TABLE IF EXISTS CONTENUTO_PLAYLIST CASCADE;
DROP TABLE IF EXISTS PLAYLIST CASCADE;
DROP TABLE IF EXISTS BRANO CASCADE;
DROP TABLE IF EXISTS ALBUM CASCADE;
DROP TABLE IF EXISTS ARTISTA CASCADE;
DROP TABLE IF EXISTS UTENTE CASCADE;

CREATE TABLE UTENTE (
                        ID_Utente SERIAL PRIMARY KEY,
                        Username VARCHAR(50) NOT NULL UNIQUE,
                        Email VARCHAR(100) NOT NULL UNIQUE,
                        Password VARCHAR(255) NOT NULL,
                        TipoUtente VARCHAR(10) NOT NULL DEFAULT 'FREE',
                        SkipRimanenti INT DEFAULT 3,
                        DataScadenzaPremium DATE,
                        UrlFotoProfilo VARCHAR(255) DEFAULT '/Generic avatar.png',

                        CONSTRAINT CHK_TipoUtente CHECK (TipoUtente IN ('FREE', 'PREMIUM')),
                        CONSTRAINT CHK_SkipRimanenti CHECK (SkipRimanenti >= 0)
);

CREATE TABLE ARTISTA (
                         ID_Artista SERIAL PRIMARY KEY,
                         NomeDArte VARCHAR(100) NOT NULL,
                         Biografia TEXT,
                         FK_Utente INT UNIQUE,

                         FOREIGN KEY (FK_Utente) REFERENCES UTENTE(ID_Utente) ON DELETE SET NULL
);

CREATE TABLE ALBUM (
                       ID_Album SERIAL PRIMARY KEY,
                       Titolo VARCHAR(100) NOT NULL,
                       AnnoUscita INT CHECK (AnnoUscita > 1900),
                       UrlCopertina VARCHAR(255),
                       FK_Artista INT NOT NULL,

                       FOREIGN KEY (FK_Artista) REFERENCES ARTISTA(ID_Artista) ON DELETE CASCADE
);

CREATE TABLE BRANO (
                       ID_Brano SERIAL PRIMARY KEY,
                       Titolo VARCHAR(100) NOT NULL,
                       DurataSecondi INT NOT NULL CHECK (DurataSecondi > 0),
                       Genere VARCHAR(50),
                       UrlImmagine VARCHAR(255),
                       FK_Artista INT NOT NULL,
                       FK_Album INT,
                       UrlFileAudio VARCHAR(255),

                       FOREIGN KEY (FK_Artista) REFERENCES ARTISTA(ID_Artista) ON DELETE CASCADE,
                       FOREIGN KEY (FK_Album) REFERENCES ALBUM(ID_Album) ON DELETE SET NULL
);

CREATE TABLE PLAYLIST (
                          ID_Playlist SERIAL PRIMARY KEY,
                          Nome VARCHAR(100) NOT NULL,
                          IsPubblica BOOLEAN DEFAULT TRUE,
                          UrlIcona VARCHAR(255),
                          FK_Utente INT NOT NULL,

                          FOREIGN KEY (FK_Utente) REFERENCES UTENTE(ID_Utente) ON DELETE CASCADE
);

CREATE TABLE CONTENUTO_PLAYLIST (
                                    FK_Playlist INT NOT NULL,
                                    FK_Brano INT NOT NULL,
                                    Posizione INT NOT NULL CHECK (Posizione > 0),

                                    PRIMARY KEY (FK_Playlist, FK_Brano),
                                    FOREIGN KEY (FK_Playlist) REFERENCES PLAYLIST(ID_Playlist) ON DELETE CASCADE,
                                    FOREIGN KEY (FK_Brano) REFERENCES BRANO(ID_Brano) ON DELETE CASCADE
);

CREATE TABLE CRONOLOGIA (
                            ID_Ascolto SERIAL PRIMARY KEY,
                            DataOra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            SecondiAscoltati INT NOT NULL CHECK (SecondiAscoltati >= 0),
                            IsOnDemand BOOLEAN DEFAULT FALSE,
                            FK_Utente INT NOT NULL,
                            FK_Brano INT NOT NULL,

                            FOREIGN KEY (FK_Utente) REFERENCES UTENTE(ID_Utente) ON DELETE CASCADE,
                            FOREIGN KEY (FK_Brano) REFERENCES BRANO(ID_Brano) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION fn_verifica_e_decrementa_skip()
    RETURNS TRIGGER AS $$
DECLARE
    v_tipoUtente VARCHAR(10);
    v_skipRimanenti INT;
    v_durataBrano INT;
BEGIN
    SELECT TipoUtente, SkipRimanenti INTO v_tipoUtente, v_skipRimanenti
    FROM UTENTE
    WHERE ID_Utente = NEW.FK_Utente;

    SELECT DurataSecondi INTO v_durataBrano
    FROM BRANO
    WHERE ID_Brano = NEW.FK_Brano;

    IF NEW.SecondiAscoltati > v_durataBrano THEN
        NEW.SecondiAscoltati := v_durataBrano;
    END IF;

    IF v_tipoUtente = 'FREE' AND NEW.SecondiAscoltati < v_durataBrano THEN
        IF v_skipRimanenti <= 0 THEN
            RAISE EXCEPTION 'Errore DB: Impossibile registrare lo skip. L''utente Free ha esaurito gli skip disponibili.';
        ELSE
            UPDATE UTENTE
            SET SkipRimanenti = SkipRimanenti - 1
            WHERE ID_Utente = NEW.FK_Utente;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER TRG_VerificaEDecrementaSkip
    BEFORE INSERT ON CRONOLOGIA
    FOR EACH ROW
EXECUTE FUNCTION fn_verifica_e_decrementa_skip();

