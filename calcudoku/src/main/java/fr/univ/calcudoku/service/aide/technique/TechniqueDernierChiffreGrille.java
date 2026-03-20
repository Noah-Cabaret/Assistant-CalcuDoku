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

            // On cherche s'il y a déjà (Taille - 1) occurrences d'un chiffre dans toute la
            // grille
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

                // Par élimination, on trouve l'intersection vide
                for (int i = 0; i < taille; i++) {
                    if (!ligneContient[i])
                        ligneManquante = i;
                    if (!colContient[i])
                        colManquante = i;
                }

                if (ligneManquante != -1 && colManquante != -1) {
                    Case caseCible = grille.getCase(colManquante, ligneManquante);
                    if (caseCible.getValeur() == v)
                        continue;

                    boolean contientErreur = (caseCible.getValeur() != 0
                            && caseCible.getValeur() != caseCible.getSolution());

                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, v);
                    List<Case> surbrillance = new ArrayList<>();

                    if (contientErreur) {
                        surbrillance.add(caseCible);
                        return new Indice("Dernier Chiffre Restant",
                                "Erreur ! Il s'agit du tout dernier emplacement possible dans la grille pour le chiffre "
                                        + v + ".",
                                surbrillance, solutions, true);
                    } else if (indiceNormal == null) {
                        surbrillance.addAll(casesAvecValeur);
                        String msg = "Déduction globale : Il ne manque plus qu'un seul exemplaire de ce chiffre dans toute la grille.\nTrouvez sa dernière position par simple élimination des lignes et colonnes !";
                        indiceNormal = new Indice("Dernier Chiffre Restant", msg, surbrillance, solutions, false);
                    }
                }
            }
        }
        return indiceNormal;
    }
}