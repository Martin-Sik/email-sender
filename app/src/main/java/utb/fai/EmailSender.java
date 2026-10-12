package utb.fai;

import java.net.*;
import java.io.*;

public class EmailSender {

    private Socket socket;
    private InputStream vstup;
    private OutputStream vystup;

    /*
     * Constructor opens Socket to host/port. If the Socket throws an exception
     * during opening,
     * the exception is not handled in the constructor.
     */
    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        this.vstup = socket.getInputStream();
        this.vystup = socket.getOutputStream();
        
        prectiOdpoved();
    }

    /*
     * Sends email from an email address to an email address with some subject and
     * text.
     * If the Socket throws an exception during sending, the exception is not
     * handled by this method.
     */
    public void send(String from, String to, String subject, String text) throws IOException {
        // 1. Pozdrav serveru
        odesliPrikaz("EHLO localhost\r\n");
        
        // 2. Nastaveni odesilatele
        odesliPrikaz("MAIL FROM:<" + from + ">\r\n");
        
        // 3. Nastaveni prijemce
        odesliPrikaz("RCPT TO:<" + to + ">\r\n");
        
        // 4. Zacatek zadavani obsahu zpravy
        odesliPrikaz("DATA\r\n");

        // 5. Odeslani hlavicek (Predmet, Od koho, Komu), prazdneho radku a samotneho textu
        // SMTP vyzaduje zakonceni zpravy tečkou na novem radku
        String obsahZpravy = "Subject: " + subject + "\r\n" +
                             "From: " + from + "\r\n" +
                             "To: " + to + "\r\n" +
                             "\r\n" +
                             text + "\r\n" +
                             ".\r\n";
        
        odesliPrikaz(obsahZpravy);

    }

    /*
     * Sends QUIT and closes the socket
     */
    public void close() {
        try {
            odesliPrikaz("QUIT\r\n");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Zjednodusuje odesilani textovych prikazu na server
    private void odesliPrikaz(String prikaz) throws IOException {
        byte[] dataKProlaneni = prikaz.getBytes();
        vystup.write(dataKProlaneni, 0, dataKProlaneni.length);
        vystup.flush();
        prectiOdpoved(); // Hned po odeslani zkusime precist, co server odpovedel
    }

    // Precte a vypise odpoved serveru
    private void prectiOdpoved() {
        try {
            Thread.sleep(100); // Kratka pauza, aby mel server cas odpovedet
            if (vstup.available() > 0) {
                byte[] odpoved = new byte[1024];
                int delka = vstup.read(odpoved);
                System.out.write(odpoved, 0, delka); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
