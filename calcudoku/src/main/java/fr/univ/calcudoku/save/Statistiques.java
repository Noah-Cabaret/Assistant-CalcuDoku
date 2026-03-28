package fr.univ.calcudoku.save;

import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

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
		try (FileWriter ini = new FileWriter("profils/" + compte + "/statistiques.ini")) {
			ini.write("[Statistiques]\n");
			ini.write("parties_jouees=" + this.partiesJouees + "\n");
			ini.write("victoires=" + this.victoires + "\n");
			ini.write("ratio_victoires=" + this.ratioVictoires + "\n");
			ini.write("temps_moyen=" + this.moyenne + "\n");
			ini.write("progression_aventure=" + this.progressionAventure + "\n");
			ini.write("difficulte_max=" + this.diffMax + "\n");
			ini.write("score=" + this.score + "\n");
		} catch(Exception e) { e.printStackTrace(); }
	}

	@Override
	public void charger(String compte) {
        File f = new File("profils/" + compte + "/statistiques.ini");
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
                    case "parties_jouees": this.partiesJouees = Integer.parseInt(valeur); break;
                    case "victoires": this.victoires = Integer.parseInt(valeur); break;
                    case "ratio_victoires": this.ratioVictoires = Double.parseDouble(valeur); break;
                    case "temps_moyen": this.moyenne = Double.parseDouble(valeur); break;
                    case "progression_aventure": this.progressionAventure = Integer.parseInt(valeur); break;
                    case "difficulte_max": 
                        if (!valeur.equals("null")) this.diffMax = Sauvegarde.Difficulte.valueOf(valeur); 
                        break;
                    case "score": this.score = Long.parseLong(valeur); break;
                }
            }
		} catch(Exception e) { System.out.println("Erreur chargement Statistiques: " + e.getMessage()); }
	}
}