package fr.univ.calcudoku.save;

import fr.univ.calcudoku.model.Grille;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;

public class Sauvegarde {
    
	public enum ModeDeJeu { LIBR, AVEN }
	public enum Difficulte { FACIL, MOYEN, DIFFI }

	public Historique hist;
	public Temps tmp;

	private boolean terminee;
	private String idGrille;
	private ModeDeJeu mode;
	private Difficulte diff;
	private int bonus, malus;

	public Sauvegarde() {
		this.idGrille = "";
		this.tmp = new Temps();
		this.hist = new Historique();
	}

	public boolean getTerminee() { return this.terminee; }
	public void setTerminee(boolean newTerminee) { this.terminee = newTerminee; }

	public String getIdGrille() { return this.idGrille; }
	
	public int getNumeroGrille() {
		if (this.idGrille != null && this.idGrille.contains("_")) {
			String[] parts = this.idGrille.split("_");
			try {
				return Integer.parseInt(parts[parts.length - 1]);
			} catch (NumberFormatException e) {}
		}
		return 0; // Par défaut
	}

	public void setIdGrille(String newIdGrille) {
		this.idGrille = newIdGrille;
        if (newIdGrille != null && newIdGrille.contains("_")) {
            String[] parts = newIdGrille.split("_");
            if (parts[0].equalsIgnoreCase("libre")) {
                this.mode = ModeDeJeu.LIBR;
                if (parts.length >= 3) {
                    try {
                        int d = Integer.parseInt(parts[2]);
                        if (d == 1) this.diff = Difficulte.FACIL;
                        else if (d == 2) this.diff = Difficulte.MOYEN;
                        else if (d == 3) this.diff = Difficulte.DIFFI;
                    } catch (NumberFormatException e) { }
                }
            } else if (parts[0].equalsIgnoreCase("aventure")) {
                this.mode = ModeDeJeu.AVEN;
                this.diff = Difficulte.MOYEN; 
            }
        }
	}

	public ModeDeJeu getMode() { return this.mode; }
	public void setMode(ModeDeJeu newMode) { this.mode = newMode; }

	public Difficulte getDiff() { return this.diff; }
	public void setDiff(Difficulte newDiff) { this.diff = newDiff; }

	public void enreg(String compte, Grille grille) {
		if(this.idGrille == null || this.idGrille.isEmpty() || this.mode == null || this.diff == null) return;
		
        // Chemins relatifs sécurisés
		String cheminSave = "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN) cheminSave += "/aventure/";
		else cheminSave += "/";
        
        new File(cheminSave).mkdirs();
		cheminSave += this.idGrille;

		boolean iniFonctionnel = true;
		try (FileWriter ini = new FileWriter(cheminSave + ".ini")) {
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
		} catch(IOException e) {
			System.out.println(e);
			iniFonctionnel = false;
		}

		if(iniFonctionnel) {
			try (FileWriter json = new FileWriter(cheminSave + ".json")) {
				int[][] matrice = new int[grille.getTaille()][grille.getTaille()];
				for(int i = 0; i < grille.getTaille(); i++) {
					for(int j = 0; j < grille.getTaille(); j++) {
						matrice[i][j] = grille.getCase(i, j).getValeur();
					}
				}
				new Gson().toJson(matrice, json);
			} catch(Exception e) { System.out.println(e); }
		}
	}

	// LECTURE DU FICHIER INI ROBUSTE LIGNE PAR LIGNE
	public void charger(String compte, Grille grille) {
		String cheminSave = "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN) cheminSave += "/aventure/";
		else cheminSave += "/";
		cheminSave += this.idGrille;

		boolean iniFonctionnel = true;
		try {
			File fichierIni = new File(cheminSave + ".ini");
			if(fichierIni.isFile()) {
				BufferedReader br = new BufferedReader(new FileReader(fichierIni));
				String ligne;
				while ((ligne = br.readLine()) != null) {
					ligne = ligne.trim();
					if (ligne.isEmpty() || ligne.startsWith("[")) continue;
					
					String[] parts = ligne.split("=", 2);
					if (parts.length < 2) continue;
					
					String cle = parts[0].trim();
					String valeur = parts[1].trim();
					
					switch (cle) {
						case "terminee": this.terminee = Boolean.parseBoolean(valeur); break;
						case "grille": this.idGrille = valeur; break;
						case "mode": this.mode = ModeDeJeu.valueOf(valeur); break;
						case "difficulte": this.diff = Difficulte.valueOf(valeur); break;
						case "temps": this.tmp.setTempsPrecedent(Double.parseDouble(valeur)); break;
						case "historique": 
							if (!valeur.isEmpty() && !valeur.equals("|")) {
								// Exemple de valeur: [1,2,3]-[4,5,6]|
								String[] etapesStr = valeur.replace("|", "").split("-");
								for (String etapeStr : etapesStr) {
									if (etapeStr.isEmpty()) continue;
									// on retire les crochets
									String clean = etapeStr.replace("[", "").replace("]", "");
									String[] coords = clean.split(",");
									if (coords.length == 3) {
										Etape e = new Etape();
										e.setX(Integer.parseInt(coords[0].trim()));
										e.setY(Integer.parseInt(coords[1].trim()));
										e.setN(Integer.parseInt(coords[2].trim()));
										this.hist.addEtape(e);
									}
								}
							}
							break;
						case "index": this.hist.setIndex(Integer.parseInt(valeur)); break;
						case "bonus": this.bonus = Integer.parseInt(valeur); break;
						case "malus": this.malus = Integer.parseInt(valeur); break;
					}
				}
				br.close();
			} else {
				System.out.println("Fichier INI introuvable : " + cheminSave + ".ini");
				iniFonctionnel = false;
			}
		} catch(Exception e) {
			System.out.println("Erreur chargement INI: " + e); 
			iniFonctionnel = false;
		}

		if(iniFonctionnel) {
			try {
				File fichierJson = new File(cheminSave + ".json");
				if(fichierJson.isFile()) {
					FileReader lecteurJson = new FileReader(fichierJson);
					int[][] matrice = new Gson().fromJson(lecteurJson, int[][].class);
					for(int j = 0; j < matrice.length; j++) {
						for(int i = 0; i < matrice.length; i++) {
							grille.getCase(i, j).setValeur(matrice[i][j]);
						}
					}
					lecteurJson.close();
				}
			} catch(Exception e) { System.out.println(e); }
		}
	}

	public void effacer(String compte) {
		String cheminSave = "profils/" + compte + "/parties";
		if(this.mode == ModeDeJeu.AVEN) cheminSave += "/aventure/";
		else cheminSave += "/";
		cheminSave += this.idGrille;

		new File(cheminSave + ".ini").delete();
		new File(cheminSave + ".json").delete();
		terminee = false;
	}
}