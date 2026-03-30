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
 * Technique d'aide : Dernier Chiffre de la Grille.
 * Identifie un chiffre qui a déjà été placé un nombre suffisant de fois 
 * (taille - 1) dans la grille, permettant de déduire son dernier emplacement
 * par simple élimination des lignes et colonnes.
 */
public class TechniqueDernierChiffreGrille implements TechniqueAide {

    /**
     * Analyse la grille à la recherche d'un chiffre dont il ne manque plus qu'une occurrence.
     * @param grille La grille actuelle à analyser.
     * @return Un Indice contenant les messages progressifs, ou null si la technique ne s'applique pas.
     */
    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (int v = 1; v <= taille; v++) {
            int count = 0;
            boolean existingError = false; 
            boolean[] ligneContient = new boolean[taille];
            boolean[] colContient = new boolean[taille];
            List<Case> casesAvecValeur = new ArrayList<>();

            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    Case c = grille.getCase(x, y);
                    if (c.getValeur() == v) {
                        count++;
                        ligneContient[y] = true;
                        colContient[x] = true;
                        casesAvecValeur.add(c);
                        
                        if (c.getValeur() != c.getSolution()) {
                            existingError = true;
                        }
                    }
                }
            }

            if (count == taille - 1 && !existingError) {
                int ligneManquante = -1;
                int colManquante = -1;

                for (int i = 0; i < taille; i++) {
                    if (!ligneContient[i]) ligneManquante = i;
                    if (!colContient[i]) colManquante = i;
                }

                if (ligneManquante != -1 && colManquante != -1) {
                    Case caseCible = grille.getCase(colManquante, ligneManquante);
                    
                    Map<Case, Integer> solutions = new HashMap<>();
                    List<Case> surbrillance = new ArrayList<>();
                    List<String> messages = new ArrayList<>();

                    if (caseCible.getValeur() == 0) {
                        surbrillance.addAll(casesAvecValeur);
                        messages.add("Observez la grille dans son ensemble. Un chiffre en particulier a déjà été beaucoup placé.");
                        messages.add("Il ne manque plus qu'un seul exemplaire du chiffre " + v + " dans toute la grille.");
                        messages.add("Trouvez la dernière position du chiffre " + v + " par simple élimination avec les lignes et colonnes en surbrillance !");
                        
                        indicesNormaux.add(new Indice("Dernier Chiffre Restant", messages, surbrillance, solutions, false));
                    } 
                    else if (caseCible.getValeur() != caseCible.getSolution()) {
                        surbrillance.add(caseCible);
                        messages.add("Attention, une erreur s'est glissée sur le placement d'un chiffre très courant.");
                        messages.add("Il ne manque théoriquement qu'un seul exemplaire du chiffre " + v + ", mais sa seule place logique est occupée.");
                        messages.add("Erreur ! La case en surbrillance devrait contenir le dernier exemplaire du chiffre " + v + ", car toutes les autres lignes et colonnes en possèdent déjà un.");
                        
                        indicesErreurs.add(new Indice("Dernier Chiffre Restant", messages, surbrillance, solutions, true));
                    }
                }
            }
        }
        
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        else if (!indicesNormaux.isEmpty()) return indicesNormaux.get(random.nextInt(indicesNormaux.size()));

        return null;
    }
}