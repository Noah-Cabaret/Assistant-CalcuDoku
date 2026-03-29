package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.save.Temps;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Gestionnaire du temps de la partie.
 * S'occupe de l'affichage du chronomètre et de la logique de compte à rebours.
 */
public class ChronoManager {

    private Timeline timeline;
    private int secondesEcoulees;
    private final Temps tempsSave;
    private final Defi.TypeDefi defi;
    private final Label labelChrono;
    
    // Callback : l'action à exécuter si le temps du défi "Contre la montre" est écoulé
    private final Runnable actionDefaiteTemps;

    public ChronoManager(Label labelChrono, Temps tempsSave, Defi.TypeDefi defi, Runnable actionDefaiteTemps) {
        this.labelChrono = labelChrono;
        this.tempsSave = tempsSave;
        this.defi = defi;
        this.actionDefaiteTemps = actionDefaiteTemps;
    }

    public void demarrer() {
        if (tempsSave != null && tempsSave.getTempsPrecedent() != null) {
            secondesEcoulees = tempsSave.getTempsPrecedent().intValue();
        } else {
            secondesEcoulees = 0;
        }

        actualiserAffichage();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondesEcoulees++;
            actualiserAffichage();
            
            if (defi == Defi.TypeDefi.CHRON) {
                int restant = Math.max(0, tempsSave.getTempsMax().intValue() - secondesEcoulees);
                if (restant <= 0) {
                    arreter();
                    if (actionDefaiteTemps != null) actionDefaiteTemps.run();
                }
            }
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        if (tempsSave != null) tempsSave.lancer();
    }

    public void arreter() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void actualiserAffichage() {
        if (defi == Defi.TypeDefi.CHRON) {
            int restant = Math.max(0, tempsSave.getTempsMax().intValue() - secondesEcoulees);
            labelChrono.setText(String.format("%02d:%02d", restant / 60, restant % 60));
        } else {
            labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
        }
    }

    public int getSecondesEcoulees() {
        return secondesEcoulees;
    }
}