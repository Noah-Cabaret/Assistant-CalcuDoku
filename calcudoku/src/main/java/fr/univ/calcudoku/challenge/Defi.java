package fr.univ.calcudoku.challenge;

/**
 * Définit les types de défis spéciaux applicables à une partie.
 */
public class Defi {
    /** Types de défis disponibles */
    public enum TypeDefi {
        /** Aucun défi */
        AUCUN,
        /** Mode survie : nombre de vies limité */
        SURVI,
        /** Mode chrono : temps limité */
        CHRON,
        /** Mode sans aide */
        NOAID
    }
}