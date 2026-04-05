package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurAlignementBloc;
import fr.univ.calcudoku.service.aide.visitor.VisiteurManqueAnnotations;
import fr.univ.calcudoku.service.aide.visitor.VisiteurScannerAxe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Technique d'aide : Verrouillage de Bloc.
 * Détecte quand un chiffre d'un bloc est contraint à une seule ligne ou colonne,
 * permettant de l'éliminer des autres cases de cet axe.
 */
public class TechniqueVerrouillageBloc implements TechniqueAide {
    
    @Override
    public Indice analyser(Grille grille) {
        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            List<List<Integer>> combos = bloc.getCombinaisonsMaths();
            if (combos != null && combos.size() == 1) {
                List<Integer> comboUnique = combos.get(0);

                // 1. Le joueur a-t-il bien annoté le bloc ?
                VisiteurManqueAnnotations visiteurNotes = new VisiteurManqueAnnotations(comboUnique);
                visiteurNotes.visiter(bloc);
                if (visiteurNotes.isManqueAnnotations()) continue; 

                // 2. Le bloc est-il aligné ?
                VisiteurAlignementBloc visiteurAlignement = new VisiteurAlignementBloc();
                visiteurAlignement.visiter(bloc);

                if (!visiteurAlignement.isAligneLigne() && !visiteurAlignement.isAligneColonne()) continue;

                // 3. Scanner l'axe pour nettoyer
                boolean estLigne = visiteurAlignement.isAligneLigne();
                int indexAxe = estLigne ? visiteurAlignement.getYCommun() : visiteurAlignement.getXCommun();
                String nomAxe = estLigne ? "ligne" : "colonne";

                VisiteurScannerAxe scanner = new VisiteurScannerAxe(indexAxe, estLigne, bloc.getListeCases(), comboUnique);
                scanner.visiter(grille);

                List<Case> casesANettoyer = scanner.getCasesANettoyer();

                if (!casesANettoyer.isEmpty()) {
                    List<String> messages = new ArrayList<>();
                    messages.add("Regardez vos blocs parfaitement annotés. Un groupement aligné monopolise certains chiffres, vous permettant de nettoyer vos autres annotations sur la même " + nomAxe + " !");
                    messages.add("Les cases en surbrillance contiennent des annotations impossibles. Ces chiffres sont déjà verrouillés par un autre bloc de la " + nomAxe + ". Effacez-les !");

                    List<Case> toutesCibles = new ArrayList<>(bloc.getListeCases());
                    toutesCibles.addAll(casesANettoyer);

                    return new Indice("Verrouillage d'Axe", messages, toutesCibles, new HashMap<>(), false);
                }
            }
        }
        return null;
    }
}