package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueDerniereCaseBloc implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            for (Case caseCible : bloc.getListeCases()) {
                // LOGIQUE MATHÉMATIQUE : On vérifie si TOUTES les AUTRES cases sont remplies
                boolean autresRemplies = true;
                for (Case c : bloc.getListeCases()) {
                    if (c != caseCible && c.getValeur() == 0) { autresRemplies = false; break; }
                }

                if (autresRemplies) {
                    int chiffreSolution = 0;
                    int valeurOriginale = caseCible.getValeur();

                    // LOGIQUE MATHÉMATIQUE : On simule pour trouver la solution
                    for (int v = 1; v <= taille; v++) {
                        if (grille.estCoupValide(caseCible.getX(), caseCible.getY(), v)) {
                            caseCible.setValeur(v);
                            boolean mathOk = bloc.groupementValide();
                            caseCible.setValeur(valeurOriginale); 
                            if (mathOk) { chiffreSolution = v; break; }
                        }
                    }

                    if (chiffreSolution != 0) {
                        if (valeurOriginale == chiffreSolution) continue;

                        // VÉRIFICATION ERREUR : La case est remplie mais ne correspond pas à la solution finale
                        boolean contientErreur = (valeurOriginale != 0 && valeurOriginale != caseCible.getSolution());
                        
                        Map<Case, Integer> solutions = new HashMap<>();
                        solutions.put(caseCible, chiffreSolution);
                        List<Case> casesASurbriller = new ArrayList<>();
                        String message;

                        if (contientErreur) {
                            casesASurbriller.add(caseCible);
                            message = "Erreur mathématique ! Toutes les autres cases de ce bloc sont remplies.\n" +
                                      "Pour atteindre le résultat cible, cette case doit obligatoirement valoir " + chiffreSolution + ".";
                            return new Indice("Calcul Final du Bloc", message, casesASurbriller, solutions, true);
                        } else if (indiceNormal == null) {
                            casesASurbriller.addAll(bloc.getListeCases());
                            message = "Toutes les cases d'un bloc sont remplies sauf une.\n" +
                                      "Faites le calcul mathématique pour déduire ce qui manque !";
                            indiceNormal = new Indice("Calcul Final du Bloc", message, casesASurbriller, solutions, false);
                        }
                    }
                }
            }
        }
        return indiceNormal;
    }
}