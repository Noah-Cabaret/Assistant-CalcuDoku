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
 * Technique : Intra Bloc (Grand L).
 * Analyse un grand bloc (>=4) en "L" qui nécessite un chiffre en double.
 * L'alignement de la ligne principale du bloc restreint les places du doublon.
 */
public class TechniqueIntraBloc_3_5 extends TechniqueIntraBloc {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            // On vérifie la topologie : >= 4 cases, formant une ligne avec un débordement
            if (bloc.getListeCases().size() >= 4 && verifierTopologieGrandL(bloc)) {
                
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
                            return new Indice("Technique Intra-bloc", "Erreur détectée ! Ce bloc allongé force l'utilisation d'un doublon.\nCertains de ces chiffres sont mal placés pour éviter les conflits.", casesFausses, solutions, true);
                        } else if (indiceNormal == null) {
                            String msg = "Techniques intra-bloc : Observez ce grand bloc en 'L' allongé. Sa seule combinaison nécessite des doublons !\nVous devez ruser pour placer ces doublons sans violer les règles sur la ligne principale du bloc.";
                            indiceNormal = new Indice("Technique Intra-bloc", msg, bloc.getListeCases(), solutions, false);
                        }
                    }
                }
            }
        }
        return indiceNormal; 
    }

    private boolean verifierTopologieGrandL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        int taille = cases.size();
        
        for (int i = 0; i < taille; i++) {
            Case candidateSortante = cases.get(i);
            Integer commonX = null;
            Integer commonY = null;
            
            for (int j = 0; j < taille; j++) {
                if (i == j) continue; 
                
                if (commonX == null) commonX = cases.get(j).getX();
                else if (commonX != cases.get(j).getX()) commonX = -1; 
                
                if (commonY == null) commonY = cases.get(j).getY();
                else if (commonY != cases.get(j).getY()) commonY = -1; 
            }
            
            if (commonX != null && commonX != -1 && candidateSortante.getX() != commonX) return true; 
            if (commonY != null && commonY != -1 && candidateSortante.getY() != commonY) return true; 
        }
        return false; 
    }
}