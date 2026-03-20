package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.model.Case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TechniqueIntraBloc_1_3 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            // LOGIQUE MATHÉMATIQUE : Vérification de la forme et du doublon
            if (bloc.getListeCases().size() == 3 && verifierTopologieL(bloc) && verifierDoublonMathematique(bloc)) {
                
                Map<Case, Integer> reponsesMaths = calculerSolutions(bloc);

                boolean contientErreur = false;
                List<Case> casesFausses = new ArrayList<>();

                for (Case c : bloc.getListeCases()) {
                    int valeurJoueur = c.getValeur();
                    // VÉRIFICATION ERREUR
                    if (valeurJoueur != 0 && valeurJoueur != c.getSolution()) {
                        contientErreur = true;
                        casesFausses.add(c);
                    }
                }

                // S'il reste des éléments à remplir (pas parfaits)
                boolean estParfait = true;
                for (Case c : bloc.getListeCases()) if (c.getValeur() != reponsesMaths.get(c)) estParfait = false;

                if (!estParfait) {
                    if (contientErreur) {
                        return new Indice("Doublon en L", "Erreur détectée ! Ce bloc de 3 cases en 'L' force le placement d'un doublon.\nLes chiffres en rouge sont mal placés (les doublons vont aux extrémités).", casesFausses, reponsesMaths, true);
                    } else if (indiceNormal == null) {
                        indiceNormal = new Indice("Doublon en L", "Ce bloc de 3 cases en 'L' n'a qu'une seule combinaison comportant un doublon.\nPlacez-le aux extrémités pour respecter les règles !", bloc.getListeCases(), reponsesMaths, false);
                    }
                }
            }
        }
        return indiceNormal; 
    }

    private boolean verifierDoublonMathematique(GroupementCases bloc) {
        List<List<Integer>> combinaisons = bloc.getCombinaisonsMaths();
        if (combinaisons == null || combinaisons.size() != 1) return false;
        Set<Integer> valeursUniques = new HashSet<>();
        for (Integer valeur : combinaisons.get(0)) {
            if (!valeursUniques.add(valeur)) return true; 
        }
        return false;
    }

    private boolean verifierTopologieL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        Case c1 = cases.get(0), c2 = cases.get(1), c3 = cases.get(2);
        return !(c1.getX() == c2.getX() && c2.getX() == c3.getX()) && !(c1.getY() == c2.getY() && c2.getY() == c3.getY());
    }

    private Map<Case, Integer> calculerSolutions(GroupementCases bloc) {
        Map<Case, Integer> solutions = new HashMap<>();
        List<Integer> combinaison = bloc.getCombinaisonsMaths().get(0);
        List<Case> cases = bloc.getListeCases();

        int chiffreDoublon = 0, chiffreUnique = 0;
        if (combinaison.get(0).equals(combinaison.get(1))) { chiffreDoublon = combinaison.get(0); chiffreUnique = combinaison.get(2); } 
        else if (combinaison.get(0).equals(combinaison.get(2))) { chiffreDoublon = combinaison.get(0); chiffreUnique = combinaison.get(1); } 
        else { chiffreDoublon = combinaison.get(1); chiffreUnique = combinaison.get(0); }

        Case caseCoin = cases.get(0);
        Case c1 = cases.get(0), c2 = cases.get(1), c3 = cases.get(2);
        if ((c1.getX() == c2.getX() || c1.getX() == c3.getX()) && (c1.getY() == c2.getY() || c1.getY() == c3.getY())) caseCoin = c1;
        else if ((c2.getX() == c1.getX() || c2.getX() == c3.getX()) && (c2.getY() == c1.getY() || c2.getY() == c3.getY())) caseCoin = c2;
        else caseCoin = c3;

        for (Case c : cases) {
            if (c.equals(caseCoin)) solutions.put(c, chiffreUnique);
            else solutions.put(c, chiffreDoublon);
        }
        return solutions;
    }
}