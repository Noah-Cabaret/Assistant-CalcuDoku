package fr.univ.calcudoku.save;

import java.io.FileWriter;
import fr.univ.calcudoku.utils.Constantes;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Statistiques d'un profil joueur (parties jouées, victoires, progression, score).
 * Sauvegardées dans le fichier statistiques.ini du profil.
 */
public class Statistiques extends Donnees {
	private int partiesJouees = 0;
	private int victoires = 0;
	private Double ratioVictoires = 0.0;
	private Double moyenne = 0.0;
	private int progressionAventure = 1;
	private Sauvegarde.Difficulte diffMax = Sauvegarde.Difficulte.FACIL;
	private long score = 0;

	public Statistiques() {}

	public int getPartiesJouees() { return this.partiesJouees; }
	public void setPartiesJouees(int newPartiesJouees) { this.partiesJouees = newPartiesJouees; }
	public int getVictoires() { return this.victoires; }
	public void setVictoires(int newVictoires) { this.victoires = newVictoires; }
	public Double getRatioVictoires() { return this.ratioVictoires; }
	public void setRatioVictoires() { 
		if (this.partiesJouees > 0) this.ratioVictoires = (double) this.victoires / this.partiesJouees; 
		else this.ratioVictoires = 0.0;
	}
	public Double getMoyenne() { return this.moyenne; }
	public void setMoyenne(Double newMoyenne) { this.moyenne = newMoyenne; }
	public int getProgressionAventure() { return this.progressionAventure; }
	public void setProgressionAventure(int newProgressionAventure) { this.progressionAventure = newProgressionAventure; }
	public Sauvegarde.Difficulte getDiffMax() { return this.diffMax; }
	public void setDiffMax(Sauvegarde.Difficulte newDiffMax) { this.diffMax = newDiffMax; }
	public long getScore() { return this.score; }
	public void setScore(long newScore) { this.score = newScore; }
	@Override
	public void enreg(String compte) {
		try (FileWriter ini = new FileWriter(Constantes.DOSSIER_PROFILS + compte + Constantes.FICHIER_STATISTIQUES)) {
			ini.write("[Statistiques]\n");
			ini.write(Constantes.STAT_PARTIES_JOUEES + "=" + this.partiesJouees + "\n");
			ini.write(Constantes.STAT_VICTOIRES + "=" + this.victoires + "\n");
			ini.write(Constantes.STAT_RATIO + "=" + this.ratioVictoires + "\n");
			ini.write(Constantes.STAT_TEMPS_MOYEN + "=" + this.moyenne + "\n");
			ini.write(Constantes.STAT_PROGRESSION + "=" + this.progressionAventure + "\n");
			ini.write(Constantes.STAT_DIFF_MAX + "=" + this.diffMax + "\n");
			ini.write(Constantes.STAT_SCORE + "=" + this.score + "\n");
		} catch(Exception e) { e.printStackTrace(); }
	}
	@Override
	public void charger(String compte) {
		File f = new File(Constantes.DOSSIER_PROFILS + compte + Constantes.FICHIER_STATISTIQUES);
		if (!f.exists()) return;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String ligne;
			while ((ligne = br.readLine()) != null) {
				ligne = ligne.trim();
				if (ligne.isEmpty() || ligne.startsWith("[")) continue;
				String[] parts = ligne.split("=", 2);
				if (parts.length < 2) continue;
				String cle = parts[0].trim();
				String valeur = parts[1].trim();
				switch(cle) {
					case Constantes.STAT_PARTIES_JOUEES: this.partiesJouees = Integer.parseInt(valeur); break;
					case Constantes.STAT_VICTOIRES: this.victoires = Integer.parseInt(valeur); break;
					case Constantes.STAT_RATIO: this.ratioVictoires = Double.parseDouble(valeur); break;
                    case Constantes.STAT_TEMPS_MOYEN: this.moyenne = Double.parseDouble(valeur); break;
                    case Constantes.STAT_PROGRESSION: this.progressionAventure = Integer.parseInt(valeur); break;
                    case Constantes.STAT_DIFF_MAX: 
                        if (!valeur.equals("null")) this.diffMax = Sauvegarde.Difficulte.valueOf(valeur); 
                        break;
                    case Constantes.STAT_SCORE: this.score = Long.parseLong(valeur); break;
                }
            }
		} catch(Exception e) { System.out.println("Erreur chargement Statistiques: " + e.getMessage()); }
	}
}