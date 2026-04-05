package fr.univ.calcudoku.save;

import java.time.Instant;

/**
 * Gère le chronométrage d'une partie.
 * Permet de lancer, arrêter et calculer le temps total écoulé.
 */
public class Temps {
    private Double debut;
    private Double tempsPrecedent;
    private Double tempsMax;

    /** Crée un chronomètre à zéro. */
    public Temps() {
        this.tempsPrecedent = 0.0;
        this.tempsMax = 0.0;
        this.debut = 0.0;
    }

    /**
     * Retourne l'instant courant en secondes (précision nanoseconde).
     * @return le timestamp actuel
     */
    public static Double maintenant() {
        Instant inst = Instant.now();
        return inst.getEpochSecond() + inst.getNano() / 1_000_000_000.0;
    }

    public Double getTempsPrecedent() { return this.tempsPrecedent; }
    public void setTempsPrecedent(Double newTempsPrecedent) { this.tempsPrecedent = newTempsPrecedent; }
    public Double getTempsMax() { return this.tempsMax; }
    public void setTempsMax(Double newTempsMax) { this.tempsMax = newTempsMax; }
    
    /**
     * Calcule le temps total écoulé depuis le début (ou le temps sauvegardé si arrêté).
     * @return le temps total en secondes
     */
    public Double tempsTotal() {
        return this.debut == 0.0 ? this.tempsPrecedent : this.tempsPrecedent + Temps.maintenant() - this.debut;
    }
    
    /** Lance le chronomètre. */
    public void lancer() { 
        this.debut = Temps.maintenant(); 
    }
    
   
    /** Arrête le chronomètre et cumule le temps écoulé. */
    public void arreter() {
        if (this.debut != 0.0) {
            this.tempsPrecedent += (Temps.maintenant() - this.debut);
            this.debut = 0.0; 
        }
    }

    /** @return les heures écoulées */
    public int heures() { return (tempsTotal().intValue() / 3600); }
    /** @return les minutes écoulées (modulo 60) */
    public int minutes() { return ((tempsTotal().intValue() / 60) % 60); }
    /** @return les secondes écoulées (modulo 60) */
    public int secondes() { return (tempsTotal().intValue() % 60); }
    public String toString() { return tempsTotal().toString(); }
}