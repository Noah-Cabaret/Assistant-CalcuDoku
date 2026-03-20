package fr.univ.calcudoku.save;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Options extends Donnees
{
	/* Attributs :
	 * aide : sélection de l'aide en jeu correspondante :
	 * - calculatrice interne à côté de la grille
	 * - combinaisons possibles pour un bloc de la grille
	 *
	 * themeSombre : change le blanc en noir et inversement
	 */

	public enum AideAuCalcul
	{
		CALCULATRICE,
		COMBINAISONS,
	};

	private AideAuCalcul aide;

	private boolean themeSombre;

	public Options()
	{
		this.aide = AideAuCalcul.CALCULATRICE;
		this.themeSombre = false;
	}

	/* Sauvegarde en format INI pour plus de facilité à scanner
	 * le fichier dans le chargement
	 */

	@Override
	public void enreg(String compte)
	{
		try
		{
			FileWriter ini = new FileWriter("profils/" + compte + "/options.ini");

			ini.write("[Paramètres]\n");
			ini.write("aide_au_calcul=" + this.aide + "\n");
			ini.write("theme_sombre=" + this.themeSombre + "\n");

			ini.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	/* Copie des données du fichier dans l'objet Options
	 * (utilisation de Scanner inspirée du fscanf du C pour
	 * une lecture du code plus facile)
	 */

	@Override
	public void charger(String compte)
	{
		try
		{
			Scanner sc = new Scanner(new File("profils/" + compte + "/options.ini"));
			sc.useLocale(Locale.US);
			sc.useDelimiter("[=\n]");

			sc.next();
			sc.next(); this.aide = AideAuCalcul.valueOf(sc.next());
			sc.next(); this.themeSombre = Boolean.valueOf(sc.next());

			sc.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}
