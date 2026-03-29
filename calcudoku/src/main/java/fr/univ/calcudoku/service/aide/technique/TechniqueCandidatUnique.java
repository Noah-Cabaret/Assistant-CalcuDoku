package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Technique d'aide : Candidat Unique (Cross-Hatching).
 * Identifie une case précise qui ne peut prendre qu'un seul chiffre possible 
 * en observant l'intersection de sa ligne et de sa colonne.
 */
public class TechniqueCandidatUnique implements TechniqueAide {

    /**
     * Analyse chaque case de la grille en croisant les informations de sa ligne et sa colonne.
     * Fournit un indice progressif pour le joueur.
     * * @param grille La grille à analyser.
     * @return Un Indice contenant les messages d'aide progressifs, ou null.
     */
    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Case c = grille.getCase(x, y);

                Set<Integer> chiffresVus = new HashSet<>();
                int nbSurLigne = 0, nbSurColonne = 0;

                for (int i = 0; i < taille; i++) {
                    if (i != x) {
                        int val = grille.getCase(i, y).getValeur();
                        if (val != 0) { chiffresVus.add(val); nbSurLigne++; }
                    }
                    if (i != y) {
                        int val = grille.getCase(x, i).getValeur();
                        if (val != 0) { chiffresVus.add(val); nbSurColonne++; }
                    }
                }

                // Si on a vu N-1 chiffres différents dans la croix formée par la ligne et la colonne
                if (chiffresVus.size() == taille - 1) {
                    int chiffreManquant = trouverChiffreManquant(chiffresVus, taille);
                    int valeurJoueur = c.getValeur();
                    
                    if (valeurJoueur == chiffreManquant) continue;

                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != c.getSolution());
                    
                    if (!contientErreur && (nbSurLigne == taille - 1 || nbSurColonne == taille - 1)) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    casesASurbriller.add(c);
                    Map<Case, Integer> solutions = new HashMap<>(); 
                    List<String> messages = new ArrayList<>();

                    if (contientErreur) {
                        messages.add("Une erreur de placement s'est produite. Une de vos cases contient un chiffre qui entre en conflit avec son environnement.");
                        messages.add("Si vous regardez les chiffres déjà présents sur la ligne et la colonne de cette case, vous verrez qu'il n'y avait qu'une seule option valide.");
                        messages.add("Erreur détectée ! La case en surbrillance ne peut contenir qu'un seul chiffre possible à cause des autres chiffres déjà présents sur sa ligne et sa colonne.");
                        
                        indicesErreurs.add(new Indice("Candidat Unique", messages, casesASurbriller, solutions, true));
                    } else {
                        messages.add("Concentrez-vous sur une case en particulier. Son environnement immédiat limite énormément ses possibilités.");
                        messages.add("Regardez les chiffres déjà placés en formant une croix : prenez en compte à la fois la ligne et la colonne de la case vide.");
                        messages.add("Technique du candidat unique : En croisant la ligne et la colonne de la case en surbrillance, il ne reste plus qu'un seul candidat possible pour la remplir !");
                        
                        indicesNormaux.add(new Indice("Candidat Unique", messages, casesASurbriller, solutions, false));
                    }
                }
            }
        }

        // Sélection aléatoire
        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null;
    }

    /**
     * Identifie le chiffre manquant dans un ensemble contenant N-1 valeurs.
     * * @param chiffresVus L'ensemble des chiffres déjà présents.
     * @param taille      La valeur maximale possible (taille de la grille).
     * @return Le chiffre manquant de 1 à taille.
     */
    private int trouverChiffreManquant(Set<Integer> chiffresVus, int taille) {
        for (int n = 1; n <= taille; n++) {
            if (!chiffresVus.contains(n)) return n;
        }
        return -1;
    }
}