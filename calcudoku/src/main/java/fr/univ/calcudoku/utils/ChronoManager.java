package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.save.Sauvegarde;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Gère le chronomètre affiché pendant une partie.
 * Supporte le mode chrono (décompte) et le mode normal (compteur).
 */
public class ChronoManager {

    private Timeline timeline;
    private int secondesEcoulees;
    private final Sauvegarde save;
    private final Label labelChrono;
    private final Runnable actionDefaiteTemps;

    /**
     * Crée un gestionnaire de chrono.
     * @param labelChrono le label d'affichage du temps
     * @param save la sauvegarde courante (pour récupérer le temps précédent)
     * @param actionDefaiteTemps action à exécuter si le temps est écoulé (mode chrono)
     */
    public ChronoManager(Label labelChrono, Sauvegarde save, Runnable actionDefaiteTemps) {
        this.labelChrono = labelChrono;
        this.save = save;
        this.actionDefaiteTemps = actionDefaiteTemps;
    }

    /** Démarre le chronomètre. */
    public void demarrer() {
        if (save.getTemps() != null && save.getTemps().getTempsPrecedent() != null) {
            secondesEcoulees = save.getTemps().getTempsPrecedent().intValue();
        } else {
            secondesEcoulees = 0;
        }

        actualiserAffichage();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondesEcoulees++;
            
            if (save.getDefi() == Defi.TypeDefi.CHRON) {
                int restant = Math.max(0, save.getTemps().getTempsMax().intValue() - secondesEcoulees);
                if (restant <= 0) {
                    arreter();
                    if (actionDefaiteTemps != null) actionDefaiteTemps.run();
                }
            }
            actualiserAffichage();
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        if (save.getTemps() != null) save.getTemps().lancer();
    }

    /** Arrête le chronomètre. */
    public void arreter() {
        if (timeline != null) timeline.stop();
        if (save.getTemps() != null) save.getTemps().arreter();
    }

    public void actualiserAffichage() {
        if (save.getDefi() == Defi.TypeDefi.CHRON) {
            int restant = Math.max(0, save.getTemps().getTempsMax().intValue() - secondesEcoulees);
            labelChrono.setText(String.format("%02d:%02d", restant / 60, restant % 60));
        } else {
            labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
        }
    }

    public int getSecondesEcoulees() { return secondesEcoulees; }
}