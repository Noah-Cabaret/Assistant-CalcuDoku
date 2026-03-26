package fr.univ.calcudoku.save;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class Sauvegarde
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
	public enum Defi
	{
		AUCUN,
		SURVI,
		CHRON,
		NOAID,
	};

	public Historique hist;
	public Temps tmp;

	private boolean terminee;
	private int idGrille;
	private ModeDeJeu mode;
	private Difficulte diff;
	private Defi defi;
	private int bonus, malus;

	private static String cheminResources = Sauvegarde.class.getResource("/fxml").getPath().replace("/target/classes/fxml", "/");

	public Sauvegarde()
	{
		this.idGrille = 0;
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

	public Sauvegarde.ModeDeJeu getMode()
	{
		return this.mode;
	}

	public Sauvegarde.Difficulte getDiff()
	{
		return this.diff;
	}

	public Sauvegarde.Defi getDefi()
	{
		return this.defi;
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

	public void setDefi(Defi newDefi)
	{
		this.defi = newDefi;
	}

	/* Sauvegarde en format INI pour plus de facilité à scanner
	 * le fichier dans le chargement
	 */

	public void enreg(String compte, Grille grille)
	{
		if(this.idGrille == 0 || this.tmp.tempsTotal() == 0.0 || this.hist.taille() == 0 || this.mode == null || this.diff == null || (this.mode == ModeDeJeu.AVEN && this.defi == null))
		{
			System.out.println("ERREUR: attribut(s) non initialisé(s) :");
			if(this.idGrille == 0)
				System.out.println("idGrille");
			if(this.tmp.tempsTotal() == 0.0)
				System.out.println("temps");
			if(this.hist.taille() == 0)
				System.out.println("historique");
			if(this.mode == null)
				System.out.println("mode");
			if(this.diff == null)
				System.out.println("difficulté");
			if(this.mode == ModeDeJeu.AVEN && this.defi == null)
				System.out.println("défi");
			return;
		}
		String cheminSave = cheminResources + "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN)
			cheminSave += "/aventure/";
		else
			cheminSave += "/";
		cheminSave += this.idGrille;
		String cheminIni = cheminSave + ".ini";

		boolean iniFonctionnel = true;
		try
		{
			FileWriter ini = new FileWriter(cheminIni);

			ini.write("[Informations]\n");
			ini.write("terminee=" + this.terminee + "\n");
			ini.write("grille=" + this.idGrille + "\n");
			ini.write("mode=" + this.mode + "\n");
			ini.write("difficulte=" + this.diff + "\n");
			ini.write("defi=" + (this.mode == ModeDeJeu.AVEN ? this.defi : Defi.AUCUN) + "\n");
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
			iniFonctionnel = false;
		}

		if(iniFonctionnel)
		{
			try
			{
				String cheminJson = cheminSave + ".json";
				FileWriter json = new FileWriter(cheminJson);
				int[][] matrice = new int[grille.getTaille()][grille.getTaille()];
				for(int i = 0; i < grille.getTaille(); i++)
				{
					for(int j = 0; j < grille.getTaille(); j++)
						matrice[i][j] = grille.getCase(i, j).getValeur();
				}
				Gson gson = new Gson();
				gson.toJson(matrice, json);

				json.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

	/* Copie des données du fichier dans l'objet Sauvegarde
	 * (utilisation de Scanner inspirée du fscanf du C pour
	 * une lecture du code plus facile)
	 */

	public void charger(String compte, Grille grille)
	{
		String cheminSave = cheminResources + "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN)
			cheminSave += "/aventure/";
		else
			cheminSave += "/";
		cheminSave += this.idGrille;
		String cheminIni = cheminSave + ".ini";

		boolean iniFonctionnel = true;
		try
		{
			File fichierIni = new File(cheminIni);
			if(fichierIni.isFile())
			{
				FileReader ini = new FileReader(cheminIni);
				Scanner sc = new Scanner(ini);
				sc.useLocale(Locale.US);
				sc.useDelimiter("[=\n]");

				sc.next();
				sc.next(); this.terminee = Boolean.valueOf(sc.next());
				sc.next(); this.idGrille = sc.nextInt();
				sc.next(); this.mode = ModeDeJeu.valueOf(sc.next());
				sc.next(); this.diff = Difficulte.valueOf(sc.next());
				sc.next(); this.defi = Defi.valueOf(sc.next());
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
				ini.close();
			}
			else
				System.out.println("Wrong path");
		}
		catch(IOException e)
		{
			System.out.println(e);
			iniFonctionnel = false;
		}

		if(iniFonctionnel)
		{
			try
			{
				Gson gson = new Gson();
				String cheminJson = cheminSave + ".json";
				File fichierJson = new File(cheminJson);
				if(fichierJson.isFile())
				{
					FileReader lecteurJson = new FileReader(cheminJson);
					int[][] matrice = gson.fromJson(lecteurJson, int[][].class);
					for(int j = 0; j < matrice.length; j++)
					{
						for(int i = 0; i < matrice.length; i++)
							grille.getCase(i, j).setValeur(matrice[i][j]);
					}
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

	/* Suppression de la partie en cours (si commencée ou terminée) */

	public void effacer(String compte)
	{
		String cheminSave = cheminResources + "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN)
			cheminSave += "/aventure/";
		else
			cheminSave += "/";
		cheminSave += this.idGrille;
		String cheminIni = cheminSave + ".ini";
		String cheminJson = cheminSave + ".json";

		File ini = new File(cheminIni);
		if(ini.isFile())
			ini.delete();

		File json = new File(cheminJson);
		if(json.isFile())
			json.delete();

		terminee = false;
	}
}
