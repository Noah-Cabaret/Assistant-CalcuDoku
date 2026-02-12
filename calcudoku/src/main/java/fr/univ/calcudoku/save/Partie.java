import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Partie extends Donnees
{
	/* Attributs :
	 * hist : historique de la grille, rendu public pour pouvoir interagir
	 * avec sans réadapter toutes ses méthodes
	 *
	 * tmp : timer, rendu public pour la même raison que hist
	 * idGrille : numéro unique de la grille à charger, utile pour charger
	 * la bonne sauvegarde
	 *
	 * terminee : variable indiquant si la partie a déjà été gagnée, pour
	 * pouvoir proposer une réinitialisation de la grille
	 *
	 * mode : mode de jeu seléctionné permettant de sauvegarder en conséquence
	 * diff : difficulté de la partie, le jeu s'adaptera en conséquence
	 * bonus et malus : variables affectant le score final en fonction du
	 * temps passé et du nombre d'aides demandées
	 */

	public enum ModeDeJeu
	{
		LIBR,
		AVEN,
	};
	public enum Difficulte
	{
		FACIL,
		MOYEN,
		DIFFI,
	};

	public Historique hist;
	public Temps tmp;

	private boolean terminee;
	private int idGrille;
	private ModeDeJeu mode;
	private Difficulte diff;
	private int bonus, malus;

	public Partie()
	{
		this.tmp = new Temps();
		this.hist = new Historique();
	}

	/* Méthodes get() et set() */

	public boolean getTerminee()
	{
		return this.terminee;
	}

	public int getIdGrille()
	{
		return this.idGrille;
	}

	public Partie.ModeDeJeu getMode()
	{
		return this.mode;
	}

	public Partie.Difficulte getDiff()
	{
		return this.diff;
	}

	public void setTerminee(boolean newTerminee)
	{
		this.terminee = newTerminee;
	}

	public void setIdGrille(int newIdGrille)
	{
		this.idGrille = newIdGrille;
	}

	public void setMode(ModeDeJeu newMode)
	{
		this.mode = newMode;
	}

	public void setDiff(Difficulte newDiff)
	{
		this.diff = newDiff;
	}

	/* Sauvegarde en format INI pour plus de facilité à scanner
	 * le fichier dans le chargement
	 */

	@Override
	public void enreg(String compte)
	{
		String cheminIni = new String("profils/" + compte + "/parties/");
		if(this.mode == ModeDeJeu.LIBR)
			cheminIni += this.mode.toString() + "_" + idGrille;
		else
			cheminIni += "aventure/" + idGrille;
		cheminIni += ".ini";

		try
		{
			FileWriter ini = new FileWriter(cheminIni);

			ini.write("[Informations]\n");
			ini.write("terminee=" + this.terminee + "\n");
			ini.write("grille=" + this.idGrille + "\n");
			ini.write("mode=" + this.mode + "\n");
			ini.write("difficulte=" + this.diff + "\n");
			ini.write("temps=" + this.tmp.toString() + "\n");
			ini.write("historique=" + this.hist.toString() + "\n");
			ini.write("index=" + this.hist.getIndex() + "\n");
			ini.write("bonus=" + this.bonus + "\n");
			ini.write("malus=" + this.malus + "\n");

			ini.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	/* Copie des données du fichier dans l'objet Partie
	 * (utilisation de Scanner inspirée du fscanf du C pour
	 * une lecture du code plus facile)
	 */

	@Override
	public void charger(String compte)
	{
		try
		{
			Scanner sc = new Scanner(new File("profils/" + compte + "/parties/" + this.mode + "_" + this.idGrille + ".ini"));
			sc.useLocale(Locale.US);
			sc.useDelimiter("[=\n]");

			sc.next();
			sc.next(); this.terminee = Boolean.valueOf(sc.next());
			sc.next(); this.idGrille = sc.nextInt();
			sc.next(); this.mode = ModeDeJeu.valueOf(sc.next());
			sc.next(); this.diff = Difficulte.valueOf(sc.next());
			sc.next(); this.tmp.setTempsPrecedent(Double.parseDouble(sc.next()));

			sc.useDelimiter("[,\\[\\]\\n]");
			Etape e = new Etape();
			while(sc.next().toCharArray()[0] != '|')
			{
				e.setX(Integer.parseInt(sc.next()));
				e.setY(Integer.parseInt(sc.next()));
				e.setN(Integer.parseInt(sc.next()));
				this.hist.addEtape(e);
			}
			sc.useDelimiter("[=\n]");

			sc.next(); this.hist.setIndex(Integer.parseInt(sc.next()));
			sc.next(); this.bonus = sc.nextInt();
			sc.next(); this.malus = sc.nextInt();

			sc.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	/* Suppression de la partie en cours (si commencée ou terminée) */

	public void effacer(String compte)
	{
		File partie = new File("profils/" + compte + "/parties/" + this.mode + "_" + this.idGrille + ".ini");
		partie.delete();
		terminee = false;
	}
}
