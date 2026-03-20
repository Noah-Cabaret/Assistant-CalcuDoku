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

   // ... (Voir l'import complet plus haut dans les précédentes réponses, j'isole la méthode logique)

    private Indice chercherDerniereCase(Grille grille, int index, boolean estLigne) {
        int taille = grille.getTaille();

        for (int indexCible = 0; indexCible < taille; indexCible++) {
            int xCible = estLigne ? indexCible : index;
            int yCible = estLigne ? index : indexCible;
            Case caseCible = grille.getCase(xCible, yCible);

            int nbAutresRemplies = 0;
            boolean[] presents = new boolean[taille + 1];

            // On regarde si (Taille - 1) cases sont remplies
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

            // Déduction basique : On cherche le trou
            if (nbAutresRemplies == taille - 1) {
                int chiffreManquant = 0;
                for (int v = 1; v <= taille; v++) {
                    if (!presents[v]) { chiffreManquant = v; break; }
                }

                if (caseCible.getValeur() == chiffreManquant) continue; 

                if (chiffreManquant != 0) {
                    boolean contientErreur = (caseCible.getValeur() != 0 && caseCible.getValeur() != caseCible.getSolution());

                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, chiffreManquant);
                    List<Case> casesASurbriller = new ArrayList<>();
                    String message;

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        message = "Erreur détectée ! Les autres cases de cette zone sont justes.\nLe seul chiffre manquant pour finir est le " + chiffreManquant + ".";
                        return new Indice("Dernière Case", message, casesASurbriller, solutions, true);
                    } else {
                        for (int i = 0; i < taille; i++) casesASurbriller.add(grille.getCase(estLigne ? i : index, estLigne ? index : i));
                        message = "Déduction logique : Regardez cette zone. Il ne manque plus qu'une seule case pour la compléter, vous pouvez facilement déduire sa valeur !";
                        return new Indice("Dernière Case", message, casesASurbriller, solutions, false);
                    }
                }
            }
        }
        return null;
    }
}
