package fr.univ.calcudoku.save;

/**
 * Classe abstraite pour les données persistantes d'un profil (options, statistiques).
 * Définit les opérations de sauvegarde et de chargement depuis les fichiers INI.
 */
public abstract class Donnees {

    /**
     * Enregistre les données dans le fichier du profil.
     * @param compte le nom du profil utilisateur
     */
    public abstract void enreg(String compte);

    /**
     * Charge les données depuis le fichier du profil.
     * @param compte le nom du profil utilisateur
     */
    public abstract void charger(String compte);

}
