package fr.univ.calcudoku.save;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.utils.Constantes;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Gère la sauvegarde et le chargement d'une partie en cours.
 * Stocke l'état de la grille (valeurs, notes, hypothèses) dans des fichiers INI + JSON.
 */
public class Sauvegarde {

    /** Mode de jeu : libre ou aventure */
    public enum ModeDeJeu { LIBR, AVEN }
    /** Niveau de difficulté */
    public enum Difficulte { FACIL, MOYEN, DIFFI }

    private Historique hist;
    private Temps tmp;

    private boolean terminee;
    private String idGrille;
    private ModeDeJeu mode;
    private Difficulte diff;
    private Defi.TypeDefi defi;
    private int bonus, malus;
    private int vies;
    private int aidesUtilisees;

    private static class CaseSauvegarde {
        int valeur;
        List<Integer> notes;
        boolean validee;
        public CaseSauvegarde(int valeur, List<Integer> notes, boolean validee) {
            this.valeur = valeur;
            this.notes = notes;
            this.validee = validee;
        }
    }

    /** Crée une sauvegarde vide avec les valeurs par défaut. */
    public Sauvegarde() {
        this.idGrille = "";
        this.tmp = new Temps();
        this.hist = new Historique();
        this.defi = Defi.TypeDefi.AUCUN;
        this.diff = Difficulte.FACIL;
        this.aidesUtilisees = 0;
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
    public int getAidesUtilisees() { return this.aidesUtilisees; }
    public void setAidesUtilisees(int a) { this.aidesUtilisees = a; }

    public Historique getHistorique() { return this.hist; }
    public Temps getTemps() { return this.tmp; }

    public void resetHistorique() {
        this.hist = new Historique();
    }

    /**
     * Enregistre la partie dans les fichiers INI et JSON du profil.
     * @param compte le nom du profil
     * @param grille la grille à sauvegarder
     */
    public void enreg(String compte, Grille grille) {
        String cheminSave = Constantes.DOSSIER_PROFILS + compte + Constantes.SOUS_DOSSIER_PARTIES;
        if (this.mode == ModeDeJeu.AVEN) cheminSave += Constantes.SOUS_DOSSIER_AVENTURE;
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
            ini.write("aides=" + this.aidesUtilisees + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            iniFonctionnel = false;
        }

        if (iniFonctionnel) {
            try (FileWriter json = new FileWriter(cheminSave + this.idGrille + ".json")) {
                CaseSauvegarde[][] matrice = new CaseSauvegarde[grille.getTaille()][grille.getTaille()];
                for (int i = 0; i < grille.getTaille(); i++) {
                    for (int j = 0; j < grille.getTaille(); j++) {
                        Case c = grille.getCase(i, j);
                        List<Integer> notes = new ArrayList<>(c.getNotes()); 
                        int valeurASauvegarder = c.getValeur() + (c.isEstHypothese() ? Etape.OFFSET_HYPOTHESE : 0);
                        matrice[i][j] = new CaseSauvegarde(valeurASauvegarder, notes, c.isValidee());
                    }
                }
                new Gson().toJson(matrice, json);
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }
    }

    /**
     * Charge une partie sauvegardée depuis les fichiers INI et JSON.
     * @param compte le nom du profil
     * @param grille la grille à remplir avec les valeurs chargées
     */
    public void charger(String compte, Grille grille) {
        String cheminSave = Constantes.DOSSIER_PROFILS + compte + Constantes.SOUS_DOSSIER_PARTIES;
        if (this.mode == ModeDeJeu.AVEN) cheminSave += Constantes.SOUS_DOSSIER_AVENTURE;
        String cheminIni = cheminSave + this.idGrille + ".ini";

        boolean iniFonctionnel = true;
        try {
            File fichierIni = new File(cheminIni);
            if (fichierIni.isFile()) {
                Scanner sc = new Scanner(fichierIni);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("[")) continue;
                    
                    String[] parts = line.split("=", 2);
                    if (parts.length < 2) continue;
                    
                    String key = parts[0].trim();
                    String val = parts[1].trim();
                    
                    try {
                        switch (key) {
                            case "terminee": this.terminee = Boolean.parseBoolean(val); break;
                            case "grille": this.idGrille = val; break;
                            case "mode": this.mode = ModeDeJeu.valueOf(val); break;
                            case "difficulte": this.diff = Difficulte.valueOf(val); break;
                            case "defi": this.defi = Defi.TypeDefi.valueOf(val); break;
                            case "temps": this.tmp.setTempsPrecedent(Double.parseDouble(val)); break;
                            case "index": this.hist.setIndex(Integer.parseInt(val)); break;
                            case "bonus": this.bonus = Integer.parseInt(val); break;
                            case "malus": this.malus = Integer.parseInt(val); break;
                            case "vies": this.vies = Integer.parseInt(val); break;
                            case "aides": this.aidesUtilisees = Integer.parseInt(val); break;
                            case "historique":
                                Scanner histScanner = new Scanner(val);
                                histScanner.useDelimiter("[,\\n\\[\\]\\-]");
                                while (histScanner.hasNext()) {
                                    String token = histScanner.next().trim();
                                    if (token.isEmpty()) continue;
                                    if (token.charAt(0) == '|') break;
                                    try {
                                        Etape e = new Etape();
                                        e.setX(Integer.parseInt(token));
                                        e.setY(Integer.parseInt(histScanner.next().trim()));
                                        e.setN(Integer.parseInt(histScanner.next().trim()));
                                        this.hist.addEtape(e);
                                    } catch (Exception ex) {
                                        System.err.println("Erreur parsing étape historique: " + ex.getMessage());
                                    }
                                }
                                histScanner.close();
                                break;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur parsing clé INI: " + e.getMessage());
                    }
                }
                sc.close();
            }
        } catch (Exception e) {
            iniFonctionnel = false;
        }

        if (iniFonctionnel) {
            try {
                File fichierJson = new File(cheminSave + this.idGrille + ".json");
                if (fichierJson.isFile()) {
                    FileReader lecteurJson = new FileReader(fichierJson);
                    Gson gson = new Gson();
                    try {
                        CaseSauvegarde[][] matrice = gson.fromJson(lecteurJson, CaseSauvegarde[][].class);
                        for (int i = 0; i < matrice.length; i++) {
                            for (int j = 0; j < matrice[i].length; j++) {
                                Case c = grille.getCase(i, j);
                                
                                int valLue = matrice[i][j].valeur;
                                if (valLue >= Etape.OFFSET_HYPOTHESE) {
                                    c.setValeur(valLue - Etape.OFFSET_HYPOTHESE);
                                    c.setEstHypothese(true);
                                } else {
                                    c.setValeur(valLue);
                                    c.setEstHypothese(false);
                                }
                                c.setValidee(matrice[i][j].validee);
                                c.effacerNotes();
                                if (matrice[i][j].notes != null) {
                                    for (int note : matrice[i][j].notes) {
                                        c.basculerNote(note);
                                    }
                                }
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        lecteurJson.close();
                        lecteurJson = new FileReader(fichierJson);
                        int[][] matrice = gson.fromJson(lecteurJson, int[][].class);
                        for (int i = 0; i < matrice.length; i++) {
                            for (int j = 0; j < matrice[i].length; j++) {
                                Case c = grille.getCase(i, j);
                                int valLue = matrice[i][j];
                                if (valLue >= Etape.OFFSET_HYPOTHESE) {
                                    c.setValeur(valLue - Etape.OFFSET_HYPOTHESE);
                                    c.setEstHypothese(true);
                                } else {
                                    c.setValeur(valLue);
                                    c.setEstHypothese(false);
                                }
                                c.effacerNotes();
                            }
                        }
                    }
                    lecteurJson.close();
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }
    }

    /**
     * Supprime les fichiers de sauvegarde de cette partie.
     * @param compte le nom du profil
     */
    public void effacer(String compte) {
        String cheminSave = Constantes.DOSSIER_PROFILS + compte + Constantes.SOUS_DOSSIER_PARTIES;
        if (this.mode == ModeDeJeu.AVEN) cheminSave += Constantes.SOUS_DOSSIER_AVENTURE;
        
        File ini = new File(cheminSave + this.idGrille + ".ini");
        if (ini.isFile()) ini.delete();

        File json = new File(cheminSave + this.idGrille + ".json");
        if (json.isFile()) json.delete();

        terminee = false;
    }
}