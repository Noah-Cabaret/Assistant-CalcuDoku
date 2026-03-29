package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Technique : Dernière Case Ligne / Colonne.
 * Identifie une ligne ou une colonne où il ne manque plus qu'une seule case, 
 * ou signale une erreur si toutes les autres cases de la ligne/colonne sont justes.
 */
public class TechniqueDerniereCaseLigneCol implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        // Listes pour stocker TOUS les indices trouvés sur l'ensemble de la grille
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // Parcours complet de la grille (lignes et colonnes)
        for (int i = 0; i < taille; i++) {
            Indice indLigne = chercherDerniereCase(grille, i, true);
            if (indLigne != null) { 
                if (indLigne.aUneErreur()) indicesErreurs.add(indLigne); 
                else indicesNormaux.add(indLigne); 
            }
            
            Indice indCol = chercherDerniereCase(grille, i, false);
            if (indCol != null) { 
                if (indCol.aUneErreur()) indicesErreurs.add(indCol); 
                else indicesNormaux.add(indCol); 
            }
        }
        
        // Sélection aléatoire d'un seul indice parmi tous ceux détectés (priorité aux erreurs)
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        } else if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }

        return null;
    }

    private Indice chercherDerniereCase(Grille grille, int index, boolean estLigne) {
        int taille = grille.getTaille();
        int nbCasesVides = 0;
        int nbErreurs = 0;
        Case caseFausse = null;

        // Étape 1 : Analyser le contenu de la ligne ou de la colonne
        for (int i = 0; i < taille; i++) {
            Case c = grille.getCase(estLigne ? i : index, estLigne ? index : i);
            if (c.getValeur() == 0) {
                nbCasesVides++;
            } else if (c.getValeur() != c.getSolution()) {
                nbErreurs++;
                caseFausse = c;
            }
        }

        Map<Case, Integer> solutions = new HashMap<>();
        List<Case> casesASurbriller = new ArrayList<>();

        // Cas 1 : Indice normal -> Exactement 1 case vide et toutes les autres sont justes
        if (nbCasesVides == 1 && nbErreurs == 0) {
            for (int i = 0; i < taille; i++) {
                casesASurbriller.add(grille.getCase(estLigne ? i : index, estLigne ? index : i));
            }
            String message = "Déduction logique : Regardez cette " + (estLigne ? "ligne" : "colonne") + ". Il ne manque plus qu'une seule case pour la compléter, vous pouvez facilement déduire sa valeur !";
            return new Indice("Dernière Case", message, casesASurbriller, solutions, false);
        }

        // Cas 2 : Erreur détectée -> La ligne est remplie (0 vide) mais une seule case est fausse
        if (nbCasesVides == 0 && nbErreurs == 1) {
            casesASurbriller.add(caseFausse);
            String message = "Erreur détectée ! Les autres cases de cette zone sont justes, mais la valeur de cette case est incorrecte.";
            return new Indice("Dernière Case", message, casesASurbriller, solutions, true);
        }

        return null;
    }
}