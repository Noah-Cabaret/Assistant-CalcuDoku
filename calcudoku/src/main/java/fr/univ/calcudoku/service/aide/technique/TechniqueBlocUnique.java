package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Technique d'aide permettant d'identifier les blocs de la grille
 * qui ne peuvent être résolus qu'avec une seule combinaison de chiffres.
 */
public class TechniqueBlocUnique implements TechniqueAide {

    /**
     * Analyse la grille pour détecter un bloc ayant une combinaison mathématique unique.
     * Fournit un indice progressif s'adressant directement au joueur pour le guider.
     * * @param grille La grille actuelle à analyser
     * @return Un Indice contenant l'aide, ou null si la technique ne trouve rien
     */
    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            List<Case> casesDuBloc = bloc.getListeCases();
            if (casesDuBloc.size() <= 1) continue;

            List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
            
            if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
                List<Integer> combinaisonUnique = combinaisonsPossibles.get(0);
                if (aDesChiffresIdentiques(combinaisonUnique)) continue;

                boolean contientErreur = false;
                List<Case> casesFausses = new ArrayList<>();
                int nbCasesVides = 0;

                for (Case c : casesDuBloc) {
                    if (c.getValeur() == 0) nbCasesVides++;
                    else if (c.getValeur() != c.getSolution()) { contientErreur = true; casesFausses.add(c); }
                }

                if (!contientErreur && nbCasesVides <= 1) continue; 

                List<String> messages = new ArrayList<>();

                if (contientErreur) {
                    // Progression de l'indice en cas d'erreur
                    messages.add("Une incohérence s'est glissée dans l'un de vos blocs. Réfléchissez aux différentes façons d'atteindre sa cible.");
                    messages.add("Vérifiez vos calculs. Les chiffres que vous avez placés ne permettent pas d'atteindre la cible avec l'unique combinaison possible de ce bloc.");
                    messages.add("Les cases en surbrillance sont incorrectes. En effet, ce bloc ne peut être résolu qu'avec une seule combinaison de chiffres bien précise.");
                    indicesErreurs.add(new Indice("Combinaison Unique", messages, casesFausses, new HashMap<>(), true));
                } else {
                    // Progression pour un bloc à déduire
                    String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                    String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                    int cible = bloc.getResultatCible();
                    int taille = casesDuBloc.size();
                    
                    messages.add("Observez bien la grille. Certains blocs sont de bons points de départ car ils ont une contrainte très forte.");
                    
                    // Adaptation du message intermédiaire (logique) en fonction de l'opérateur
                    if (symbole.equals("+")) {
                        messages.add("Il n'existe qu'une seule somme mathématique possible pour faire " + cible + " avec " + taille + " cases.");
                    } else if (symbole.equals("x") || symbole.equals("*")) {
                        messages.add("La seule façon d'obtenir " + cible + " avec " + taille + " cases est d'utiliser une seule combinaison de multiplication.");
                    } else if (symbole.equals("-")) {
                        messages.add("Compte tenu de la taille de la grille, une seule paire de chiffres a une différence exacte de " + cible + ".");
                    } else if (symbole.equals("/")) {
                        messages.add("Il n'existe qu'une seule paire de chiffres dont le quotient donne exactement " + cible + ".");
                    } else {
                        messages.add("Il n'existe qu'une seule combinaison pour atteindre la cible de " + cible + " dans ce bloc.");
                    }
                    
                    messages.add("La combinaison unique est (" + comboStr + "). N'hésitez pas à utiliser le mode annotation sur les cases en surbrillance !");
                    
                    indicesNormaux.add(new Indice("Combinaison Unique", messages, casesDuBloc, new HashMap<>(), false));
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null;
    }

    /**
     * Vérifie si une combinaison contient des chiffres en double.
     * * @param combinaison La combinaison de chiffres à vérifier
     * @return true si au moins un chiffre est en double, false sinon
     */
    private boolean aDesChiffresIdentiques(List<Integer> combinaison) {
        if (combinaison == null || combinaison.isEmpty()) return false;
        Set<Integer> valeursVues = new HashSet<>();
        for (Integer valeur : combinaison) { if (!valeursVues.add(valeur)) return true; }
        return false;
    }
}