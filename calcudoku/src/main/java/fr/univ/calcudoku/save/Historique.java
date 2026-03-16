package fr.univ.calcudoku.save;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Historique
{
	/* Attributs :
	 * hist : historique des étapes du jeu
	 * index : position dans l'historique, le jeu chargera toutes les
	 * étapes entre 0 et index
	 */

	private List<Etape> hist;
	private int index;

	public Historique()
	{
		this.hist = new ArrayList<Etape>();
		this.index = 0;
		this.addEtape(null); // Ajout d'une étape vide pour l'état d'une grille vide (ne sera jamais traité)
	}

	/* Méthodes get() et set() */

	public int getIndex()
	{
		return this.index;
	}

	public void setIndex(int newIndex)
	{
		this.index = newIndex;
	}

	public List<Etape> getHist()
	{
		return Collections.unmodifiableList(this.hist);
	}

	/* Getter avec copie pour ne pas pouvoir modifier l'historique */

	public Etape getEtapeCourante()
	{
		return new Etape(this.hist.toArray(new Etape[0])[this.index]);
	}

	public int taille()
	{
		return this.hist.size();
	}

	/* Méthode pour retirer la dernière étape de l'historique */

	public void removeEtape()
	{
		this.hist.remove(this.taille() - 1);

		if(this.index > this.taille() - 1)
			this.index--;
	}

	/* Méthode pour vider toute la partie de l'historique entre
	 * (index + 1) et la fin de hist, utile uniquement pour
	 * ajouter une étape après avoir utilisé precedent()
	 */

	public void viderQueue()
	{
		while(this.taille() > this.index + 1)
			this.removeEtape();
	}

	/* Méthode d'ajout d'une étape dans l'historique (surchargée pour
	 * des envois de message plus faciles)
	 */

	public void addEtape(Etape e)
	{
		if(e == null || this.getEtapeCourante().getX() != e.getX() || this.getEtapeCourante().getY() != e.getY() || this.getEtapeCourante().getN() != e.getN() || e.annotation())
		{
			this.viderQueue();
			this.hist.add(new Etape(e));
			this.index = this.taille() - 1;
		}
	}

	public void addEtape(int x, int y, int n)
	{
		Etape etapeCourante = this.getEtapeCourante();
		if(etapeCourante.getX() != x || etapeCourante.getY() != y || etapeCourante.getN() != n || etapeCourante.annotation())
		{
			this.viderQueue();
			Etape e = new Etape(x,y,n);
			this.hist.add(e);
			this.index = this.taille() - 1;
		}
	}

	/* Revenir à l'étape précédente de la grille */

	public Etape precedent()
	{
		this.index--;
		return getEtapeCourante();
	}

	/* Revenir à l'étape suivante de la grille */

	public Etape suivant()
	{
		if(this.index < this.taille() - 1)
			this.index++;
		return getEtapeCourante();
	}

	/* Conversion en String pour la sauvegarde */

	public String toString()
	{
		String etapes = new String();
		Object[] ha = this.hist.toArray();

		for(int i = 1; i < this.taille(); i++)
		{
			etapes += "[" + ha[i].toString() + "]";
			if(i == this.taille() - 1)
				etapes += "|";
			else
				etapes += "-";
		}

		return etapes;
	}
}
