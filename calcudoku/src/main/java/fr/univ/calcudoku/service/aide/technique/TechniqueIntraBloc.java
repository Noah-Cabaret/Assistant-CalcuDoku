package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe parente abstraite pour toutes les techniques Intra-Bloc.
 * Fournit le moteur de filtrage intelligent (Backtracking) et la détection de doublons.
 * Cette classe ne retourne pas d'Indice directement.
 */
public abstract class TechniqueIntraBloc implements TechniqueAide {

    /**
     * Vérifie si une combinaison mathématique possède des chiffres en double.
     * @param combinaison La combinaison à vérifier.
     * @return vrai si la combinaison contient des doublons.
     */
    protected boolean aDesChiffresIdentiques(List<Integer> combinaison) {
        if (combinaison == null || combinaison.isEmpty()) return false;
        Set<Integer> valeursVues = new HashSet<>();
        for (Integer valeur : combinaison) {
            if (!valeursVues.add(valeur)) return true;
        }
        return false;
    }

    /**
     * Filtre et renvoie uniquement les combinaisons qui sont encore 
     * possibles sans entrer en conflit avec la grille actuelle (Topologie + Valeurs).
     * @param grille La grille analysée.
     * @param bloc   Le bloc à tester.
     * @return La liste des combinaisons valides restantes.
     */
    protected List<List<Integer>> getCombinaisonsValides(Grille grille, GroupementCases bloc) {
        List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
        List<List<Integer>> combinaisonsValides = new ArrayList<>();
        
        if (combinaisonsPossibles != null) {
            for (List<Integer> combo : combinaisonsPossibles) {
                if (peutPlacerCombinaison(grille, bloc, combo)) {
                    combinaisonsValides.add(combo);
                }
            }
        }
        return combinaisonsValides;
    }

    private boolean peutPlacerCombinaison(Grille grille, GroupementCases bloc, List<Integer> combinaison) {
        List<Case> cases = bloc.getListeCases();
        boolean[] utilise = new boolean[combinaison.size()];
        int[] placementLocal = new int[cases.size()];
        return backtrackPlacer(grille, cases, 0, combinaison, utilise, placementLocal);
    }

    private boolean backtrackPlacer(Grille grille, List<Case> cases, int indexCase, List<Integer> combinaison, boolean[] utilise, int[] placementLocal) {
        if (indexCase == cases.size()) return true;

        Case c = cases.get(indexCase);
        for (int i = 0; i < combinaison.size(); i++) {
            if (!utilise[i]) {
                int val = combinaison.get(i);
                if (estValeurAutorisee(grille, c, val, cases, placementLocal, indexCase)) {
                    utilise[i] = true;
                    placementLocal[indexCase] = val;
                    if (backtrackPlacer(grille, cases, indexCase + 1, combinaison, utilise, placementLocal)) return true;
                    utilise[i] = false;
                    placementLocal[indexCase] = 0;
                }
            }
        }
        return false;
    }

    private boolean estValeurAutorisee(Grille grille, Case c, int val, List<Case> blocCases, int[] placementLocal, int currentIndex) {
        for (int i = 0; i < grille.getTaille(); i++) {
            Case caseLigne = grille.getCase(i, c.getY());
            if (!blocCases.contains(caseLigne) && caseLigne.getValeur() == val) return false;
            
            Case caseCol = grille.getCase(c.getX(), i);
            if (!blocCases.contains(caseCol) && caseCol.getValeur() == val) return false;
        }
        
        for (int i = 0; i < currentIndex; i++) {
            Case caseAssignee = blocCases.get(i);
            if (placementLocal[i] == val && (caseAssignee.getX() == c.getX() || caseAssignee.getY() == c.getY())) {
                return false; 
            }
        }
        return true;
    }
}