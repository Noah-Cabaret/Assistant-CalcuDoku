package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Technique d'aide : Intra-bloc pour les formes en "Grand L" (4+ cases).
 * Aide à déduire le placement astucieux de doublons obligatoires dans des blocs asymétriques.
 */
public class TechniqueIntraBloc_3_5 extends TechniqueIntraBloc {

    /**
     * Analyse les grands blocs nécessitant des doublons.
     * @param grille La grille à analyser.
     * @return Un Indice avec des messages progressifs.
     */
    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            if (bloc.getListeCases().size() >= 4 && verifierTopologieGrandL(bloc)) {
                
                List<List<Integer>> combosPossibles = bloc.getCombinaisonsMaths();
                List<List<Integer>> combosValides = getCombinaisonsValides(grille, bloc);
                
                if (combosValides.size() == 1) {
                    List<Integer> combinaisonUnique = combosValides.get(0);
                    
                    if (aDesChiffresIdentiques(combinaisonUnique)) {
                        
                        int nbCasesVides = 0;
                        boolean contientErreur = false;
                        List<Case> casesFausses = new ArrayList<>();

                        for (Case c : bloc.getListeCases()) {
                            if (c.getValeur() == 0) nbCasesVides++;
                            else if (c.getValeur() != c.getSolution()) { contientErreur = true; casesFausses.add(c); }
                        }

                        if (!contientErreur && nbCasesVides <= 1) continue;

                        Map<Case, Integer> solutions = new HashMap<>();
                        List<String> messages = new ArrayList<>();

                        if (contientErreur) {
                            messages.add("Un conflit a été détecté dans un grand bloc complexe.");
                            messages.add("Ce bloc nécessite obligatoirement des doublons, mais le placement actuel brise la règle d'unicité.");
                            messages.add("Erreur détectée ! Ce bloc allongé force l'utilisation d'un doublon. Les cases en surbrillance sont mal placées.");
                            indicesErreurs.add(new Indice("Technique Intra-bloc (Grand L)", messages, casesFausses, solutions, true));
                        } else {
                            String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                            
                            messages.add("Les grands blocs allongés ou coudés cachent souvent des déductions intéressantes basées sur les doublons.");
                            messages.add("Il ne reste qu'une seule combinaison valable pour ce grand bloc, et elle nécessite d'utiliser des chiffres en double.");
                            messages.add("Technique intra-bloc : La combinaison de ce grand bloc en surbrillance est (" + comboStr + "). Placez astucieusement les doublons pour éviter les conflits !");
                            
                            indicesNormaux.add(new Indice("Technique Intra-bloc (Grand L)", messages, bloc.getListeCases(), solutions, false));
                        }
                    }
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));

        return null; 
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