/* Je mets pour l'instant mais si on n'ajoute aucun paramètre
 * on pourra juste retirer ce fichier
 */

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Options extends Donnees
{
	public enum AideAuCalcul
	{
		CALCULATRICE,
		COMBINAISONS,
	};

	private AideAuCalcul aide;

	public Options()
	{
		this.aide = CALCULATRICE;
	}

	@Override
	public void enreg(String compte)
	{
		try
		{
			FileWriter ini = new FileWriter("profils/" + compte + "/options.ini");

			ini.write("[Paramètres]\n");
			ini.write("aide_au_calcul=" + this.aide + "\n");

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
			Scanner sc = new Scanner(new File("profils/" + compte + "/options.ini"));
			sc.useLocale(Locale.US);
			sc.useDelimiter("[=\n]");

			sc.next();
			sc.next(); this.aide = AideAuCalcul.valueOf(sc.next());

			sc.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}
