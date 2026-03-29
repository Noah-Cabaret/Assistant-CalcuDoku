package fr.univ.calcudoku.utils;

/**
 * Classe de lancement (Launcher) pour contourner le bug de démarrage de JavaFX.
 */
public class GenerateurLauncher {
    public static void main(String[] args) {
        // Appelle la méthode main de ton vrai générateur
        GenerateurSnapshot.main(args);
    }
}