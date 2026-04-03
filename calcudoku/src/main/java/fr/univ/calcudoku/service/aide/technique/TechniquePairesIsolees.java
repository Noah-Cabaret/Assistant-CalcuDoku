package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurScannerAxe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TechniquePairesIsolees implements TechniqueAide {
    @Override
    public Indice analyser(Grille grille) {
        int t = grille.getTaille();
        for (int i = 0; i < t; i++) {
            Indice res = analyserZone(grille, i, true); // Lignes
            if (res == null) res = analyserZone(grille, i, false); // Colonnes
            if (res != null) return res;
        }
        return null;
    }

    private Indice analyserZone(Grille g, int idx, boolean estLigne) {
        List<Case> zone = new ArrayList<>();
        for (int i = 0; i < g.getTaille(); i++) 
            zone.add(g.getCase(estLigne ? i : idx, estLigne ? idx : i));

        for (int i = 0; i < zone.size(); i++) {
            Case c1 = zone.get(i);
            if (c1.getValeur() != 0 || c1.getNotes().size() != 2) continue;

            for (int j = i + 1; j < zone.size(); j++) {
                Case c2 = zone.get(j);
                // On vérifie si c2 a exactement les mêmes notes que c1
                if (c2.getValeur() == 0 && c2.getNotes().equals(c1.getNotes())) {
                    Set<Integer> paire = c1.getNotes();
                    
                    // On cherche s'il y a des trucs à effacer AILLEURS
                    VisiteurScannerAxe scanner = new VisiteurScannerAxe(idx, estLigne, List.of(c1, c2), paire);
                    scanner.visiter(g);

                    if (!scanner.getCasesANettoyer().isEmpty()) {
                        List<String> msg = new ArrayList<>();
                        msg.add("Paire Isolée détectée : les chiffres " + paire + " sont bloqués dans ces deux cases.");
                        msg.add("Vous pouvez les effacer des autres cases en surbrillance sur cette " + (estLigne ? "ligne" : "colonne") + ".");
                        
                        List<Case> cibles = new ArrayList<>(List.of(c1, c2));
                        cibles.addAll(scanner.getCasesANettoyer());
                        return new Indice("Paire Isolée", msg, cibles, new HashMap<>(), false);
                    }
                }
            }
        }
        return null;
    }
}