import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Statistiques extends Donnees
{
	/* Attributs :
	 * partiesJouees : nombre de parties commencées ou terminées
	 * victoires : nombre de parties terminées
	 * ratioVictoires : victoires / partiesJouees
	 * moyenne : temps moyen passé sur les grilles terminées
	 * progressionAventure : nombre unique désignant le niveau du
	 * mode aventure sur lequel s'est arrêté le joueur
	 *
	 * diffMax : difficulté de la grille terminée la plus dure
	 * score : score total de toutes les grilles terminées
	 */
	private int partiesJouees;
	private int victoires;
	private Double ratioVictoires;
	public Double moyenne;
	private int progressionAventure;
	private Partie.Difficulte diffMax;
	private long score;

	public Statistiques() {}

	/* Méthodes get() et set() */

	public int getPartiesJouees()
	{
		return this.partiesJouees;
	}

	public void setPartiesJouees(int newPartiesJouees)
	{
		this.partiesJouees = newPartiesJouees;
	}

	public int getVictoires()
	{
		return this.victoires;
	}

	public void setVictoires(int newVictoires)
	{
		this.victoires = newVictoires;
	}

	public Double getRatioVictoires()
	{
		return this.ratioVictoires;
	}

	public void setRatioVictoires()
	{
		this.ratioVictoires = this.partiesJouees / this.victoires;
	}

	public Double getMoyenne()
	{
		return this.moyenne;
	}

	public void setMoyenne(Double newMoyenne)
	{
		this.moyenne = newMoyenne;
	}

	public int getProgressionAventure()
	{
		return this.progressionAventure;
	}

	public void setProgressionAventure(int newProgressionAventure)
	{
		this.progressionAventure = newProgressionAventure;
	}

	public Partie.Difficulte getDiffMax()
	{
		return this.diffMax;
	}

	public void setDiffMax(Partie.Difficulte newDiffMax)
	{
		this.diffMax = newDiffMax;
	}

	public long getScore()
	{
		return this.score;
	}

	public void setScore(long newScore)
	{
		this.score = newScore;
	}

	/* Sauvegarde en format INI pour plus de facilité à scanner
	 * le fichier dans le chargement
	 */

	@Override
	public void enreg(String compte)
	{
		try
		{
			FileWriter ini = new FileWriter("profils/" + compte + "/statistiques.ini");

			ini.write("[Statistiques]\n");
			ini.write("parties_jouees=" + this.partiesJouees + "\n");
			ini.write("victoires=" + this.victoires + "\n");
			ini.write("ratio_victoires=" + this.ratioVictoires);
			ini.write("temps_moyen=" + this.moyenne + "\n");
			ini.write("progression_aventure=" + this.progressionAventure + "\n");
			ini.write("difficulte_max=" + this.diffMax + "\n");
			ini.write("score=" + this.score + "\n");

			ini.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	/* Copie des données du fichier dans l'objet Statistiques
	 * (utilisation de Scanner inspirée du fscanf du C pour
	 * une lecture du code plus facile)
	 */

	@Override
	public void charger(String compte)
	{
		try
		{
			Scanner sc = new Scanner(new File("profils/" + compte + "/statistiques.ini"));
			sc.useLocale(Locale.US);
			sc.useDelimiter("[=\n]");

			sc.next();
			sc.next(); this.partiesJouees = sc.nextInt();
			sc.next(); this.victoires = sc.nextInt();
			sc.next(); this.ratioVictoires = sc.nextDouble();
			sc.next(); this.moyenne = sc.nextDouble();
			sc.next(); this.progressionAventure = Integer.parseInt(sc.next());
			sc.next(); this.diffMax = Difficulte.valueOf(sc.next());
			sc.next(); this.score = Long.valueOf(sc.next());

			sc.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}
