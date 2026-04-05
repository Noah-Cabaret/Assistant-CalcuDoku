package fr.univ.calcudoku.save;

/**
 * Représente un record sur une grille (meilleur score + temps).
 * Sérialisé en JSON via Gson.
 */
public class Record {
    /** Score obtenu */
    public long score;
    /** Temps de résolution en secondes */
    public int temps;
    /** Nom du joueur */
    public String joueur;

    /**
     * Crée un nouveau record.
     * @param score le score obtenu
     * @param temps le temps en secondes
     * @param joueur le nom du joueur
     */
    public Record(long score, int temps, String joueur) {
        this.score = score;
        this.temps = temps;
        this.joueur = joueur;
    }
}
