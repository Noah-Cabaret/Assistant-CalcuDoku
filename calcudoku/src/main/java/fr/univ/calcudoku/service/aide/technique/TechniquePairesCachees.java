package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TechniquePairesCachees implements TechniqueAide {
    @Override
    public Indice analyser(Grille grille) {
        int t = grille.getTaille();
        for (int i = 0; i < t; i++) {
            Indice res = analyserZone(grille, i, true);
            if (res == null) res = analyserZone(grille, i, false);
            if (res != null) return res;
        }
        return null;
    }

    private Indice analyserZone(Grille g, int idx, boolean estLigne) {
        int t = g.getTaille();
        // On compte combien de fois chaque chiffre apparaît dans les notes de la zone
        for (int n1 = 1; n1 <= t; n1++) {
            for (int n2 = n1 + 1; n2 <= t; n2++) {
                List<Case> casesAvecN1 = new ArrayList<>();
                List<Case> casesAvecN2 = new ArrayList<>();

                for (int i = 0; i < t; i++) {
                    Case c = g.getCase(estLigne ? i : idx, estLigne ? idx : i);
                    if (c.getValeur() == 0) {
                        if (c.getNotes().contains(n1)) casesAvecN1.add(c);
                        if (c.getNotes().contains(n2)) casesAvecN2.add(c);
                    }
                }

                if (casesAvecN1.size() == 2 && casesAvecN1.equals(casesAvecN2)) {
                    Case c1 = casesAvecN1.get(0);
                    Case c2 = casesAvecN1.get(1);

                    if (c1.getNotes().size() > 2 || c2.getNotes().size() > 2) {
                        List<String> msg = new ArrayList<>();
                        msg.add("Paire Cachée : les chiffres " + n1 + " et " + n2 + " ne peuvent aller que dans ces deux cases.");
                        msg.add("Toutes les autres notes de ces deux cases sont donc impossibles. Nettoyez-les !");
                        return new Indice("Paire Cachée", msg, casesAvecN1, new HashMap<>(), false);
                    }
                }
            }
        }
        return null;
    }
}