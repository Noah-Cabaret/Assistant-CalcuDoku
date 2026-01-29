package fr.univ.calcudoku.model;

import java.util.Collections;
import java.util.List;

public enum Operation {
    ADDITION("+") {
        @Override
        public int calculer(List<Integer> valeurs) {
            return valeurs.stream().mapToInt(Integer::intValue).sum();
        }
    },
    SOUSTRACTION("-") {
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            Collections.sort(valeurs);
            return valeurs.get(valeurs.size() - 1) - valeurs.get(0);
        }
    },
    MULTIPLICATION("×") {
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            int res = 1;
            for (int v : valeurs) res *= v;
            return res;
        }
    },
    DIVISION("÷") { 
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            Collections.sort(valeurs);
            int min = valeurs.get(0);
            int max = valeurs.get(valeurs.size() - 1);
            
            if (min == 0) return 0; 
            
            if (max % min != 0) return -1;
            
            return max / min;
        }
    },
    RIEN("") { 
        @Override
        public int calculer(List<Integer> valeurs) {
            return valeurs.isEmpty() ? 0 : valeurs.get(0);
        }
    };

    private final String symbole;

    Operation(String symbole) {
        this.symbole = symbole;
    }

    public String getSymbole() {
        return symbole;
    }

    public abstract int calculer(List<Integer> valeurs);
}