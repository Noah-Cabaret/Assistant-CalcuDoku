package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Technique : Candidat Unique.
 * Cherche une case vide à l'intersection d'une ligne et d'une colonne très remplies,
 * ne laissant qu'un seul chiffre possible.
 */
public class TechniqueCandidatUnique implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        Indice indiceNormal = null;

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Case c = grille.getCase(x, y);

                Set<Integer> chiffresVus = new HashSet<>();
                int nbSurLigne = 0, nbSurColonne = 0;

                // Scan en croix (Ligne + Colonne)
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

                // Si (Taille - 1) chiffres différents ont été vus, il n'en reste qu'un
                if (chiffresVus.size() == taille - 1) {
                    int chiffreManquant = trouverChiffreManquant(chiffresVus, taille);
                    int valeurJoueur = c.getValeur();
                    
                    if (valeurJoueur == chiffreManquant) continue;

                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != c.getSolution());
                    
                    // Laisse la priorité à "Dernière case Ligne/Col" si c'est plus simple
                    if (!contientErreur && (nbSurLigne == taille - 1 || nbSurColonne == taille - 1)) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    casesASurbriller.add(c);
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(c, chiffreManquant);

                    if (contientErreur) {
                        String msg = "Erreur détectée ! La case ciblée ne peut contenir que le chiffre " + chiffreManquant + " à cause des autres chiffres présents sur sa ligne et sa colonne.";
                        return new Indice("Candidat Unique", msg, casesASurbriller, solutions, true);
                    } else if (indiceNormal == null) {
                        String msg = "Techniques à candidat unique : Selon les règles, un nombre n'apparaît qu'une fois par ligne et colonne. En croisant la ligne et la colonne de cette case, il ne reste plus qu'un seul candidat possible !";
                        indiceNormal = new Indice("Candidat Unique", msg, casesASurbriller, solutions, false);
                    }
                }
            }
        }
        return indiceNormal;
    }

    private int trouverChiffreManquant(Set<Integer> chiffresVus, int taille) {
        for (int n = 1; n <= taille; n++) if (!chiffresVus.contains(n)) return n;
        return -1;
    }
}