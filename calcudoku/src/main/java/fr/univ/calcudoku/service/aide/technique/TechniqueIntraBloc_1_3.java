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

public class TechniqueIntraBloc_1_3 extends TechniqueIntraBloc {

    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            if (bloc.getListeCases().size() == 3 && verifierTopologiePetitL(bloc)) {
                
                List<List<Integer>> combosPossibles = bloc.getCombinaisonsMaths();
                List<List<Integer>> combosValides = getCombinaisonsValides(grille, bloc);
                
                // Si la grille a réduit les possibilités à UNE SEULE et qu'elle a un doublon
                if (combosValides.size() == 1) {
                    List<Integer> combinaisonUnique = combosValides.get(0);
                    
                    if (aDesChiffresIdentiques(combinaisonUnique)) {
                        
                        int nbCasesVides = 0;
                        boolean contientErreur = false;
                        List<Case> casesFausses = new ArrayList<>();

                        for (Case c : bloc.getListeCases()) {
                            if (c.getValeur() == 0) nbCasesVides++;
                            else if (c.getValeur() != c.getSolution()) { contientErreur = true; casesFausses.add(c); }
                        }

                        // On laisse la technique "Dernière case" s'en occuper s'il reste 0 ou 1 case vide sans erreur
                        if (!contientErreur && nbCasesVides <= 1) continue;

                        Map<Case, Integer> solutions = new HashMap<>();

                        if (contientErreur) {
                            String msg = "Erreur détectée ! Au vu de la grille, ce bloc force le placement d'un doublon mathématique.\nLes chiffres en surbrillance créent un conflit ou sont mal placés.";
                            indicesErreurs.add(new Indice("Technique Intra-bloc (Petit L)", msg, casesFausses, solutions, true));
                        } else {
                            String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                            String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                            int cible = bloc.getResultatCible();
                            String msg;

                            // Message adapté selon la situation (unique depuis le début OU par élimination)
                            if (combosPossibles.size() == 1) {
                                msg = "Technique intra-bloc : Observez ce bloc en forme de 'L'. Pour atteindre " + cible + " en utilisant (" + symbole + "), la seule combinaison possible est (" + comboStr + ") !\nPour ne pas violer les règles du jeu, le chiffre en double doit obligatoirement être placé aux deux extrémités du 'L'.";
                            } else {
                                msg = "Déduction intra-bloc : Au départ, ce bloc en 'L' avait plusieurs combinaisons.\nMais grâce aux chiffres déjà placés dans la grille, il n'en reste plus qu'une de valable : (" + comboStr + ") !\nElle contient un doublon qui doit obligatoirement être placé aux extrémités du 'L'.";
                            }
                            
                            indicesNormaux.add(new Indice("Technique Intra-bloc (Petit L)", msg, bloc.getListeCases(), solutions, false));
                        }
                    }
                }
            }
        }

        // L'aléatoire est maintenant parfait grâce au stockage dans des listes
        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));

        return null; 
    }

    private boolean verifierTopologiePetitL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        Case c1 = cases.get(0), c2 = cases.get(1), c3 = cases.get(2);
        boolean estLigneVerticale = (c1.getX() == c2.getX() && c2.getX() == c3.getX());
        boolean estLigneHorizontale = (c1.getY() == c2.getY() && c2.getY() == c3.getY());
        return !estLigneVerticale && !estLigneHorizontale;
    }
}