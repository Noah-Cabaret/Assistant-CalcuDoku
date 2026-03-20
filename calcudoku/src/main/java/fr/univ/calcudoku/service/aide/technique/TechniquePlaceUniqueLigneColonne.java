package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniquePlaceUniqueLigneColonne implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceErreur = null;
        int taille = grille.getTaille();

        for (int i = 0; i < taille; i++) {
            Indice indLigne = chercherPlaceUnique(grille, i, true);
            if (indLigne != null) {
                if (!indLigne.aUneErreur()) return indLigne;
                if (indiceErreur == null) indiceErreur = indLigne;
            }

            Indice indCol = chercherPlaceUnique(grille, i, false);
            if (indCol != null) {
                if (!indCol.aUneErreur()) return indCol;
                if (indiceErreur == null) indiceErreur = indCol;
            }
        }

        return indiceErreur;
    }

    private Indice chercherPlaceUnique(Grille grille, int indexLigneOuCol, boolean estLigne) {
        int taille = grille.getTaille();
        
        // MODIFICATION : Suppression de la vérification (nbCasesVides <= 1)
        // On veut continuer l'analyse pour trouver les erreurs même si la grille est pleine.

        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            if (chiffreDejaPlace(grille, indexLigneOuCol, estLigne, chiffre)) {
                continue;
            }

            if (compterOccurrencesGrille(grille, chiffre) >= taille - 1) {
                continue;
            }

            List<Case> casesPossibles = new ArrayList<>();

            for (int i = 0; i < taille; i++) {
                int x = estLigne ? i : indexLigneOuCol;
                int y = estLigne ? indexLigneOuCol : i;
                Case c = grille.getCase(x, y);

                // MODIFICATION : On vérifie aussi les cases remplies par le joueur (pour détecter l'erreur)
                if (c.getValeur() != chiffre) {
                    if (grille.estCoupValide(x, y, chiffre) && blocAccepteChiffre(c.getGroupement(), chiffre)) {
                        casesPossibles.add(c);
                    }
                }
            }

            if (casesPossibles.size() == 1) {
                Case caseCible = casesPossibles.get(0);
                GroupementCases bloc = caseCible.getGroupement();
                
                if (bloc.getListeCases().size() == 1) {
                    continue; 
                }

                // MODIFICATION : Suppression du filtre B (Dernière case vide du bloc).
                // Il bloquait la détection des erreurs si le joueur avait rempli tout le bloc.

                int valeurJoueur = caseCible.getValeur();
                if (valeurJoueur == chiffre) continue; // Si correct, on passe

                // MODIFICATION : Paramétrage de l'erreur
                boolean contientErreur = (valeurJoueur != 0); 

                Map<Case, Integer> solutions = new HashMap<>();
                solutions.put(caseCible, chiffre);

                List<Case> casesASurbriller = new ArrayList<>();
                for (int i = 0; i < taille; i++) {
                    int x = estLigne ? i : indexLigneOuCol;
                    int y = estLigne ? indexLigneOuCol : i;
                    casesASurbriller.add(grille.getCase(x, y));
                }

                String axe = estLigne ? "la ligne " + (indexLigneOuCol + 1) : "la colonne " + (indexLigneOuCol + 1);
                String nom = "Place Unique en " + (estLigne ? "Ligne" : "Colonne");
                String message;
                
                // MODIFICATION : Message d'erreur dynamique
                if (contientErreur) {
                    message = "Erreur détectée ! Regardez " + axe + ".\n" +
                              "Le chiffre " + chiffre + " ne peut mathématiquement aller que dans cette case. " +
                              "Votre valeur " + valeurJoueur + " est donc incorrecte.";
                } else {
                    message = "Regardez " + axe + ".\n" +
                              "Le chiffre " + chiffre + " doit obligatoirement y figurer.\n" +
                              "Toutes les autres cases de cette zone sont bloquées. Il n'y a qu'un seul endroit possible !";
                }

                return new Indice(nom, message, casesASurbriller, solutions, contientErreur);
            }
        }
        return null;
    }

    private boolean chiffreDejaPlace(Grille grille, int index, boolean estLigne, int chiffre) {
        for (int i = 0; i < grille.getTaille(); i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            if (grille.getCase(x, y).getValeur() == chiffre) {
                return true;
            }
        }
        return false;
    }

    private int compterOccurrencesGrille(Grille grille, int chiffre) {
        int count = 0;
        int taille = grille.getTaille();
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                if (grille.getCase(x, y).getValeur() == chiffre) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean blocAccepteChiffre(GroupementCases bloc, int chiffre) {
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (combinaison.contains(chiffre)) {
                return true;
            }
        }
        return false;
    }
}