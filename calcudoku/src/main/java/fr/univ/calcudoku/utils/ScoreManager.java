package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.save.Sauvegarde;

/**
 * Calcule le score de fin de partie et formate le temps d'affichage.
 */
public class ScoreManager {

    /**
     * Calcule le score final en fonction de la taille, du temps, des erreurs et des aides.
     * @param tailleGrille taille de la grille
     * @param secondesEcoulees temps en secondes
     * @param malus nombre d'erreurs
     * @param aidesUtilisees nombre d'aides utilisées
     * @param difficulte niveau de difficulté
     * @return le score calculé
     */
    public static long calculerScore(int tailleGrille, int secondesEcoulees, int malus, int aidesUtilisees, Sauvegarde.Difficulte difficulte) {
        long pointsBase = (long) (tailleGrille * tailleGrille) * 100L;

        long penaliteTemps = Math.min(secondesEcoulees * 2L, (long) (pointsBase * 0.5));
        long penaliteErreurs = malus * 50L;
        long penaliteAides = aidesUtilisees * 50L;

        long score = pointsBase - penaliteTemps - penaliteErreurs - penaliteAides;
        score = Math.max(100L, score);

        if (difficulte == Sauvegarde.Difficulte.MOYEN) {
            score = (long) (score * 1.5);
        } else if (difficulte == Sauvegarde.Difficulte.DIFFI) {
            score *= 2;
        }

        return score;
    }

    /**
     * Formate un temps en secondes en chaîne lisible.
     * @param secondesEcoulees le temps en secondes
     * @return le temps formaté (ex: "2 min 30 s")
     */
    public static String formaterTemps(int secondesEcoulees) {
        return (secondesEcoulees / 60) + " min " + (secondesEcoulees % 60) + " s";
    }
}
