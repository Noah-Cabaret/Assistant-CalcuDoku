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

public class TechniqueBlocUnique implements TechniqueAide {

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
                    if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                        contientErreur = true;
                        casesFausses.add(c);
                    }
                }

                if (!contientErreur && nbCasesVides <= 1) continue; 
                if (!contientErreur && nbCasesVides == 0) continue; 

                if (contientErreur) {
                    String msg = "Erreur détectée ! Ce bloc ne peut être résolu qu'avec une seule combinaison de chiffres précise.\nLes chiffres en surbrillance sont incorrects.";
                    indicesErreurs.add(new Indice("Combinaison Unique", msg, casesFausses, new HashMap<>(), true));
                } else {
                    String msg = "Techniques de blocs uniques : Observez ce bloc. Les règles du CalcuDoku font qu'il n'existe qu'une seule combinaison de nombres possible pour atteindre ce résultat avec cette opération ! Déduisez-la.";
                    indicesNormaux.add(new Indice("Combinaison Unique", msg, casesDuBloc, new HashMap<>(), false));
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null;
    }

    private boolean aDesChiffresIdentiques(List<Integer> combinaison) {
        if (combinaison == null || combinaison.isEmpty()) return false;
        Set<Integer> valeursVues = new HashSet<>();
        for (Integer valeur : combinaison) {
            if (!valeursVues.add(valeur)) return true;
        }
        return false;
    }
}