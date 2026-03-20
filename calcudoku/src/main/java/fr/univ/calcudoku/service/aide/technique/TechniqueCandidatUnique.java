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

                // LOGIQUE MATHÉMATIQUE : Croisement des occurences
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

                if (chiffresVus.size() == taille - 1) {
                    int chiffreManquant = trouverChiffreManquant(chiffresVus, taille);
                    int valeurJoueur = c.getValeur();
                    
                    if (valeurJoueur == chiffreManquant) continue;

                    // VÉRIFICATION ERREUR 
                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != c.getSolution());

                    // Bypass du filtre si c'est une erreur
                    if (!contientErreur && (nbSurLigne == taille - 1 || nbSurColonne == taille - 1)) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    casesASurbriller.add(c);
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(c, chiffreManquant);

                    if (contientErreur) {
                        String msg = "Erreur détectée ! La case située à la ligne " + (y + 1) + " et colonne " + (x + 1) + " "
                                   + "ne peut contenir que le chiffre " + chiffreManquant + " à cause des autres chiffres croisés.";
                        return new Indice("Candidat Unique", msg, casesASurbriller, solutions, true);
                    } else if (indiceNormal == null) {
                        String msg = "Regardez la case située à la ligne " + (y + 1) + " et à la colonne " + (x + 1) + ".\n"
                                   + "En croisant les chiffres de sa ligne et sa colonne, il ne reste qu'une possibilité : le chiffre " + chiffreManquant + ".";
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