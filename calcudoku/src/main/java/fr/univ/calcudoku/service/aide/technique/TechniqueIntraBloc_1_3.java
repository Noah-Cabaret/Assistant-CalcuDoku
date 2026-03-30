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
 * Technique d'aide : Intra-bloc pour les formes en "Petit L" (3 cases).
 * Permet de forcer le placement de doublons aux extrémités du "L".
 */
public class TechniqueIntraBloc_1_3 extends TechniqueIntraBloc {

    /**
     * Analyse les blocs de 3 cases en forme de "L".
     * @param grille La grille à analyser.
     * @return Un Indice avec des messages progressifs.
     */
    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            if (bloc.getListeCases().size() == 3 && verifierTopologiePetitL(bloc)) {
                
                List<List<Integer>> combosPossibles = bloc.getCombinaisonsMaths();
                List<List<Integer>> combosValides = getCombinaisonsValides(grille, bloc);
                
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

                        if (!contientErreur && nbCasesVides <= 1) continue;

                        Map<Case, Integer> solutions = new HashMap<>();
                        List<String> messages = new ArrayList<>();

                        if (contientErreur) {
                            messages.add("Il y a un problème de placement dans un bloc en forme de 'L'.");
                            messages.add("Ce bloc force l'utilisation d'un doublon, mais vos chiffres actuels créent un conflit.");
                            messages.add("Erreur détectée ! Au vu de la grille, le bloc en surbrillance force un doublon mathématique, mais les chiffres placés sont faux.");
                            indicesErreurs.add(new Indice("Technique Intra-bloc (Petit L)", messages, casesFausses, solutions, true));
                        } else {
                            String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                            String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                            int cible = bloc.getResultatCible();
                            
                            messages.add("Observez la forme des blocs. Certains blocs coudés (en 'L') ont des propriétés particulières.");
                            messages.add("La seule combinaison valable pour ce bloc nécessite un chiffre en double. Réfléchissez à comment placer un doublon sans violer les règles.");
                            
                            if (combosPossibles.size() == 1) {
                                messages.add("Déduction intra-bloc : Pour atteindre " + cible + " (" + symbole + "), la seule combinaison du bloc en surbrillance est (" + comboStr + "). Le chiffre en double doit obligatoirement être placé aux deux extrémités du 'L' !");
                            } else {
                                messages.add("Déduction intra-bloc : Par élimination avec la grille, la seule combinaison du bloc en surbrillance est (" + comboStr + "). Le chiffre en double doit être placé aux deux extrémités du 'L' !");
                            }
                            
                            indicesNormaux.add(new Indice("Technique Intra-bloc (Petit L)", messages, bloc.getListeCases(), solutions, false));
                        }
                    }
                }
            }
        }

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