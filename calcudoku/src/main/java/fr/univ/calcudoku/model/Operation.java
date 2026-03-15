package fr.univ.calcudoku.model;

import java.util.Collections;
import java.util.List;

/**
 * Énumération des opérations mathématiques disponibles dans Calcudoku.
 * Chaque opération définit comment calculer le résultat à partir des valeurs.
 */
public enum Operation {
    /** Addition des valeurs */
    ADDITION("+") {
        @Override
        public int calculer(List<Integer> valeurs) {
            return valeurs.stream().mapToInt(Integer::intValue).sum();
        }
    },
    
    /** Soustraction (valeur max - valeur min) */
    SOUSTRACTION("-") {
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            // CORRECTION : On cherche juste le min et le max sans modifier la liste !
            int max = Collections.max(valeurs);
            int min = Collections.min(valeurs);
            return max - min;
        }
    },
    
    /** Multiplication des valeurs */
    MULTIPLICATION("×") {
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            int res = 1;
            for (int v : valeurs) res *= v;
            return res;
        }
    },
    
    /** Division (valeur max / valeur min) */
    DIVISION("÷") { 
        @Override
        public int calculer(List<Integer> valeurs) {
            if (valeurs.isEmpty()) return 0;
            // CORRECTION : On cherche juste le min et le max sans modifier la liste !
            int max = Collections.max(valeurs);
            int min = Collections.min(valeurs);
            
            if (min == 0) return 0; 
            if (max % min != 0) return -1;
            
            return max / min;
        }
    },
    
    /** Aucune opération, retourne simplement la seule valeur */
    RIEN("") { 
        @Override
        public int calculer(List<Integer> valeurs) {
            return valeurs.isEmpty() ? 0 : valeurs.get(0);
        }
    };

    /** Le symbole visuel de l'opération */
    private final String symbole;

    /**
     * Constructeur d'une opération.
     * @param symbole le symbole visuel (+, -, *, /)
     */
    Operation(String symbole) {
        this.symbole = symbole;
    }

    /**
     * Retourne le symbole de l'opération.
     * @return le symbole visuel
     */
    public String getSymbole() {
        return symbole;
    }

    /**
     * Calcule le résultat de l'opération sur une liste de valeurs.
     * @param valeurs la liste des valeurs de la cage
     * @return le résultat de l'opération
     */
    public abstract int calculer(List<Integer> valeurs);
}