package fr.univ.calcudoku.save;

public class Etape
{
	/* Attributs :
	 * x : position x sur la grille
	 * y : position y sur la grille
	 * n : valeur de la case à la position (x,y), 0 étant une case vide
	 */

	private int x, y, n;

	public Etape() {}

	public Etape(int x, int y, int n)
	{
		this.x = x;
		this.y = y;
		this.n = n;
	}

	/* Constructeur surchargé pour pouvoir dupliquer une étape à partir d'une autre étape */

	public Etape(Etape e)
	{
		if(e != null)
		{
			this.x = e.getX();
			this.y = e.getY();
			this.n = e.getN();
		}
	}

	/* Méthodes get() et set() */

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public int getN()
	{
		return this.n;
	}

	public void setX(int newX)
	{
		this.x = newX;
	}

	public void setY(int newY)
	{
		this.y = newY;
	}

	public void setN(int newN)
	{
		this.n = newN;
	}

	public void setEtape(int newX, int newY, int newN)
	{
		this.x = newX;
		this.y = newY;
		this.n = newN;
	}

	public void setEtape(Etape newEtape)
	{
		this.x = newEtape.x;
		this.y = newEtape.y;
		this.n = newEtape.n;
	}

	/* Vérifications du type d'étape */

	/* Aucun mode activé */
	public boolean normale()
	{
		return this.n >= 0 && this.n <= 9;
	}

	/* Mode annotation activé */
	public boolean annotation()
	{
		return this.n >= 10 && this.n <= 19;
	}

	/* Mode hypothèse activé */
	public boolean hypothese()
	{
		return this.n >= 20 && this.n <= 29;
	}

	/* Conversion en String pour la sauvegarde de l'historique */

	public String toString()
	{
		return this.x + "," + this.y + "," + this.n;
	}
}
