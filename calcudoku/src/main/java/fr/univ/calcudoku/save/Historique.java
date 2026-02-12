import java.io.*;
import java.util.*;

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

	/* Méthode pour retirer la dernière étape de l'historique */

	public void removeEtape()
	{
		this.hist.remove(this.hist.size() - 1);

		if(this.index > this.hist.size() - 1)
			this.index--;
	}

	/* Méthode pour vider toute la partie de l'historique entre
	 * (index + 1) et la fin de hist
	 */

	private void viderQueue()
	{
		while(this.hist.size() > this.index + 1)
			this.removeEtape();
	}

	/* Méthode d'ajout d'une étape dans l'historique (surchargée pour
	 * des envois de message plus faciles)
	 */

	public void addEtape(Etape e)
	{
		this.viderQueue();
		this.hist.add(new Etape(e));
		this.index = this.hist.size() - 1;
	}

	public void addEtape(int x, int y, int n)
	{
		this.viderQueue();
		Etape e = new Etape(x,y,n);
		this.hist.add(e);
		this.index = this.hist.size() - 1;
	}

	/* Revenir à l'étape précédente de la grille */

	public void annuler()
	{
		/* TODO
		 * vider graphiquement la case à l'étape this.index sans pour
		 * autant changer l'historique, sinon impossible d'utiliser
		 * refaire()
		 */
		this.index--;
	}

	/* Revenir à l'étape suivante de la grille */

	public void refaire()
	{
		/* TODO
		 * même chose que annuler() mais pour le contraire
		 */
		this.index++;
	}

	/* Conversion en String pour la sauvegarde */

	public String toString()
	{
		String etapes = new String();
		Object[] ha = this.hist.toArray();

		for(int i = 0; i < this.hist.size(); i++)
		{
			etapes += "[" + ha[i].toString() + "]";
			if(i == this.hist.size() - 1)
				etapes += "|";
			else
				etapes += "-";
		}

		return etapes;
	}
}
