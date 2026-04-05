package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurScannerAxe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Technique d'aide : Place Unique sur Ligne/Colonne.
 * Identifie un chiffre qui ne peut aller qu'à un seul endroit sur un axe donné.
 */
public class TechniquePlaceUniqueLigneColonne implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        List<Indice> indicesNormaux = new ArrayList<>();
        List<Indice> indicesErreurs = new ArrayList<>();

        for (int i = 0; i < taille; i++) {
            testerAxe(grille, i, true, indicesErreurs, indicesNormaux);
            testerAxe(grille, i, false, indicesErreurs, indicesNormaux);
        }

        Random random = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        return null;
    }

    private void testerAxe(Grille grille, int index, boolean estLigne, List<Indice> erreurs, List<Indice> normaux) {
        int taille = grille.getTaille();
        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            List<Case> placesPossibles = new ArrayList<>();
            for (int j = 0; j < taille; j++) {
                Case c = grille.getCase(estLigne ? j : index, estLigne ? index : j);
                if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                    placesPossibles.add(c);
                }
            }

            if (placesPossibles.size() == 1) {
                Case cible = placesPossibles.get(0);
                // Utilisation du scanner pour voir si le chiffre est déjà "bloqué" par erreur ailleurs
                VisiteurScannerAxe scanner = new VisiteurScannerAxe(index, estLigne, Collections.singletonList(cible), Collections.singletonList(chiffre));
                scanner.visiter(grille);

                List<String> messages = new ArrayList<>();
                String axeStr = estLigne ? "la ligne " + (index + 1) : "la colonne " + (index + 1);
                
                messages.add("Un chiffre spécifique n'a plus qu'une seule place possible dans " + axeStr + ".");
                messages.add("Technique de la place unique : Le chiffre " + chiffre + " ne peut aller que dans la case en surbrillance !");
                
                normaux.add(new Indice("Place Unique", messages, Collections.singletonList(cible), new HashMap<>(), false));
            }
        }
    }
}