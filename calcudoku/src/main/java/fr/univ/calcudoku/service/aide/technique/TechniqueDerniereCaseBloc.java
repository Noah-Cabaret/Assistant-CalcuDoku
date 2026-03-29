package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Technique : Dernière Case du Bloc.
 * Identifie un bloc où toutes les cases sont remplies correctement, sauf une seule qui est vide.
 */
public class TechniqueDerniereCaseBloc implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        // Liste pour stocker TOUS les indices normaux trouvés sur l'ensemble de la grille
        List<Indice> indicesNormaux = new ArrayList<>();

        // 1. Récupérer tous les blocs uniques de la grille pour éviter les doublons
        Set<GroupementCases> tousLesBlocs = new HashSet<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                GroupementCases bloc = grille.getCase(i, j).getGroupement();
                if (bloc != null) {
                    tousLesBlocs.add(bloc);
                }
            }
        }

        // 2. Parcourir et analyser chaque bloc de la grille
        for (GroupementCases bloc : tousLesBlocs) {
            
            // On ignore les blocs de 1 seule case (une autre technique comme TechniqueBlocUnique s'en charge)
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            boolean contientErreur = false;

            // On inspecte les cases du bloc
            for (Case c : bloc.getListeCases()) {
                if (c.getValeur() == 0) {
                    nbCasesVides++;
                } else if (c.getValeur() != c.getSolution()) {
                    contientErreur = true; // Une case déjà remplie est fausse
                }
            }

            // CORRECTION DU BUG : 
            // On ne déclenche l'indice QUE s'il reste EXACTEMENT 1 case vide (nbCasesVides == 1).
            // On s'assure aussi que les autres cases sont justes pour ne pas donner un calcul faussé.
            if (nbCasesVides == 1 && !contientErreur) {
                
                List<Case> surbrillance = new ArrayList<>(bloc.getListeCases());
                Map<Case, Integer> solutions = new HashMap<>();
                
                String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                String message = "Technique de la dernière case : Il ne reste qu'une seule case vide dans ce bloc.\n"
                               + "Vous pouvez facilement déduire sa valeur en utilisant l'opération (" 
                               + symbole + ") et le résultat cible (" + bloc.getResultatCible() + ").";
                
                // On ajoute l'indice trouvé à notre liste
                indicesNormaux.add(new Indice("Dernière Case du Bloc", message, surbrillance, solutions, false));
            }
        }

        // 3. Sélection aléatoire d'un indice parmi tous ceux détectés sur la grille
        if (!indicesNormaux.isEmpty()) {
            Random random = new Random();
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }

        return null;
    }
}