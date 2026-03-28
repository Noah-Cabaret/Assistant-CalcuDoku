package fr.univ.calcudoku.service;

import fr.univ.calcudoku.save.Options;
import fr.univ.calcudoku.save.Statistiques;
import fr.univ.calcudoku.save.Sauvegarde;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileManager {

    private static final String DOSSIER_ROOT = "profils";
    private String profilActif;

    public ProfileManager() {
        File dossier = new File(DOSSIER_ROOT);
        if (!dossier.exists()) dossier.mkdir();
    }

    public boolean creerProfil(String nom) {
        File dossierProfil = new File(DOSSIER_ROOT, nom);
        if (dossierProfil.exists()) return false;

        if (dossierProfil.mkdirs()) {
            new File(dossierProfil, "parties/aventure").mkdirs();
            new File(dossierProfil, "jeu/images").mkdirs();
            new File(dossierProfil, "jeu/json").mkdirs();
            new File(dossierProfil, "jeu/ini").mkdirs();

            new Options().enreg(nom);
            new Statistiques().enreg(nom);
            return true;
        }
        return false;
    }

    public boolean supprimerProfil(String nom) {
        File dossier = new File(DOSSIER_ROOT, nom);
        if (dossier.exists()) return supprimerRecursif(dossier);
        return false;
    }

    private boolean supprimerRecursif(File fichierOuDossier) {
        if (fichierOuDossier.isDirectory()) {
            File[] enfants = fichierOuDossier.listFiles();
            if (enfants != null) for (File enfant : enfants) supprimerRecursif(enfant);
        }
        return fichierOuDossier.delete();
    }

    public List<String> listerProfils() {
        List<String> noms = new ArrayList<>();
        File dossier = new File(DOSSIER_ROOT);
        File[] sousDossiers = dossier.listFiles(File::isDirectory);
        if (sousDossiers != null) {
            for (File f : sousDossiers) noms.add(f.getName());
        }
        return noms;
    }

    public void chargerProfil(String nom) {
        this.profilActif = nom;
    }

    public String getProfilActif() {
        return profilActif;
    }
    
    // --- NOUVEAU : CALCULE ET SAUVEGARDE LA FIN D'UNE PARTIE ---
    public void enregistrerFinDePartie(String nomProfil, boolean victoire, double temps, long score, Sauvegarde.Difficulte diff, boolean estAventure) {
        if(nomProfil.equals("Invité")) return; 
        
        Statistiques stats = new Statistiques();
        stats.charger(nomProfil);

        stats.setPartiesJouees(stats.getPartiesJouees() + 1);
        
        if (victoire) {
            stats.setVictoires(stats.getVictoires() + 1);
            if (score > stats.getScore()) stats.setScore(score);
            if (diff != null && diff.ordinal() > stats.getDiffMax().ordinal()) stats.setDiffMax(diff);
            if (estAventure) stats.setProgressionAventure(stats.getProgressionAventure() + 1);
        }
        stats.setRatioVictoires();

        // Calcul de la moyenne de temps globale
        double ancienneMoyenne = stats.getMoyenne() != null ? stats.getMoyenne() : 0.0;
        int parties = stats.getPartiesJouees();
        double nouvelleMoyenne = ancienneMoyenne + ((temps - ancienneMoyenne) / parties);
        stats.setMoyenne(nouvelleMoyenne);

        stats.enreg(nomProfil);
    }

    public Map<String, String> lireStatistiques(String nomProfil) {
        Map<String, String> statsMap = new HashMap<>();
        Statistiques stats = new Statistiques();
        
        File fichierStats = new File("profils/" + nomProfil + "/statistiques.ini");
        if (fichierStats.exists()) stats.charger(nomProfil);

        // --- NOUVEAU : Toutes les stats sont envoyées à l'interface ---
        statsMap.put("parties_jouees", String.valueOf(stats.getPartiesJouees()));
        statsMap.put("victoires", String.valueOf(stats.getVictoires()));
        statsMap.put("temps_moyen", String.valueOf((int)(stats.getMoyenne() != null ? stats.getMoyenne() : 0.0))); 
        statsMap.put("ratio_parties", String.valueOf(stats.getRatioVictoires() != null ? stats.getRatioVictoires() : 0.0));
        statsMap.put("progression", String.valueOf(stats.getProgressionAventure()));
        
        String diff = "1";
        if (stats.getDiffMax() == Sauvegarde.Difficulte.MOYEN) diff = "2";
        else if (stats.getDiffMax() == Sauvegarde.Difficulte.DIFFI) diff = "3";
        statsMap.put("difficulte_max", diff);
        
        statsMap.put("score_max", String.valueOf(stats.getScore()));
        
        Options opt = new Options();
        if (new File("profils/" + nomProfil + "/options.ini").exists()) opt.charger(nomProfil);
        
        statsMap.put("mode_sombre", String.valueOf(opt.isThemeSombre()));
        if (opt.getAide() == Options.AideAuCalcul.CALCULATRICE) statsMap.put("aide_calcul", "calculatrice");
        else statsMap.put("aide_calcul", "combinaisons");

        return statsMap;
    }

    public void mettreAJourStatistique(String nomProfil, String cle, String valeur) {
        if (cle.equals("mode_sombre") || cle.equals("aide_calcul")) {
            Options opt = new Options();
            opt.charger(nomProfil);
            if (cle.equals("mode_sombre")) opt.setThemeSombre(Boolean.parseBoolean(valeur));
            else if (cle.equals("aide_calcul")) opt.setAide(valeur.equals("calculatrice") ? Options.AideAuCalcul.CALCULATRICE : Options.AideAuCalcul.COMBINAISONS);
            opt.enreg(nomProfil);
        }
    }
}