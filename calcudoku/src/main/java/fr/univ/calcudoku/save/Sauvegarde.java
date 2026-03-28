package fr.univ.calcudoku.save;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Grille;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;
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
    private Defi.TypeDefi defi;
    private int bonus, malus;
    private int vies;

    public Sauvegarde() {
        this.idGrille = "";
        this.tmp = new Temps();
        this.hist = new Historique();
        this.defi = Defi.TypeDefi.AUCUN;
        this.diff = Difficulte.FACIL; // NOUVEAU : Empêche l'écriture de 'null' !
    }

    public boolean getTerminee() { return this.terminee; }
    public void setTerminee(boolean newTerminee) { this.terminee = newTerminee; }

    public String getIdGrille() { return this.idGrille; }
    public void setIdGrille(String newIdGrille) { this.idGrille = newIdGrille; }

    public ModeDeJeu getMode() { return this.mode; }
    public void setMode(ModeDeJeu newMode) { this.mode = newMode; }

    public Difficulte getDiff() { return this.diff; }
    public void setDiff(Difficulte newDiff) { this.diff = newDiff; }

    public Defi.TypeDefi getDefi() { return this.defi; }
    public void setDefi(Defi.TypeDefi newDefi) { this.defi = newDefi; }

    public int getBonus() { return this.bonus; }
    public void setBonus(int newBonus) { this.bonus = newBonus; }

    public int getMalus() { return this.malus; }
    public void setMalus(int newMalus) { this.malus = newMalus; }

    public int getVies() { return this.vies; }
    public void setVies(int newVies) { this.vies = newVies; }

    public void enreg(String compte, Grille grille) {
        String cheminSave = "profils/" + compte + "/parties/";
        if(this.mode == ModeDeJeu.AVEN) cheminSave += "aventure/";
        
        File dossier = new File(cheminSave);
        if (!dossier.exists()) dossier.mkdirs();
        
        String cheminIni = cheminSave + this.idGrille + ".ini";

        boolean iniFonctionnel = true;
        try (FileWriter ini = new FileWriter(cheminIni)) {
            ini.write("[Informations]\n");
            ini.write("terminee=" + this.terminee + "\n");
            ini.write("grille=" + this.idGrille + "\n");
            ini.write("mode=" + this.mode + "\n");
            ini.write("difficulte=" + this.diff + "\n");
            ini.write("defi=" + this.defi + "\n");
            ini.write("temps=" + this.tmp.toString() + "\n");
            ini.write("historique=" + this.hist.toString() + "\n");
            ini.write("index=" + this.hist.getIndex() + "\n");
            ini.write("bonus=" + this.bonus + "\n");
            ini.write("malus=" + this.malus + "\n");
            ini.write("vies=" + this.vies + "\n");
        } catch(IOException e) {
            e.printStackTrace();
            iniFonctionnel = false;
        }

        if(iniFonctionnel) {
            try (FileWriter json = new FileWriter(cheminSave + this.idGrille + ".json")) {
                int[][] matrice = new int[grille.getTaille()][grille.getTaille()];
                for(int i = 0; i < grille.getTaille(); i++) {
                    for(int j = 0; j < grille.getTaille(); j++) {
                        matrice[i][j] = grille.getCase(i, j).getValeur();
                    }
                }
                new Gson().toJson(matrice, json);
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    public void charger(String compte, Grille grille) {
        String cheminSave = "profils/" + compte + "/parties/";
        if(this.mode == ModeDeJeu.AVEN) cheminSave += "aventure/";
        String cheminIni = cheminSave + this.idGrille + ".ini";

        boolean iniFonctionnel = true;
        try {
            File fichierIni = new File(cheminIni);
            if(fichierIni.isFile()) {
                Scanner sc = new Scanner(fichierIni);
                sc.useLocale(Locale.US);
                sc.useDelimiter("[=\\n]");

                sc.next(); // [Informations]
                sc.next(); this.terminee = Boolean.parseBoolean(sc.next().trim());
                sc.next(); this.idGrille = sc.next().trim();
                
                sc.next(); 
                try { this.mode = ModeDeJeu.valueOf(sc.next().trim()); } catch(Exception e) { this.mode = ModeDeJeu.LIBR; }
                
                sc.next(); 
                try { this.diff = Difficulte.valueOf(sc.next().trim()); } catch(Exception e) { this.diff = Difficulte.FACIL; }
                
                sc.next(); 
                try { this.defi = Defi.TypeDefi.valueOf(sc.next().trim()); } catch(Exception e) { this.defi = Defi.TypeDefi.AUCUN; }
                
                sc.next(); this.tmp.setTempsPrecedent(Double.parseDouble(sc.next().trim()));

                sc.useDelimiter("[,\\n\\[\\]]");
                Etape e = new Etape();
                while(sc.hasNext()) {
                    String token = sc.next().trim();
                    if(token.isEmpty()) continue;
                    if(token.charAt(0) == '|') break; // Fin de l'historique
                    try {
                        e.setX(Integer.parseInt(token));
                        e.setY(Integer.parseInt(sc.next().trim()));
                        e.setN(Integer.parseInt(sc.next().trim()));
                        this.hist.addEtape(e);
                    } catch(Exception ex) {}
                }
                sc.useDelimiter("[=\\n]");

                sc.next(); this.hist.setIndex(Integer.parseInt(sc.next().trim()));
                sc.next(); this.bonus = Integer.parseInt(sc.next().trim());
                sc.next(); this.malus = Integer.parseInt(sc.next().trim());
                sc.next(); this.vies = Integer.parseInt(sc.next().trim());
                sc.close();
            }
        } catch(Exception e) {
            System.err.println("Fichier de sauvegarde introuvable ou illisible : " + e.getMessage());
            iniFonctionnel = false;
        }

        if(iniFonctionnel) {
            try {
                File fichierJson = new File(cheminSave + this.idGrille + ".json");
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
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    public void effacer(String compte) {
        String cheminSave = "profils/" + compte + "/parties/";
        if(this.mode == ModeDeJeu.AVEN) cheminSave += "aventure/";
        
        File ini = new File(cheminSave + this.idGrille + ".ini");
        if(ini.isFile()) ini.delete();

        File json = new File(cheminSave + this.idGrille + ".json");
        if(json.isFile()) json.delete();

        terminee = false;
    }
}