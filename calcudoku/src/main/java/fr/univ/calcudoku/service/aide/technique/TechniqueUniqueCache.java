package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueUniqueCache implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            for(Case c : bloc.getListeCases()) {
                if (c.getValeur() == 0) nbCasesVides++;
            }

            int taille = grille.getTaille();

            // LOGIQUE MATHÉMATIQUE : On teste la capacité d'accueil de la cage
            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                if (blocContientChiffreValide(bloc, chiffre)) continue; 
                
                // LE FAMEUX CORRECTIF : Le chiffre doit être obligatoire pour ce bloc
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() != chiffre && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }

                if (placesPossibles.size() == 1) {
                    Case caseCible = placesPossibles.get(0);
                    int valeurJoueur = caseCible.getValeur();
                    if (valeurJoueur == chiffre) continue;

                    // VÉRIFICATION ERREUR 
                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());

                    // Filtre Anti-doublon (s'efface devant une erreur)
                    if (!contientErreur && nbCasesVides <= 1) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();
                    solutions.put(caseCible, chiffre);

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        return new Indice("Unique Caché (Bloc)", "Erreur détectée dans cette cage mathématique.\nCette case est la seule place valide pour le chiffre " + chiffre + ".", casesASurbriller, solutions, true);
                    } else if (indiceNormal == null) {
                        casesASurbriller.addAll(bloc.getListeCases());
                        indiceNormal = new Indice("Unique Caché (Bloc)", "Regardez cette cage mathématique.\nLe chiffre " + chiffre + " doit obligatoirement y figurer.\nToutes les autres cases sont bloquées !", casesASurbriller, solutions, false);
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