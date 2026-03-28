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
		this.hist = new ArrayList<Etape>();
		this.index = 0;
		this.addEtape(null); 
	}

	public int getIndex() { return this.index; }
	public void setIndex(int newIndex) { this.index = newIndex; }
	public List<Etape> getHist() { return Collections.unmodifiableList(this.hist); }

	public Etape getEtapeCourante() {
		return new Etape(this.hist.get(this.index)); // Retourne une copie sécurisée
	}

	public int taille() { return this.hist.size(); }

	public void removeEtape() {
		this.hist.remove(this.taille() - 1);
		if(this.index > this.taille() - 1) this.index--;
	}

	public void viderQueue() {
		while(this.taille() > this.index + 1) this.removeEtape();
	}

	public void addEtape(Etape e) {
		if(e == null) {
			this.viderQueue();
			this.hist.add(new Etape(0, 0, 0));
			this.index = this.taille() - 1;
			return;
		}
		
		Etape courante = this.getEtapeCourante();
		
		// CORRECTION : On force l'enregistrement si c'est une annotation (normale ou hypothèse)
		// pour être sûr d'enregistrer les clics qui désactivent une annotation
		if(courante.getX() != e.getX() || courante.getY() != e.getY() || courante.getN() != e.getN() || e.annotation() || e.hypotheseAnnotation()) {
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
		if(this.index < this.taille() - 1) this.index++;
		return getEtapeCourante();
	}

	public void appliquerUndo(Grille grilleModele, boolean modeHypotheseActif) {
		if (this.getIndex() > 0) {
			Etape etapeAnnulee = this.getEtapeCourante();
			int targetX = etapeAnnulee.getX();
			int targetY = etapeAnnulee.getY();

			this.index--;

			Case caseCible = grilleModele.getCase(targetX, targetY);
			caseCible.setValeur(0);
			caseCible.effacerNotes();

			for (int k = 1; k <= this.index; k++) {
				Etape e = this.hist.get(k);
				if (e.getX() == targetX && e.getY() == targetY) {
					if (e.normale()) {
						caseCible.effacerNotes();
						caseCible.setValeur(e.getN());
					} else if (e.annotation()) {
						caseCible.basculerNote(e.getN() - 10);
					} else if (e.hypotheseNormale()) {
						caseCible.effacerNotes();
						caseCible.setValeur(e.getN() - 20);
					} else if (e.hypotheseAnnotation()) {
						caseCible.basculerNote(e.getN() - 30);
					}

					if (e.getN() == 0 || e.getN() == 20) {
						caseCible.setValeur(0);
						caseCible.effacerNotes();
					}
				}
			}
		}
	}

	public void appliquerRedo(Grille grilleModele) {
		if(this.getIndex() < this.taille() - 1) {
			Etape etapeSuivante = this.suivant();
			Case caseCible = grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY());

			// CORRECTION : On s'assure que faire REDO sur un "Effacer" supprime bien les notes aussi
			if (etapeSuivante.getN() == 0 || etapeSuivante.getN() == 20) {
				caseCible.setValeur(0);
				caseCible.effacerNotes();
			} else if(etapeSuivante.normale()) {
				caseCible.effacerNotes();
				caseCible.setValeur(etapeSuivante.getN());
			} else if(etapeSuivante.annotation()) {
				caseCible.basculerNote(etapeSuivante.getN() - 10);
			} else if(etapeSuivante.hypotheseNormale()) {
				caseCible.effacerNotes();
				caseCible.setValeur(etapeSuivante.getN() - 20);
			} else if(etapeSuivante.hypotheseAnnotation()) {
				caseCible.basculerNote(etapeSuivante.getN() - 30);
			}
		}
	}

	public void validerHypotheses() {
		// CORRECTION CRITIQUE : On modifie directement l'objet dans la liste, et pas la copie !
		int curr = this.index;
		while (curr > 0) {
			Etape e = this.hist.get(curr);
			if (e.hypotheseNormale() || e.hypotheseAnnotation()) {
				e.setN(e.getN() - 20); // Les marqueurs Hypothèse sont VRAIMENT retirés de la mémoire
			} else {
				break;
			}
			curr--;
		}
		this.viderQueue(); // On supprime tous les éventuels "Redo" qui étaient en attente
	}

	public void rollbackHypotheses(Grille grilleModele, boolean modeHypotheseActif) {
		// CORRECTION : On lit directement dans l'historique sans créer de copies fantômes
		while(this.getIndex() > 0) {
			Etape e = this.hist.get(this.index);
			if (e.hypotheseNormale() || e.hypotheseAnnotation()) {
				appliquerUndo(grilleModele, modeHypotheseActif);
			} else {
				break;
			}
		}
		this.viderQueue();
	}

	public String toString() {
		String etapes = new String();
		for(int i = 1; i < this.taille(); i++) {
			etapes += "[" + this.hist.get(i).toString() + "]";
			if(i == this.taille() - 1) etapes += "|";
			else etapes += "-";
		}
		return etapes;
	}
}