package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueDerniereCaseLigneCol implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();

        for (int i = 0; i < taille; i++) {
            Indice indLigne = chercherDerniereCase(grille, i, true);
            if (indLigne != null) {
                return indLigne;
            }

            Indice indCol = chercherDerniereCase(grille, i, false);
            if (indCol != null) {
                return indCol;
            }
        }

        return null;
    }

    private Indice chercherDerniereCase(Grille grille, int index, boolean estLigne) {
        int taille = grille.getTaille();
        Case caseVide = null;
        int nbVides = 0;
        boolean[] presents = new boolean[taille + 1];

        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            Case c = grille.getCase(x, y);

            if (c.getValeur() == 0) {
                caseVide = c;
                nbVides++;
            } else {
                if (c.getValeur() <= taille) {
                    presents[c.getValeur()] = true;
                }
            }
        }

        if (nbVides == 1 && caseVide != null) {
            int chiffreManquant = 0;
            for (int v = 1; v <= taille; v++) {
                if (!presents[v]) {
                    chiffreManquant = v;
                    break;
                }
            }

            if (chiffreManquant != 0) {
                Map<Case, Integer> solutions = new HashMap<>();
                solutions.put(caseVide, chiffreManquant);

                List<Case> casesASurbriller = new ArrayList<>();
                for (int i = 0; i < taille; i++) {
                    int x = estLigne ? i : index;
                    int y = estLigne ? index : i;
                    casesASurbriller.add(grille.getCase(x, y));
                }
                String nom = "Dernière Case en " + (estLigne ? "Ligne" : "Colonne");
                String message = "Il ne manque qu'une seule case pour compléter une " + (estLigne ? "ligne.\n" : "colonne.\n");

                return new Indice(nom, message, casesASurbriller, solutions, false);
            }
        }

        return null;
    }
}
