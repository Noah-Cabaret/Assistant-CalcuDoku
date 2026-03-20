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
        Indice indiceNormal = null;

        for (int v = 1; v <= taille; v++) {
            int count = 0;
            boolean[] ligneContient = new boolean[taille];
            boolean[] colContient = new boolean[taille];
            List<Case> casesAvecValeur = new ArrayList<>();

            // LOGIQUE MATHÉMATIQUE : On cherche s'il y a N-1 exemplaires du chiffre
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
                    if (caseCible.getValeur() == v) continue;

                    // VÉRIFICATION ERREUR 
                    boolean contientErreur = (caseCible.getValeur() != 0 && caseCible.getValeur() != caseCible.getSolution());
                    
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, v);
                    List<Case> surbrillance = new ArrayList<>();

                    if (contientErreur) {
                        surbrillance.add(caseCible);
                        return new Indice("Dernier Chiffre Restant", "Erreur ! Vous avez placé un " + caseCible.getValeur() + ", mais il s'agit du tout dernier emplacement de la grille pour le " + v + ".", surbrillance, solutions, true);
                    } else if (indiceNormal == null) {
                        surbrillance.addAll(casesAvecValeur);
                        indiceNormal = new Indice("Dernier Chiffre Restant", "Il y a un chiffre dont il ne manque plus qu'un exemplaire dans toute la grille.\nTrouvez sa position par élimination !", surbrillance, solutions, false);
                    }
                }
            }
        }
        return indiceNormal;
    }
}