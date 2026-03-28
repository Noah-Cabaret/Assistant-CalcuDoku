package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClavierHandler {
    
    /**
     * Crée un écouteur d'événements clavier propre et indépendant du contrôleur.
     */
    public static EventHandler<KeyEvent> creerFiltre(Grille grille, Supplier<Case> getCaseSelectionnee, BiConsumer<Integer, Integer> onDeplacement, Consumer<Integer> onChiffreClique, Runnable onEffacer) {
        return event -> {
            Case caseSel = getCaseSelectionnee.get();
            
            // 1. Gestion des déplacements et suppressions
            if (caseSel != null) {
                int x = caseSel.getX(); 
                int y = caseSel.getY();
                int taille = grille.getTaille();
                
                switch (event.getCode()) {
                    case UP -> y = Math.max(0, y - 1);
                    case DOWN -> y = Math.min(taille - 1, y + 1);
                    case LEFT -> x = Math.max(0, x - 1);
                    case RIGHT -> x = Math.min(taille - 1, x + 1);
                    case BACK_SPACE, DELETE -> { onEffacer.run(); event.consume(); return; }
                    default -> {}
                }
                
                // Si la case a changé, on applique l'action
                if (x != caseSel.getX() || y != caseSel.getY()) {
                    onDeplacement.accept(x, y);
                    event.consume(); 
                    return; 
                }
            }
            
            // 2. Gestion universelle des chiffres saisis
            int val = -1;
            if (event.getCode().isDigitKey()) {
                String s = event.getCode().getName().replaceAll("\\D", "");
                if (!s.isEmpty()) val = Integer.parseInt(s);
            } else if (event.getCode().toString().startsWith("NUMPAD")) {
                String s = event.getCode().toString().replaceAll("\\D", "");
                if (!s.isEmpty()) val = Integer.parseInt(s);
            }
            
            if (val > 0 && val <= grille.getTaille()) { 
                onChiffreClique.accept(val); 
                event.consume(); 
            }
        };
    }
}