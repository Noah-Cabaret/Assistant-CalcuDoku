package fr.univ.calcudoku.save;

/**
 * Représente une étape dans l'historique des actions du joueur.
 * Chaque étape contient la position (x, y) et la valeur placée (n).
 * Les offsets permettent de distinguer le type d'action :
 * normale, annotation, hypothèse, ou annotation en mode hypothèse.
 */
public class Etape {

    /** Offset ajouté à la valeur pour indiquer une annotation */
    public static final int OFFSET_ANNOTATION = 10;
    /** Offset ajouté à la valeur pour indiquer une hypothèse */
    public static final int OFFSET_HYPOTHESE = 20;
    /** Offset ajouté pour une annotation en mode hypothèse */
    public static final int OFFSET_HYPOTHESE_ANNOTATION = 30;

    private int x;
    private int y;
    private int n;

    /** Constructeur par défaut (valeurs à 0). */
    public Etape() {
    }

    /**
     * Crée une étape avec ses coordonnées et sa valeur.
     * @param x colonne de la case
     * @param y ligne de la case
     * @param n valeur placée (avec offset selon le type)
     */
    public Etape(int x, int y, int n) {
        this.x = x;
        this.y = y;
        this.n = n;
    }

    /**
     * Constructeur de copie.
     * @param e l'étape à copier
     */
    public Etape(Etape e) {
        if (e != null) {
            this.x = e.getX();
            this.y = e.getY();
            this.n = e.getN();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getN() {
        return this.n;
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;
    }

    public void setN(int newN) {
        this.n = newN;
    }

    /**
     * Modifie les coordonnées et la valeur de l'étape.
     * @param newX nouvelle colonne
     * @param newY nouvelle ligne
     * @param newN nouvelle valeur
     */
    public void setEtape(int newX, int newY, int newN) {
        this.x = newX;
        this.y = newY;
        this.n = newN;
    }

    public void setEtape(Etape newEtape) {
        this.x = newEtape.x;
        this.y = newEtape.y;
        this.n = newEtape.n;
    }

    /** @return true si c'est un coup normal (valeur directe) */
    public boolean normale() {
        return this.n >= 0 && this.n < OFFSET_ANNOTATION;
    }

    /** @return true si c'est une annotation (note crayon) */
    public boolean annotation() {
        return this.n >= OFFSET_ANNOTATION && this.n < OFFSET_HYPOTHESE;
    }

    /** @return true si c'est un coup en mode hypothèse */
    public boolean hypotheseNormale() {
        return this.n >= OFFSET_HYPOTHESE && this.n < OFFSET_HYPOTHESE_ANNOTATION;
    }

    /** @return true si c'est une annotation en mode hypothèse */
    public boolean hypotheseAnnotation() {
        return this.n >= OFFSET_HYPOTHESE_ANNOTATION && this.n < OFFSET_HYPOTHESE_ANNOTATION + OFFSET_ANNOTATION;
    }

    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.n;
    }
}