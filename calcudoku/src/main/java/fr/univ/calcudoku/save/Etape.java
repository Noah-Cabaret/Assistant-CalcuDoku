package fr.univ.calcudoku.save;

public class Etape {
    private int x;
    private int y;
    private int n;

    public Etape() {
    }

    public Etape(int x, int y, int n) {
        this.x = x;
        this.y = y;
        this.n = n;
    }

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

    public boolean normale() {
        return this.n >= 0 && this.n <= 9;
    }

    public boolean annotation() {
        return this.n >= 10 && this.n <= 19;
    }

    public boolean hypotheseNormale() {
        return this.n >= 20 && this.n <= 29;
    }

    public boolean hypotheseAnnotation() {
        return this.n >= 30 && this.n <= 39;
    }

    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.n;
    }
}