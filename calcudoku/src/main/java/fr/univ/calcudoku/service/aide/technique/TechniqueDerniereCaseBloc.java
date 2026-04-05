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

/**
 * Technique d'aide : Dernière Case d'un Bloc.
 * Identifie un bloc mathématique où il ne manque plus qu'une seule case.
 * Le joueur peut alors déduire la valeur en appliquant l'opération inverse.
 */
public class TechniqueDerniereCaseBloc implements TechniqueAide {

    /**
     * Analyse la grille pour trouver un bloc presque complet.
     * @param grille La grille à analyser.
     * @return Un Indice contenant les messages progressifs.
     */
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
                String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";

                // Pour - et /, il peut rester 2 valeurs possibles : on ne déclenche
                // la technique que si les contraintes de la grille n'en laissent qu'une.
                if (symbole.equals("-") || symbole.equals("/")) {
                    bloc.calculerPossibilites(grille);
                    if (bloc.getCombinaisonsMaths().size() > 1) continue;
                }

                List<Case> surbrillance = new ArrayList<>(bloc.getListeCases());
                Map<Case, Integer> solutions = new HashMap<>();
                List<String> messages = new ArrayList<>();
                
                int cible = bloc.getResultatCible();

                messages.add("Un bloc est presque entièrement rempli. C'est le moment idéal pour utiliser les mathématiques.");
                
                if (symbole.equals("+")) {
                    messages.add("Il ne reste qu'une case vide dans ce bloc d'addition.");
                    messages.add("Dernière case : Soustrayez la somme des cases déjà remplies au résultat cible (" + cible + ") pour trouver la valeur manquante du bloc en surbrillance.");
                } else if (symbole.equals("x") || symbole.equals("*")) {
                    messages.add("Il ne reste qu'une case vide dans ce bloc de multiplication.");
                    messages.add("Dernière case : Divisez le résultat cible (" + cible + ") par le produit des cases déjà remplies pour déduire la valeur du bloc en surbrillance.");
                } else if (symbole.equals("-")) {
                    messages.add("Il ne reste qu'une case vide dans ce bloc de soustraction.");
                    messages.add("Dernière case : La case en surbrillance doit être soit plus grande, soit plus petite que l'autre pour avoir une différence de " + cible + ".");
                } else if (symbole.equals("/")) {
                    messages.add("Il ne reste qu'une case vide dans ce bloc de division.");
                    messages.add("Dernière case : La case manquante en surbrillance doit être un multiple ou un diviseur de l'autre pour obtenir un quotient de " + cible + ".");
                } else {
                    messages.add("Il ne reste qu'une seule case vide dans ce bloc.");
                    messages.add("Dernière case : Déduisez la valeur de la case en surbrillance pour atteindre la cible de " + cible + ".");
                }
                
                indicesNormaux.add(new Indice("Dernière Case du Bloc", messages, surbrillance, solutions, false));
            }
        }

        if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(new Random().nextInt(indicesNormaux.size()));
        }

        return null;
    }
}