package fr.univ.calcudoku.service;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import java.util.ArrayList;
import java.util.List;

/**
 * Service chargé de vérifier les règles du jeu et de valider la grille.
 */
public class ValidateurJeu {

    /**
     * Vérifie si la grille est totalement remplie et 100% correcte.
     * @param grille La grille à analyser
     * @return true si le joueur a gagné, false sinon
     */
    public static boolean estVictoire(Grille grille) {
        return grille.estGagnee();
    }

    /**
     * Parcourt la grille et renvoie la liste des cases qui sont fausses.
     * Ignore les cases vides (qui valent 0).
     * @param grille La grille à analyser
     * @return Une liste contenant les cases en erreur
     */
    public static List<Case> trouverErreurs(Grille grille) {
        List<Case> casesEnErreur = new ArrayList<>();

        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                Case c = grille.getCase(x, y);
                if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                    casesEnErreur.add(c);
                }
            }
        }

        return casesEnErreur;
    }
}