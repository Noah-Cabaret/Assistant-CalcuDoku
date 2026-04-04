package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.save.Sauvegarde;

public class ScoreManager {

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

    public static String formaterTemps(int secondesEcoulees) {
        return (secondesEcoulees / 60) + " min " + (secondesEcoulees % 60) + " s";
    }
}
