package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.service.aide.visitor.VisiteurChercheurBlocN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TechniqueIntraBloc_1_3 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        VisiteurChercheurBlocN chercheur = new VisiteurChercheurBlocN(3);
        grille.accepter(chercheur);

        for (GroupementCases bloc : chercheur.getBlocsTrouves()) {
            boolean aUnDoublon = verifierDoublonMathematique(bloc);
            boolean formeValide = verifierTopologieL(bloc);

            if (aUnDoublon && formeValide) {
                // NOTE : Le calcul de la solution est déjà effectué explicitement !
                Map<Case, Integer> reponses = calculerSolutions(bloc);

                boolean estDejaCompletEtCorrect = true;
                boolean contientErreur = false;

                for (Case c : bloc.getListeCases()) {
                    int valeurJoueur = c.getValeur();
                    
                    if (valeurJoueur != reponses.get(c)) {
                        estDejaCompletEtCorrect = false;
                        if (valeurJoueur != 0) {
                            contientErreur = true; // Détection de l'erreur
                        }
                    }
                }

                if (!estDejaCompletEtCorrect) {
                    String nom = "Technique Intra-Bloc (Doublon en L)";
                    String message;

                    // MODIFICATION : Message dynamique pour différencier une erreur d'un simple indice
                    if (contientErreur) {
                        message = "Erreur détectée ! Ce bloc de 3 cases en 'L' n'a qu'une seule combinaison possible comportant un doublon.\n" +
                                  "Certaines de vos valeurs ne correspondent pas à cette disposition (les chiffres doublons vont toujours aux extrémités).";
                    } else {
                        message = "Ce bloc de 3 cases en 'L' n'a qu'une seule combinaison possible comportant un doublon. " +
                                  "Pour éviter d'avoir le même chiffre sur une ligne/colonne, le doublon se place obligatoirement aux extrémités.";
                    }
                    
                    // L'objet renvoie bien 'reponses' pour un affichage petit à petit
                    return new Indice(nom, message, bloc.getListeCases(), reponses, contientErreur);
                }
            }
        }
        
        return null; 
    }

    private boolean verifierDoublonMathematique(GroupementCases bloc) {
        List<List<Integer>> combinaisons = bloc.getCombinaisonsMaths();
        if (combinaisons == null || combinaisons.size() != 1) return false;

        List<Integer> combinaisonUnique = combinaisons.get(0);
        Set<Integer> valeursUniques = new HashSet<>();
        for (Integer valeur : combinaisonUnique) {
            if (!valeursUniques.add(valeur)) {
                return true; 
            }
        }
        return false;
    }

    private boolean verifierTopologieL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        if (cases.size() != 3) return false;

        Case c1 = cases.get(0);
        Case c2 = cases.get(1);
        Case c3 = cases.get(2);

        boolean estLigneVerticale = (c1.getX() == c2.getX() && c2.getX() == c3.getX());
        boolean estLigneHorizontale = (c1.getY() == c2.getY() && c2.getY() == c3.getY());

        return !estLigneVerticale && !estLigneHorizontale;
    }

    private Map<Case, Integer> calculerSolutions(GroupementCases bloc) {
        Map<Case, Integer> solutions = new HashMap<>();
        List<Integer> combinaison = bloc.getCombinaisonsMaths().get(0);
        List<Case> cases = bloc.getListeCases();

        int chiffreDoublon = 0;
        int chiffreUnique = 0;
        
        if (combinaison.get(0).equals(combinaison.get(1))) {
            chiffreDoublon = combinaison.get(0);
            chiffreUnique = combinaison.get(2);
        } else if (combinaison.get(0).equals(combinaison.get(2))) {
            chiffreDoublon = combinaison.get(0);
            chiffreUnique = combinaison.get(1);
        } else {
            chiffreDoublon = combinaison.get(1);
            chiffreUnique = combinaison.get(0);
        }

        Case caseCoin = trouverCoin(cases);

        for (Case c : cases) {
            if (c.equals(caseCoin)) {
                solutions.put(c, chiffreUnique);
            } else {
                solutions.put(c, chiffreDoublon);
            }
        }

        return solutions;
    }

    private Case trouverCoin(List<Case> cases) {
        Case c1 = cases.get(0);
        Case c2 = cases.get(1);
        Case c3 = cases.get(2);

        if ((c1.getX() == c2.getX() || c1.getX() == c3.getX()) && 
            (c1.getY() == c2.getY() || c1.getY() == c3.getY())) {
            return c1;
        }
        if ((c2.getX() == c1.getX() || c2.getX() == c3.getX()) && 
            (c2.getY() == c1.getY() || c2.getY() == c3.getY())) {
            return c2;
        }
        return c3;
    }
}