package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique : Place Unique en Ligne/Colonne.
 * Trouve une ligne ou une colonne où un chiffre n'a plus qu'une seule case disponible.
 */
public class TechniquePlaceUniqueLigneColonne implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        Indice indiceNormal = null;

        for (int i = 0; i < taille; i++) {
            Indice indLigne = chercherPlaceUnique(grille, i, true);
            if (indLigne != null) { if (indLigne.aUneErreur()) return indLigne; if (indiceNormal == null) indiceNormal = indLigne; }

            Indice indCol = chercherPlaceUnique(grille, i, false);
            if (indCol != null) { if (indCol.aUneErreur()) return indCol; if (indiceNormal == null) indiceNormal = indCol; }
        }
        return indiceNormal;
    }

    private Indice chercherPlaceUnique(Grille grille, int indexLigneOuCol, boolean estLigne) {
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
                if (caseCible.getGroupement().getListeCases().size() == 1) continue; 

                int valeurJoueur = caseCible.getValeur();
                if (valeurJoueur == chiffre) continue; 

                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution()); 
                if (!contientErreur && nbCasesVides <= 1) continue;

                Map<Case, Integer> solutions = new HashMap<>();
                List<Case> casesASurbriller = new ArrayList<>();
                String axe = estLigne ? "la ligne " + (indexLigneOuCol + 1) : "la colonne " + (indexLigneOuCol + 1);
                
                if (contientErreur) {
                    casesASurbriller.add(caseCible);
                    return new Indice("Place Unique", "Erreur détectée ! Regardez " + axe + ".\n" +
                              "Le chiffre " + chiffre + " ne peut mathématiquement aller que dans cette case.", casesASurbriller, solutions, true);
                } else {
                    for (int i = 0; i < taille; i++) casesASurbriller.add(grille.getCase(estLigne ? i : indexLigneOuCol, estLigne ? indexLigneOuCol : i));
                    return new Indice("Place Unique", "Techniques uniques cachées : Regardez " + axe + ".\nPar processus d'élimination, un certain chiffre ne peut être placé que dans une seule case. Trouvez-le !", casesASurbriller, solutions, false);
                }
            }
        }
        return null;
    }

    private boolean chiffreDejaPlace(Grille grille, int index, boolean estLigne, int chiffre) {
        for (int i = 0; i < grille.getTaille(); i++) if (grille.getCase(estLigne ? i : index, estLigne ? index : i).getValeur() == chiffre) return true;
        return false;
    }

    private int compterOccurrencesGrille(Grille grille, int chiffre) {
        int count = 0;
        for (int y = 0; y < grille.getTaille(); y++) for (int x = 0; x < grille.getTaille(); x++) if (grille.getCase(x, y).getValeur() == chiffre) count++;
        return count;
    }

    private boolean blocAccepteChiffre(GroupementCases bloc, int chiffre) {
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) return true;
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) if (combinaison.contains(chiffre)) return true;
        return false;
    }
}