package fr.univ.calcudoku.save;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Case;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Historique {
    private List<Etape> hist;
    private int index;

    public Historique() {
        this.hist = new ArrayList<>();
        this.index = 0;
        this.addEtape(null);
    }

    public int getIndex() { return this.index; }
    public void setIndex(int newIndex) { this.index = newIndex; }
    public List<Etape> getHist() { return Collections.unmodifiableList(this.hist); }

    public Etape getEtapeCourante() {
        return new Etape(this.hist.get(this.index));
    }

    public int taille() { return this.hist.size(); }

    public void removeEtape() {
        this.hist.remove(this.taille() - 1);
        if (this.index > this.taille() - 1) this.index--;
    }

    public void viderQueue() {
        while (this.taille() > this.index + 1) this.removeEtape();
    }

    public void addEtape(Etape e) {
        if (e == null) {
            this.viderQueue();
            this.hist.add(new Etape(0, 0, 0));
            this.index = this.taille() - 1;
            return;
        }
        Etape courante = this.getEtapeCourante();
        if (courante.getX() != e.getX() || courante.getY() != e.getY() || courante.getN() != e.getN() || e.annotation() || e.hypotheseAnnotation()) {
            this.viderQueue();
            this.hist.add(new Etape(e));
            this.index = this.taille() - 1;
        }
    }

    public void addEtape(int x, int y, int n) {
        Etape e = new Etape(x, y, n);
        addEtape(e);
    }

    public Etape precedent() {
        this.index--;
        return getEtapeCourante();
    }

    public Etape suivant() {
        if (this.index < this.taille() - 1) this.index++;
        return getEtapeCourante();
    }

    public void appliquerUndo(Grille grilleModele, boolean modeHypotheseActif) {
        if (this.getIndex() > 0) {
            this.index--;
            reconstruireGrilleComplete(grilleModele);
        }
    }

    public void appliquerRedo(Grille grilleModele) {
        if (this.getIndex() < this.taille() - 1) {
            this.index++;
            reconstruireGrilleComplete(grilleModele);
        }
    }

    public void validerHypotheses() {
        int curr = this.index;
        while (curr > 0) {
            Etape e = this.hist.get(curr);
            if (e.hypotheseNormale() || e.hypotheseAnnotation()) {
                e.setN(e.getN() - Etape.OFFSET_HYPOTHESE);
            } else {
                break;
            }
            curr--;
        }
        this.viderQueue(); 
    }

    public void rollbackHypotheses(Grille grilleModele, boolean modeHypotheseActif) {
        boolean changed = false;
        while (this.getIndex() > 0) {
            Etape e = this.hist.get(this.index);
            if (e.hypotheseNormale() || e.hypotheseAnnotation()) {
                this.index--;
                changed = true;
            } else {
                break;
            }
        }
        if (changed) {
            reconstruireGrilleComplete(grilleModele);
        }
        this.viderQueue();
    }

    private void reconstruireGrilleComplete(Grille grilleModele) {
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                c.setValeur(0);
                c.effacerNotes();
                c.setEstHypothese(false);
            }
        }

        for (int k = 1; k <= this.index; k++) {
            Etape e = this.hist.get(k);
            Case c = grilleModele.getCase(e.getX(), e.getY());
            
            if (e.getN() == 0 || e.getN() == Etape.OFFSET_HYPOTHESE) {
                c.setValeur(0);
                c.effacerNotes();
                c.setEstHypothese(false);
            } else if (e.normale()) {
                c.effacerNotes();
                c.setValeur(e.getN());
                c.setEstHypothese(false);
                supprimerNotesLigneColonne(grilleModele, e.getX(), e.getY(), e.getN());
            } else if (e.annotation()) {
                c.basculerNote(e.getN() - Etape.OFFSET_ANNOTATION);
            } else if (e.hypotheseNormale()) {
                c.effacerNotes();
                c.setValeur(e.getN() - Etape.OFFSET_HYPOTHESE);
                c.setEstHypothese(true);
                supprimerNotesLigneColonne(grilleModele, e.getX(), e.getY(), e.getN() - Etape.OFFSET_HYPOTHESE);
            } else if (e.hypotheseAnnotation()) {
                c.basculerNote(e.getN() - Etape.OFFSET_HYPOTHESE_ANNOTATION);
            }
        }
    }

    private void supprimerNotesLigneColonne(Grille grille, int targetX, int targetY, int valeur) {
        int taille = grille.getTaille();
        for (int i = 0; i < taille; i++) {
            if (i != targetY) grille.getCase(targetX, i).supprimerUneNote(valeur);
            if (i != targetX) grille.getCase(i, targetY).supprimerUneNote(valeur);
        }
    }

    @Override
    public String toString() {
        StringBuilder etapes = new StringBuilder();
        for (int i = 1; i < this.taille(); i++) {
            etapes.append("[").append(this.hist.get(i).toString()).append("]");
            if (i == this.taille() - 1) etapes.append("|");
            else etapes.append("-");
        }
        return etapes.toString();
    }
}