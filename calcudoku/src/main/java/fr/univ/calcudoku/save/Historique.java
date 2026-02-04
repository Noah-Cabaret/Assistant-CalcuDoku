import java.io.*;
import java.util.*;

public class Historique
{
	private List<Etape> hist;
	private int index;

	public Historique()
	{
		this.hist = new ArrayList<Etape>();
		this.index = 0;
	}

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

	private void viderQueue()
	{
		while(this.hist.size() > this.index + 1)
			this.hist.remove(this.hist.size());
	}

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

	public void removeEtape()
	{
		this.hist.remove(this.hist.size() - 1);

		if(this.index > this.hist.size() - 1)
			this.index--;
	}

	public void annuler()
	{
		/* TODO
		 * vider graphiquement la case à l'étape this.index sans pour
		 * autant changer l'historique, sinon impossible d'utiliser
		 * refaire()
		 */
		this.index--;
	}

	public void refaire()
	{
		/* TODO
		 * même chose que annuler() mais pour le contraire
		 */
		this.index++;
	}

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
