import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Statistiques extends Donnees
{
	private int partiesJouees;
	private int victoires;
	public Double moyenne;
	private int progressionAventure;
/*
	private Partie.Difficulte diffMax;
	private long score;
*/
	public Statistiques() {}

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

	public void setMoyenne(Double newMoyenne)
	{
		this.moyenne = newMoyenne;
	}

	@Override
	public void enreg(String compte)
	{
		try
		{
			FileWriter ini = new FileWriter("profils/" + compte + "/statistiques.ini");

			ini.write("[Statistiques]\n");
			ini.write("parties_jouees=" + this.partiesJouees + "\n");
			ini.write("victoires=" + this.victoires + "\n");
			ini.write("temps_moyen=" + this.moyenne + "\n");
			ini.write("progression_aventure=" + this.progressionAventure + "\n");
/*
			ini.write("difficulte_max=" + this.diffMax + "\n");
			ini.write("score=" + this.score + "\n");
*/
			ini.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

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
			sc.next(); this.moyenne = sc.nextDouble();
			sc.next(); this.progressionAventure = Integer.parseInt(sc.next());
/*
			sc.next(); this.diffMax = Difficulte.valueOf(sc.next());
			sc.next(); this.score = Long.valueOf(sc.next());
*/
			sc.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}
