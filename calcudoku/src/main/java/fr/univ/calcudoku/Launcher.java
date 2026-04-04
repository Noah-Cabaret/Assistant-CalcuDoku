package fr.univ.calcudoku;

/**
 * Classe de lancement (Launcher) pour l'application Calcudoku.
 * <p>
 * Cette classe sert de point d'entrée alternatif pour contourner un problème
 * courant avec les applications JavaFX empaquetées en JAR exécutable.
 * Elle appelle simplement la méthode {@code main} de la classe principale {@link MainApp}.
 */
public class Launcher {
    /**
     * Point d'entrée du programme.
     * @param args les arguments de la ligne de commande passés à l'application.
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}