package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurAlignementBloc;
import fr.univ.calcudoku.service.aide.visitor.VisiteurScannerAxe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Technique d'aide : Chiffre Incontournable.
 * Détecte un chiffre qui doit obligatoirement apparaître dans toutes les
 * combinaisons valides d'un bloc aligné sur un axe.
 */
public class TechniqueChiffreIncontournable extends TechniqueIntraBloc {

    @Override
    public Indice analyser(Grille grille) {
        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            List<List<Integer>> combosValides = getCombinaisonsValides(grille, bloc);
            
            if (combosValides.size() > 1) {
                // 1. Trouver les chiffres incontournables
                Set<Integer> chiffresCommuns = new HashSet<>(combosValides.get(0));
                for (int i = 1; i < combosValides.size(); i++) chiffresCommuns.retainAll(combosValides.get(i));
                for (Case c : bloc.getListeCases()) if (c.getValeur() != 0) chiffresCommuns.remove(c.getValeur());

                if (chiffresCommuns.isEmpty()) continue;

                // 2. Vérifier l'alignement
                VisiteurAlignementBloc visiteurAlignement = new VisiteurAlignementBloc();
                visiteurAlignement.visiter(bloc);

                if (!visiteurAlignement.isAligneLigne() && !visiteurAlignement.isAligneColonne()) continue;

                boolean estLigne = visiteurAlignement.isAligneLigne();
                int indexAxe = estLigne ? visiteurAlignement.getYCommun() : visiteurAlignement.getXCommun();
                String nomAxe = estLigne ? "ligne" : "colonne";

                // 3. Chercher si on peut nettoyer cet axe
                for (int chiffreObligatoire : chiffresCommuns) {
                    VisiteurScannerAxe scanner = new VisiteurScannerAxe(indexAxe, estLigne, bloc.getListeCases(), Collections.singletonList(chiffreObligatoire));
                    scanner.visiter(grille);
                    
                    List<Case> casesANettoyer = scanner.getCasesANettoyer();

                    if (!casesANettoyer.isEmpty()) {
                        boolean joueurLeSait = false;
                        List<Case> casesVidesBloc = new ArrayList<>();
                        for (Case c : bloc.getListeCases()) {
                            if (c.getValeur() == 0) {
                                casesVidesBloc.add(c);
                                if (c.getNotes().contains(chiffreObligatoire)) joueurLeSait = true;
                            }
                        }

                        List<String> messages = new ArrayList<>();
                        if (!joueurLeSait) {
                            messages.add("Certains blocs ont plusieurs combinaisons possibles, mais cachent une certitude : un chiffre précis est présent dans toutes les options !");
                            messages.add("Peu importe la combinaison finale de ce bloc, le chiffre " + chiffreObligatoire + " est incontournable. Ajoutez-le dans les annotations des cases en surbrillance !");
                            return new Indice("Le Chiffre Incontournable", messages, casesVidesBloc, new HashMap<>(), false);
                        } else {
                            messages.add("Vous avez repéré qu'un chiffre était obligatoire dans ce bloc parfaitement aligné. Servez-vous de cette excellente déduction !");
                            messages.add("Puisque le chiffre " + chiffreObligatoire + " est obligatoirement confiné dans le bloc, il ne peut pas être ailleurs sur la même " + nomAxe + ". Effacez-le des cases en surbrillance !");
                            
                            List<Case> toutesCibles = new ArrayList<>(bloc.getListeCases());
                            toutesCibles.addAll(casesANettoyer);
                            return new Indice("Nettoyage par Chiffre Incontournable", messages, toutesCibles, new HashMap<>(), false);
                        }
                    }
                }
            }
        }
        return null;
    }
}