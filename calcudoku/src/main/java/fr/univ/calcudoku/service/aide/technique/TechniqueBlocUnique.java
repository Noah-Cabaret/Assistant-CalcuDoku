package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TechniqueBlocUnique implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            List<Case> casesDuBloc = bloc.getListeCases();
            if (casesDuBloc.size() <= 1) continue;

            // LOGIQUE MATHÉMATIQUE : On lit les combinaisons
            List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
            
            if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
                List<Integer> lUniqueCombinaison = combinaisonsPossibles.get(0);
                
                boolean contientErreur = false;
                List<Case> casesFausses = new ArrayList<>();
                int nbCasesVides = 0;

                for (Case c : casesDuBloc) {
                    if (c.getValeur() == 0) nbCasesVides++;
                    // VÉRIFICATION ERREUR 
                    if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                        contientErreur = true;
                        casesFausses.add(c);
                    }
                }

                if (!contientErreur && nbCasesVides <= 1) continue; // Filtre Anti-doublon
                if (!contientErreur && nbCasesVides == 0) continue; // Parfaitement rempli

                StringBuilder chiffresTexte = new StringBuilder("{");
                for (int i = 0; i < lUniqueCombinaison.size(); i++) {
                    chiffresTexte.append(lUniqueCombinaison.get(i));
                    if (i < lUniqueCombinaison.size() - 1) chiffresTexte.append(", ");
                }
                chiffresTexte.append("}");

                if (contientErreur) {
                    String msg = "Erreur détectée ! Ce bloc ne peut être résolu qu'avec la combinaison stricte : " + chiffresTexte.toString() + ".\nLes chiffres en rouge sont incorrects.";
                    return new Indice("Combinaison Unique", msg, casesFausses, new HashMap<>(), true);
                } else if (indiceNormal == null) {
                    String msg = "Regardez ce bloc (Cible : " + bloc.getResultatCible() + bloc.getOperation().getSymbole() + ").\nIl n'existe mathématiquement qu'une combinaison : " + chiffresTexte.toString() + ".";
                    indiceNormal = new Indice("Combinaison Unique", msg, casesDuBloc, new HashMap<>(), false);
                }
            }
        }
        return indiceNormal;
    }
}