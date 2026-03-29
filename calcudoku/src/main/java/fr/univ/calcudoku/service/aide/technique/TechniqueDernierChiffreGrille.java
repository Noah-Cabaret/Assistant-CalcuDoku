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
 * Technique : Dernier Chiffre de la Grille.
 * Identifie un chiffre qui a déjà été placé 'taille - 1' fois dans la grille.
 * Sa dernière position peut être déduite par élimination.
 */
public class TechniqueDernierChiffreGrille implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        // Listes pour stocker TOUS les indices trouvés
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // On vérifie chaque chiffre possible (ex: de 1 à 5 pour une grille 5x5)
        for (int v = 1; v <= taille; v++) {
            int count = 0;
            boolean existingError = false; // Sécurité : on vérifie que les chiffres déjà placés sont justes
            boolean[] ligneContient = new boolean[taille];
            boolean[] colContient = new boolean[taille];
            List<Case> casesAvecValeur = new ArrayList<>();

            // Parcours complet de la grille pour compter les occurrences du chiffre 'v'
            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    Case c = grille.getCase(x, y);
                    if (c.getValeur() == v) {
                        count++;
                        ligneContient[y] = true;
                        colContient[x] = true;
                        casesAvecValeur.add(c);
                        
                        // Si le joueur a mal placé l'un de ces chiffres, on marque une erreur existante
                        if (c.getValeur() != c.getSolution()) {
                            existingError = true;
                        }
                    }
                }
            }

            // On ne déclenche l'indice que s'il y a exactement 'taille - 1' exemplaires ET qu'ils sont tous bien placés
            if (count == taille - 1 && !existingError) {
                int ligneManquante = -1;
                int colManquante = -1;

                for (int i = 0; i < taille; i++) {
                    if (!ligneContient[i]) ligneManquante = i;
                    if (!colContient[i]) colManquante = i;
                }

                if (ligneManquante != -1 && colManquante != -1) {
                    Case caseCible = grille.getCase(colManquante, ligneManquante);
                    
                    Map<Case, Integer> solutions = new HashMap<>(); // Vide
                    List<Case> surbrillance = new ArrayList<>();

                    // Cas 1 : La case est vide -> Indice normal
                    if (caseCible.getValeur() == 0) {
                        surbrillance.addAll(casesAvecValeur);
                        String msg = "Il ne manque plus qu'un seul exemplaire du chiffre " + v + " dans toute la grille.\nTrouvez sa dernière position par simple élimination des lignes et colonnes !";
                        indicesNormaux.add(new Indice("Dernier Chiffre Restant", msg, surbrillance, solutions, false));
                    } 
                    // Cas 2 : La case contient une valeur mais c'est faux (ça devrait être 'v') -> Erreur
                    else if (caseCible.getValeur() != caseCible.getSolution()) {
                        surbrillance.add(caseCible);
                        String msg = "Erreur ! Cette case devrait contenir le dernier exemplaire du chiffre " + v + ", car toutes les autres lignes et colonnes en possèdent déjà un.";
                        indicesErreurs.add(new Indice("Dernier Chiffre Restant", msg, surbrillance, solutions, true));
                    }
                }
            }
        }
        
        // Sélection aléatoire d'un seul indice (priorité aux erreurs signalées)
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        } else if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }

        return null;
    }
}