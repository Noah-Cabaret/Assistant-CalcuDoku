package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Technique : Bloc Unique.
 * Cherche les blocs dont l'opération mathématique n'autorise qu'une seule combinaison de chiffres.
 */
public class TechniqueBlocUnique implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            List<Case> casesDuBloc = bloc.getListeCases();
            if (casesDuBloc.size() <= 1) continue;

            List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
            
            if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
                boolean contientErreur = false;
                List<Case> casesFausses = new ArrayList<>();
                int nbCasesVides = 0;

                for (Case c : casesDuBloc) {
                    if (c.getValeur() == 0) nbCasesVides++;
                    if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                        contientErreur = true;
                        casesFausses.add(c);
                    }
                }

                if (!contientErreur && nbCasesVides <= 1) continue; 
                if (!contientErreur && nbCasesVides == 0) continue; 

                if (contientErreur) {
                    // Retrait de la construction de la chaîne détaillant la solution exacte
                    String msg = "Erreur détectée ! Ce bloc ne peut être résolu qu'avec une seule combinaison de chiffres précise.\nLes chiffres en surbrillance sont incorrects.";
                    return new Indice("Combinaison Unique", msg, casesFausses, new HashMap<>(), true);
                } else if (indiceNormal == null) {
                    String msg = "Techniques de blocs uniques : Observez ce bloc. Les règles du CalcuDoku font qu'il n'existe qu'une seule combinaison de nombres possible pour atteindre ce résultat avec cette opération ! Déduisez-la.";
                    indiceNormal = new Indice("Combinaison Unique", msg, casesDuBloc, new HashMap<>(), false);
                }
            }
        }
        return indiceNormal;
    }
}