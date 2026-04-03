package fr.univ.calcudoku.save;

public class Record {
    public long score;
    public int temps;
    public String joueur;

    public Record(long score, int temps, String joueur) {
        this.score = score;
        this.temps = temps;
        this.joueur = joueur;
    }
}
