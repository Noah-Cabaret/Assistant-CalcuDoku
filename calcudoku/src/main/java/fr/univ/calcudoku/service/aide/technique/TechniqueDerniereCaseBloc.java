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

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() == 1) {
                continue;
            }

            Case caseVide = null;
            int nbVides = 0;

            for (Case c : bloc.getListeCases()) {
                if (c.getValeur() == 0) {
                    caseVide = c;
                    nbVides++;
                }
            }

            if (nbVides == 1 && caseVide != null) {
                int chiffreSolution = 0;

                for (int v = 1; v <= taille; v++) {
                    if (grille.estCoupValide(caseVide.getX(), caseVide.getY(), v)) {
                        caseVide.setValeur(v);
                        boolean mathOk = bloc.groupementValide();
                        caseVide.setValeur(0);

                        if (mathOk) {
                            chiffreSolution = v;
                            break;
                        }
                    }
                }

                if (chiffreSolution != 0) {
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseVide, chiffreSolution);

                    List<Case> casesASurbriller = new ArrayList<>(bloc.getListeCases());

                    String nom = "Calcul Final du Bloc";
                    String message = "Toutes les cases d'un bloc sont remplies sauf une.\n" +
                                     "Faites le calcul mathématique avec les chiffres déjà présents pour déduire ce qui manque !";

                    return new Indice(nom, message, casesASurbriller, solutions, false);
                }
            }
        }

        return null;
    }
}