package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Technique : Place Unique en Ligne/Colonne.
 * Trouve une ligne ou une colonne où un chiffre n'a plus qu'une seule case disponible
 * par déduction logique et processus d'élimination.
 */
public class TechniquePlaceUniqueLigneColonne implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        // Listes pour stocker TOUS les indices trouvés
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // Parcours complet des lignes et des colonnes
        for (int i = 0; i < taille; i++) {
            chercherPlaceUnique(grille, i, true, indicesErreurs, indicesNormaux);
            chercherPlaceUnique(grille, i, false, indicesErreurs, indicesNormaux);
        }

        // Sélection aléatoire avec priorité aux erreurs
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        } else if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }
        
        return null;
    }

    private void chercherPlaceUnique(Grille grille, int indexLigneOuCol, boolean estLigne, List<Indice> indicesErreurs, List<Indice> indicesNormaux) {
        int taille = grille.getTaille();
        int nbCasesVides = 0;

        // Comptage des cases vides dans la ligne/colonne
        for (int i = 0; i < taille; i++) {
            Case c = grille.getCase(estLigne ? i : indexLigneOuCol, estLigne ? indexLigneOuCol : i);
            if (c.getValeur() == 0) nbCasesVides++;
        }

        // On teste chaque chiffre de 1 à 'taille'
        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            
            // Si le chiffre est déjà présent dans la ligne/colonne, on l'ignore
            if (chiffreDejaPlace(grille, indexLigneOuCol, estLigne, chiffre)) continue;
            
            // Si le chiffre n'a plus qu'une seule occurrence manquante dans TOUTE la grille, 
            // la technique "Dernier Chiffre Grille" s'en chargera
            if (compterOccurrencesGrille(grille, chiffre) >= taille - 1) continue;

            List<Case> casesPossibles = new ArrayList<>();
            
            // On cherche toutes les cases de la ligne/colonne qui peuvent accueillir ce chiffre
            for (int i = 0; i < taille; i++) {
                int x = estLigne ? i : indexLigneOuCol;
                int y = estLigne ? indexLigneOuCol : i;
                Case c = grille.getCase(x, y);

                if (c.getValeur() != chiffre) {
                    // On vérifie les règles du Sudoku ET les règles mathématiques du bloc
                    if (grille.estCoupValide(x, y, chiffre) && blocAccepteChiffre(c.getGroupement(), chiffre)) {
                        casesPossibles.add(c);
                    }
                }
            }

            // S'il n'y a mathématiquement qu'UNE SEULE case possible pour ce chiffre
            if (casesPossibles.size() == 1) {
                Case caseCible = casesPossibles.get(0);
                
                // Sécurité anti-fausse piste : si le joueur a fait une erreur ailleurs qui 
                // fausse notre déduction logique, on annule cet indice pour ne pas l'induire en erreur.
                if (caseCible.getSolution() != chiffre) continue;

                // On laisse la technique "Bloc de 1" s'occuper des cases seules
                if (caseCible.getGroupement().getListeCases().size() == 1) continue; 

                int valeurJoueur = caseCible.getValeur();
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution()); 
                
                // On laisse la technique "Dernière Case Ligne/Colonne" agir s'il n'y a qu'une seule case vide
                if (!contientErreur && nbCasesVides <= 1) continue;

                Map<Case, Integer> solutions = new HashMap<>();
                List<Case> casesASurbriller = new ArrayList<>();
                String axe = estLigne ? "la ligne " + (indexLigneOuCol + 1) : "la colonne " + (indexLigneOuCol + 1);
                
                if (contientErreur) {
                    casesASurbriller.add(caseCible);
                    String msg = "Erreur détectée ! Regardez " + axe + ".\n" +
                                 "À cause des intersections et des blocs, le chiffre " + chiffre + " ne peut aller que dans cette case, mais elle contient autre chose.";
                    indicesErreurs.add(new Indice("Place Unique", msg, casesASurbriller, solutions, true));
                } else {
                    for (int i = 0; i < taille; i++) {
                        casesASurbriller.add(grille.getCase(estLigne ? i : indexLigneOuCol, estLigne ? indexLigneOuCol : i));
                    }
                    String msg = "Technique de la place unique : Regardez " + axe + " en surbrillance.\n" +
                                 "Par processus d'élimination (grâce aux autres colonnes/lignes et blocs), le chiffre " + chiffre + " ne peut être placé que dans une seule case !";
                    indicesNormaux.add(new Indice("Place Unique", msg, casesASurbriller, solutions, false));
                }
            }
        }
    }

    private boolean chiffreDejaPlace(Grille grille, int index, boolean estLigne, int chiffre) {
        for (int i = 0; i < grille.getTaille(); i++) {
            if (grille.getCase(estLigne ? i : index, estLigne ? index : i).getValeur() == chiffre) return true;
        }
        return false;
    }

    private int compterOccurrencesGrille(Grille grille, int chiffre) {
        int count = 0;
        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                if (grille.getCase(x, y).getValeur() == chiffre) count++;
            }
        }
        return count;
    }

    private boolean blocAccepteChiffre(GroupementCases bloc, int chiffre) {
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) return true;
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (combinaison.contains(chiffre)) return true;
        }
        return false;
    }
}