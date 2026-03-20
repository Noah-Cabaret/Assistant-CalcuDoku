package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueDernierChiffreGrille implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();

        for (int v = 1; v <= taille; v++) {
            int count = 0;
            boolean[] ligneContient = new boolean[taille];
            boolean[] colContient = new boolean[taille];
            List<Case> casesAvecValeur = new ArrayList<>();

            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    Case c = grille.getCase(x, y);
                    if (c.getValeur() == v) {
                        count++;
                        ligneContient[y] = true;
                        colContient[x] = true;
                        casesAvecValeur.add(c);
                    }
                }
            }

            if (count == taille - 1) {
                int ligneManquante = -1;
                int colManquante = -1;

                for (int i = 0; i < taille; i++) {
                    if (!ligneContient[i]) ligneManquante = i;
                    if (!colContient[i]) colManquante = i;
                }

                if (ligneManquante != -1 && colManquante != -1) {
                    Case caseCible = grille.getCase(colManquante, ligneManquante);
                    boolean contientErreur = caseCible.getValeur() != 0 && caseCible.getValeur() != v;

                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, v);

                    List<Case> casesASurbriller = new ArrayList<>(casesAvecValeur);

                    String nom = "Dernier Chiffre Restant";
                    // Le texte ne mentionne plus "v" !
                    String message = "Il y a un chiffre dont il ne manque plus qu'un seul exemplaire dans toute la grille.\n" +
                                     "Observez bien pour trouver de quel chiffre il s'agit, et déduisez sa position par élimination !";

                    return new Indice(nom, message, casesASurbriller, solutions, contientErreur);
                }
            }
        }

        return null;
    }
}