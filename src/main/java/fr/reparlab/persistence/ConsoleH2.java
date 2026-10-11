package fr.reparlab.persistence;

import org.h2.tools.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/** Console web dans la meme JVM que la base H2 en memoire. */
public final class ConsoleH2 implements AutoCloseable {
    private final Server serveur;
    private final BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

    public ConsoleH2(boolean active) throws SQLException {
        // Ne pas enregistrer de preferences dans le dossier personnel.
        serveur = active ? Server.createWebServer(
                "-webPort", "8082", "-properties", "null", "-ifExists").start() : null;
        if (active) {
            System.out.println("\nCONSOLE H2 : http://localhost:8082");
            System.out.println("Pilote : org.h2.Driver");
            System.out.println("URL JDBC : jdbc:h2:mem:reparlab");
            System.out.println("Utilisateur : sa | Mot de passe : laisser vide");
        }
    }

    public void pausePourCapture(String etape) throws IOException {
        if (serveur == null) return;
        System.out.println("\n[PAUSE H2] " + etape);
        System.out.println("Executez vos SELECT dans le navigateur et prenez votre capture.");
        System.out.println("Puis appuyez sur Entree ICI, dans la console Run d'IntelliJ, pour continuer.");
        if (clavier.readLine() == null) {
            throw new IllegalStateException(
                    "Entree standard fermee. Lancez DemoH2Navigateur dans la console Run d'IntelliJ.");
        }
    }

    @Override
    public void close() {
        if (serveur != null) serveur.stop();
        // System.in appartient au processus : ne pas le fermer ici.
    }
}
