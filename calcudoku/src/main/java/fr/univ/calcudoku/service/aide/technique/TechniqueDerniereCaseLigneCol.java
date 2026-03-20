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
        Indice indiceNormal = null;

        for (int i = 0; i < taille; i++) {
            Indice indLigne = chercherDerniereCase(grille, i, true);
            if (indLigne != null) { if (indLigne.aUneErreur()) return indLigne; if (indiceNormal == null) indiceNormal = indLigne; }
            Indice indCol = chercherDerniereCase(grille, i, false);
            if (indCol != null) { if (indCol.aUneErreur()) return indCol; if (indiceNormal == null) indiceNormal = indCol; }
        }
        return indiceNormal;
    }

    private Indice chercherDerniereCase(Grille grille, int index, boolean estLigne) {
        int taille = grille.getTaille();

        for (int indexCible = 0; indexCible < taille; indexCible++) {
            int xCible = estLigne ? indexCible : index;
            int yCible = estLigne ? index : indexCible;
            Case caseCible = grille.getCase(xCible, yCible);

            int nbAutresRemplies = 0;
            boolean[] presents = new boolean[taille + 1];

            // LOGIQUE MATHÉMATIQUE : On vérifie les N-1 autres cases
            for (int i = 0; i < taille; i++) {
                if (i == indexCible) continue; 
                Case c = grille.getCase(estLigne ? i : index, estLigne ? index : i);
                if (c.getValeur() != 0 && c.getValeur() <= taille) {
                    if (!presents[c.getValeur()]) {
                        presents[c.getValeur()] = true;
                        nbAutresRemplies++;
                    }
                }
            }

            if (nbAutresRemplies == taille - 1) {
                int chiffreManquant = 0;
                for (int v = 1; v <= taille; v++) {
                    if (!presents[v]) { chiffreManquant = v; break; }
                }

                if (caseCible.getValeur() == chiffreManquant) continue; 

                if (chiffreManquant != 0) {
                    // VÉRIFICATION ERREUR 
                    boolean contientErreur = (caseCible.getValeur() != 0 && caseCible.getValeur() != caseCible.getSolution());

                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, chiffreManquant);
                    List<Case> casesASurbriller = new ArrayList<>();
                    String message;

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        message = "Erreur détectée ! Les autres cases de cette " + (estLigne ? "ligne" : "colonne") + " sont déjà remplies.\n" +
                                  "Le seul chiffre manquant est le " + chiffreManquant + ".";
                        return new Indice("Dernière Case", message, casesASurbriller, solutions, true);
                    } else {
                        for (int i = 0; i < taille; i++) casesASurbriller.add(grille.getCase(estLigne ? i : index, estLigne ? index : i));
                        message = "Il ne manque qu'une seule case pour compléter cette " + (estLigne ? "ligne.\n" : "colonne.\n");
                        return new Indice("Dernière Case", message, casesASurbriller, solutions, false);
                    }
                }
            }
        }
        return null;
    }
}