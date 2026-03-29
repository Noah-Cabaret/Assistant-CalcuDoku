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
 * Technique : Bloc Unique (Combinaison Unique).
 * Identifie un bloc où il n'existe mathématiquement qu'une seule combinaison de chiffres.
 * Incite le joueur à utiliser le mode annotation (candidats).
 */
public class TechniqueBlocUnique implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        
        // Listes pour stocker TOUS les indices trouvés sur la grille
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // Parcours complet de tous les blocs de la grille
        for (GroupementCases bloc : grille.getListeGroupements()) {
            List<Case> casesDuBloc = bloc.getListeCases();
            
            // On ignore les blocs d'une seule case
            if (casesDuBloc.size() <= 1) continue;

            List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
            
            // Si le bloc n'a qu'une seule combinaison mathématique possible
            if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
                
                List<Integer> combinaisonUnique = combinaisonsPossibles.get(0);
                
                // On évite les combinaisons avec des chiffres identiques
                if (aDesChiffresIdentiques(combinaisonUnique)) continue;

                boolean contientErreur = false;
                List<Case> casesFausses = new ArrayList<>();
                int nbCasesVides = 0;

                // Vérification de l'état des cases du bloc
                for (Case c : casesDuBloc) {
                    if (c.getValeur() == 0) {
                        nbCasesVides++;
                    } else if (c.getValeur() != c.getSolution()) {
                        contientErreur = true;
                        casesFausses.add(c);
                    }
                }

                // On ne déclenche l'indice normal que s'il reste plus d'une case vide
                // (Si 1 case vide, la technique "Dernière case du bloc" sera plus appropriée)
                if (!contientErreur && nbCasesVides <= 1) continue; 

                if (contientErreur) {
                    String msg = "Erreur détectée ! Ce bloc ne peut être résolu qu'avec une seule combinaison de chiffres précise.\nLes chiffres en surbrillance sont incorrects.";
                    indicesErreurs.add(new Indice("Combinaison Unique", msg, casesFausses, new HashMap<>(), true));
                } else {
                    // Construction de la chaîne affichant la combinaison (ex: "1, 2, 4")
                    String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                    
                    String msg = "Technique de combinaison unique : Observez ce bloc. Il n'existe qu'une seule combinaison de nombres possible (" 
                               + comboStr + ") pour atteindre ce résultat !\nUtilisez le mode annotation pour noter ces candidats dans les cases vides.";
                    
                    indicesNormaux.add(new Indice("Combinaison Unique", msg, casesDuBloc, new HashMap<>(), false));
                }
            }
        }

        // Sélection aléatoire d'un indice (priorité aux erreurs s'il y en a)
        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        }
        if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        }
        
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