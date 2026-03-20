package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.model.Case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique : Intra Bloc (Petit L).
 * Analyse un bloc de 3 cases en forme de L qui nécessite obligatoirement un chiffre en double.
 * Le doublon doit être aux extrémités pour ne pas violer les règles du carré latin.
 */
public class TechniqueIntraBloc_1_3 extends TechniqueIntraBloc {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            // On vérifie la topologie : 3 cases, formant un "L" (pas une ligne)
            if (bloc.getListeCases().size() == 3 && verifierTopologiePetitL(bloc)) {
                
                List<Integer> combinaisonUnique = getUniqueCombinaison(bloc);
                
                if (combinaisonUnique != null && aDesChiffresIdentiques(combinaisonUnique)) {
                    
                    boolean contientErreur = false;
                    List<Case> casesFausses = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();

                    for (Case c : bloc.getListeCases()) {
                        solutions.put(c, c.getSolution());
                        
                        if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                            contientErreur = true;
                            casesFausses.add(c);
                        }
                    }

                    boolean estParfait = true;
                    for (Case c : bloc.getListeCases()) {
                        if (c.getValeur() != c.getSolution()) estParfait = false;
                    }

                    if (!estParfait) {
                        if (contientErreur) {
                            return new Indice("Technique Intra-bloc", "Erreur détectée ! Ce bloc force le placement d'un doublon mathématique.\nLes chiffres en surbrillance sont mal placés.", casesFausses, solutions, true);
                        } else if (indiceNormal == null) {
                            String msg = "Techniques intra-bloc : Ce bloc en forme de 'L' n'a qu'une seule combinaison possible qui contient un chiffre en double.\nPour ne pas violer les règles, ces doublons doivent obligatoirement être placés aux deux extrémités du 'L' !";
                            indiceNormal = new Indice("Technique Intra-bloc", msg, bloc.getListeCases(), solutions, false);
                        }
                    }
                }
            }
        }
        return indiceNormal; 
    }

    private boolean verifierTopologiePetitL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        Case c1 = cases.get(0), c2 = cases.get(1), c3 = cases.get(2);
        boolean estLigneVerticale = (c1.getX() == c2.getX() && c2.getX() == c3.getX());
        boolean estLigneHorizontale = (c1.getY() == c2.getY() && c2.getY() == c3.getY());
        return !estLigneVerticale && !estLigneHorizontale;
    }
}