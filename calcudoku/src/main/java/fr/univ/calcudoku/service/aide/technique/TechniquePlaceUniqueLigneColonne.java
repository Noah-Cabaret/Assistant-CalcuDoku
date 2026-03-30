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
 * Technique d'aide : Place Unique en Ligne/Colonne.
 * Détecte une ligne ou une colonne où un chiffre précis n'a plus qu'une 
 * seule case disponible grâce au processus d'élimination (règles des lignes/colonnes/blocs).
 */
public class TechniquePlaceUniqueLigneColonne implements TechniqueAide {

    /**
     * Parcourt la grille pour trouver une déduction logique de "place unique" pour un chiffre.
     * * @param grille La grille actuelle à analyser.
     * @return Un Indice contenant les messages d'aide progressifs, ou null.
     */
    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // Parcours complet des lignes et des colonnes
        for (int i = 0; i < taille; i++) {
            chercherPlaceUnique(grille, i, true, indicesErreurs, indicesNormaux);
            chercherPlaceUnique(grille, i, false, indicesErreurs, indicesNormaux);
        }

        Random random = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        } else if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }
        
        return null;
    }

    /**
     * Logique de recherche pour une ligne ou colonne donnée. Identifie les places possibles
     * d'un chiffre et construit l'indice si le chiffre n'a qu'un seul emplacement valide.
     * * @param grille           La grille analysée.
     * @param indexLigneOuCol  L'index de la zone.
     * @param estLigne         Vrai si on analyse une ligne, faux pour une colonne.
     * @param indicesErreurs   Liste où ajouter les indices de type erreur.
     * @param indicesNormaux   Liste où ajouter les indices de progression normaux.
     */
    private void chercherPlaceUnique(Grille grille, int indexLigneOuCol, boolean estLigne, List<Indice> indicesErreurs, List<Indice> indicesNormaux) {
        int taille = grille.getTaille();
        int nbCasesVides = 0;

        for (int i = 0; i < taille; i++) {
            Case c = grille.getCase(estLigne ? i : indexLigneOuCol, estLigne ? indexLigneOuCol : i);
            if (c.getValeur() == 0) nbCasesVides++;
        }

        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            if (chiffreDejaPlace(grille, indexLigneOuCol, estLigne, chiffre)) continue;
            if (compterOccurrencesGrille(grille, chiffre) >= taille - 1) continue;

            List<Case> casesPossibles = new ArrayList<>();
            
            for (int i = 0; i < taille; i++) {
                int x = estLigne ? i : indexLigneOuCol;
                int y = estLigne ? indexLigneOuCol : i;
                Case c = grille.getCase(x, y);

                if (c.getValeur() != chiffre) {
                    if (grille.estCoupValide(x, y, chiffre) && blocAccepteChiffre(c.getGroupement(), chiffre)) {
                        casesPossibles.add(c);
                    }
                }
            }

            if (casesPossibles.size() == 1) {
                Case caseCible = casesPossibles.get(0);
                
                if (caseCible.getSolution() != chiffre) continue;
                if (caseCible.getGroupement().getListeCases().size() == 1) continue; 

                int valeurJoueur = caseCible.getValeur();
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution()); 
                
                if (!contientErreur && nbCasesVides <= 1) continue;

                Map<Case, Integer> solutions = new HashMap<>();
                List<Case> casesASurbriller = new ArrayList<>();
                List<String> messages = new ArrayList<>();
                String axeStr = estLigne ? "la ligne " + (indexLigneOuCol + 1) : "la colonne " + (indexLigneOuCol + 1);
                
                if (contientErreur) {
                    casesASurbriller.add(caseCible);
                    messages.add("Il semble qu'un chiffre ait été placé au mauvais endroit, bloquant la résolution logique d'une ligne ou d'une colonne.");
                    messages.add("En croisant les informations de la grille (lignes, colonnes, calculs des blocs), un certain chiffre n'avait en réalité qu'une seule place valide ici.");
                    messages.add("Erreur détectée ! Regardez " + axeStr + ".\nÀ cause des intersections, le chiffre " + chiffre + " ne peut aller que dans la case en surbrillance, mais elle contient autre chose.");
                    
                    indicesErreurs.add(new Indice("Place Unique", messages, casesASurbriller, solutions, true));
                } else {
                    for (int i = 0; i < taille; i++) {
                        casesASurbriller.add(grille.getCase(estLigne ? i : indexLigneOuCol, estLigne ? indexLigneOuCol : i));
                    }
                    messages.add("Analysez les lignes et les colonnes. Un chiffre spécifique cherche sa place et n'a plus beaucoup d'options.");
                    messages.add("En utilisant le processus d'élimination (vérifiez les chiffres présents sur les autres axes et les règles des blocs), vous pouvez restreindre ses emplacements possibles.");
                    messages.add("Technique de la place unique : Regardez " + axeStr + " en surbrillance.\nPar processus d'élimination, le chiffre " + chiffre + " ne peut être placé que dans une seule case !");
                    
                    indicesNormaux.add(new Indice("Place Unique", messages, casesASurbriller, solutions, false));
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