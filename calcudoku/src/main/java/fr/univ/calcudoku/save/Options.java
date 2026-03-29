package fr.univ.calcudoku.save;

import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class Options extends Donnees {

    public enum AideAuCalcul { CALCULATRICE, COMBINAISONS }
    private AideAuCalcul aide;
    private boolean themeSombre;

    public Options() {
        this.aide = AideAuCalcul.CALCULATRICE;
        this.themeSombre = false;
    }

    public AideAuCalcul getAide() {
        return aide;
    }

    public void setAide(AideAuCalcul aide) {
        this.aide = aide;
    }

    public boolean isThemeSombre() {
        return themeSombre;
    }

    public void setThemeSombre(boolean themeSombre) {
        this.themeSombre = themeSombre;
    }

    @Override
    public void enreg(String compte) {
        try (FileWriter ini = new FileWriter("profils/" + compte + "/options.ini")) {
            ini.write("[Paramètres]\n");
            ini.write("aide_au_calcul=" + this.aide + "\n");
            ini.write("theme_sombre=" + this.themeSombre + "\n");
        } catch(Exception e) { e.printStackTrace(); }
    }

    @Override
    public void charger(String compte) {
        File f = new File("profils/" + compte + "/options.ini");
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
                    case "aide_au_calcul": 
                        if (!valeur.equals("null")) this.aide = AideAuCalcul.valueOf(valeur); 
                        break;
                    case "theme_sombre": 
                        this.themeSombre = Boolean.parseBoolean(valeur); 
                        break;
                }
            }
        } catch(Exception e) { System.out.println("Erreur chargement Options: " + e.getMessage()); }
    }

}