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
        int nbCasesVides = 0;

        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : indexLigneOuCol;
            int y = estLigne ? indexLigneOuCol : i;
            if (grille.getCase(x, y).getValeur() == 0) {
                nbCasesVides++;
            }
        }

        if (nbCasesVides <= 1) {
            return null;
        }

        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            if (chiffreDejaPlace(grille, indexLigneOuCol, estLigne, chiffre)) {
                continue;
            }

            if (compterOccurrencesGrille(grille, chiffre) >= taille - 1) {
                continue;
            }

            List<Case> casesPossibles = new ArrayList<>();

            // ÉTAPE 1 : On liste honnêtement TOUTES les places valides
            for (int i = 0; i < taille; i++) {
                int x = estLigne ? i : indexLigneOuCol;
                int y = estLigne ? indexLigneOuCol : i;
                Case c = grille.getCase(x, y);

                if (c.getValeur() == 0) {
                    if (grille.estCoupValide(x, y, chiffre) && blocAccepteChiffre(c.getGroupement(), chiffre)) {
                        casesPossibles.add(c);
                    }
                }
            }

            // ÉTAPE 2 : S'il n'y a VRAIMENT qu'une seule place, on applique nos filtres de difficulté
            if (casesPossibles.size() == 1) {
                Case caseCible = casesPossibles.get(0);
                GroupementCases bloc = caseCible.getGroupement();
                
                // Filtre A : Est-ce un bloc de 1 case ?
                if (bloc.getListeCases().size() == 1) {
                    continue; 
                }

                // Filtre B : Est-ce la dernière case vide de son bloc ?
                int casesVidesDuBloc = 0;
                for (Case caseDuBloc : bloc.getListeCases()) {
                    if (caseDuBloc.getValeur() == 0) {
                        casesVidesDuBloc++;
                    }
                }
                if (casesVidesDuBloc == 1) {
                    continue; // On annule, c'est trop facile, une autre aide s'en chargera
                }

                // Si ça passe les filtres, on génère l'aide !
                boolean contientErreur = false; 

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
                String message = "Regardez " + axe + ".\n" +
                                 "Le chiffre " + chiffre + " doit obligatoirement y figurer.\n" +
                                 "Toutes les autres cases de cette " + (estLigne ? "ligne" : "colonne") + 
                                 " sont bloquées (soit par les colonnes/lignes croisées, soit parce que leur bloc mathématique ne permet pas d'avoir un " + chiffre + ").\n" +
                                 "Il n'y a donc qu'un seul endroit possible pour le placer !";

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