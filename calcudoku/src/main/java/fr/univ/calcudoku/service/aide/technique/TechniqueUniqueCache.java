package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique : Unique Caché (Bloc).
 * Identifie un chiffre qui doit obligatoirement être dans le bloc,
 * et démontre qu'il n'a qu'une seule case physiquement disponible.
 */
public class TechniqueUniqueCache implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            for(Case c : bloc.getListeCases()) if (c.getValeur() == 0) nbCasesVides++;

            int taille = grille.getTaille();

            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                if (blocContientChiffreValide(bloc, chiffre)) continue; 
                
                // Le chiffre doit être OBLIGATOIRE dans les mathématiques du bloc
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                // Cherche toutes les places non menacées par un même chiffre sur la ligne/colonne
                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() != chiffre && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }

                // S'il n'y a qu'une place, c'est un Unique Caché
                if (placesPossibles.size() == 1) {
                    Case caseCible = placesPossibles.get(0);
                    int valeurJoueur = caseCible.getValeur();
                    if (valeurJoueur == chiffre) continue;

                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
                    if (!contientErreur && nbCasesVides <= 1) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, chiffre);

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        return new Indice("Unique Caché", "Erreur détectée dans ce bloc.\nLe chiffre " + chiffre + " doit obligatoirement y figurer et cette case est sa seule place valide.", casesASurbriller, solutions, true);
                    } else if (indiceNormal == null) {
                        casesASurbriller.addAll(bloc.getListeCases());
                        String msg = "Techniques uniques cachées : Un certain chiffre indispensable à ce bloc ne peut être placé que dans une seule case. Par processus d'élimination avec les lignes/colonnes, trouvez où il va !";
                        indiceNormal = new Indice("Unique Caché", msg, casesASurbriller, solutions, false);
                    }
                }
            }
        }
        return indiceNormal;
    }

    private boolean blocContientChiffreValide(GroupementCases bloc, int chiffre) {
        for (Case c : bloc.getListeCases()) if (c.getValeur() == chiffre) return true;
        return false;
    }

    private boolean blocRequiertChiffre(GroupementCases bloc, int chiffre) {
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) return false;
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (!combinaison.contains(chiffre)) return false; 
        }
        return true; 
    }
}