package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TechniqueDerniereCaseBloc implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        List<Indice> indicesNormaux = new ArrayList<>();

        Set<GroupementCases> tousLesBlocs = new HashSet<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                GroupementCases bloc = grille.getCase(i, j).getGroupement();
                if (bloc != null) tousLesBlocs.add(bloc);
            }
        }

        for (GroupementCases bloc : tousLesBlocs) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            boolean contientErreur = false;

            for (Case c : bloc.getListeCases()) {
                if (c.getValeur() == 0) nbCasesVides++;
                else if (c.getValeur() != c.getSolution()) contientErreur = true;
            }

            if (nbCasesVides == 1 && !contientErreur) {
                List<Case> surbrillance = new ArrayList<>(bloc.getListeCases());
                Map<Case, Integer> solutions = new HashMap<>();
                
                String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                int cible = bloc.getResultatCible();
                String message = "";

                // Messages adaptés en fonction du signe
                if (symbole.equals("+")) {
                    message = "Dernière case : Il ne reste qu'une case vide dans ce bloc d'addition.\nSoustrayez la somme des cases déjà remplies au résultat cible (" + cible + ") pour trouver la valeur manquante.";
                } else if (symbole.equals("x") || symbole.equals("*")) {
                    message = "Dernière case : Il ne reste qu'une case vide dans ce bloc de multiplication.\nDivisez le résultat cible (" + cible + ") par le produit des cases déjà remplies pour déduire la valeur manquante.";
                } else if (symbole.equals("-")) {
                    message = "Dernière case : Il ne reste qu'une case vide dans ce bloc de soustraction.\nRéfléchissez à l'écart : la case manquante doit être soit plus grande, soit plus petite que celle déjà présente pour que leur différence vaille " + cible + ".";
                } else if (symbole.equals("/")) {
                    message = "Dernière case : Il ne reste qu'une case vide dans ce bloc de division.\nLa case manquante doit être soit un multiple, soit un diviseur de la case déjà présente pour obtenir un quotient de " + cible + ".";
                } else {
                    message = "Dernière case : Il ne reste qu'une seule case vide dans ce bloc.\nDéduisez sa valeur pour atteindre la cible de " + cible + ".";
                }
                
                indicesNormaux.add(new Indice("Dernière Case du Bloc", message, surbrillance, solutions, false));
            }
        }

        if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(new Random().nextInt(indicesNormaux.size()));
        }

        return null;
    }
}