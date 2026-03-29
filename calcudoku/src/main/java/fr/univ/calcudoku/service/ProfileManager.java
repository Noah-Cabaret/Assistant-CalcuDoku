package fr.univ.calcudoku.service;

import fr.univ.calcudoku.save.Options;
import fr.univ.calcudoku.save.Statistiques;
import fr.univ.calcudoku.save.Sauvegarde;
import fr.univ.calcudoku.utils.Constantes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileManager {

    private String profilActif;

    public ProfileManager() {
        File dossier = new File(Constantes.DOSSIER_PROFILS);
        if (!dossier.exists()) dossier.mkdir();
    }

    public boolean creerProfil(String nom) {
        File dossierProfil = new File(Constantes.DOSSIER_PROFILS, nom);
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
        File dossier = new File(Constantes.DOSSIER_PROFILS, nom);
        if (dossier.exists()) return supprimerRecursif(dossier);
        return false;
    }

    private boolean supprimerRecursif(File fichierOuDossier) {
        if (fichierOuDossier.isDirectory()) {
            File[] enfants = fichierOuDossier.listFiles();
            if (enfants != null) {
                for (File enfant : enfants) supprimerRecursif(enfant);
            }
        }
        return fichierOuDossier.delete();
    }

    public List<String> listerProfils() {
        List<String> noms = new ArrayList<>();
        File dossier = new File(Constantes.DOSSIER_PROFILS);
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
    
    public void enregistrerFinDePartie(String nomProfil, boolean victoire, double temps, long score, Sauvegarde.Difficulte diff, boolean estAventure) {
        Statistiques stats = new Statistiques();
        stats.charger(nomProfil);

        stats.setPartiesJouees(stats.getPartiesJouees() + 1);
        
        if (victoire) {
            stats.setVictoires(stats.getVictoires() + 1);
            if (score > stats.getScore()) stats.setScore(score);
            if (diff != null && diff.ordinal() > stats.getDiffMax().ordinal()) stats.setDiffMax(diff);
            if (estAventure) stats.setProgressionAventure(stats.getProgressionAventure() + 1);
            
            double ancienneMoyenne = stats.getMoyenne() != null ? stats.getMoyenne() : 0.0;
            int nbVictoires = stats.getVictoires(); 
            double nouvelleMoyenne = ancienneMoyenne + ((temps - ancienneMoyenne) / nbVictoires);
            stats.setMoyenne(nouvelleMoyenne);
        }
        
        stats.setRatioVictoires();
        stats.enreg(nomProfil);
    }

    public Map<String, String> lireStatistiques(String nomProfil) {
        Map<String, String> statsMap = new HashMap<>();
        Statistiques stats = new Statistiques();
        
        File fichierStats = new File(Constantes.DOSSIER_PROFILS + nomProfil + "/statistiques.ini");
        if (fichierStats.exists()) stats.charger(nomProfil);

        statsMap.put(Constantes.STAT_PARTIES_JOUEES, String.valueOf(stats.getPartiesJouees()));
        statsMap.put(Constantes.STAT_VICTOIRES, String.valueOf(stats.getVictoires()));
        statsMap.put(Constantes.STAT_TEMPS_MOYEN, String.valueOf((int)(stats.getMoyenne() != null ? stats.getMoyenne() : 0.0))); 
        
        double ratio = 0.0;
        if (stats.getPartiesJouees() > 0) {
            ratio = (double) stats.getVictoires() / (double) stats.getPartiesJouees();
        }
        statsMap.put(Constantes.STAT_RATIO, String.valueOf(ratio));
        
        statsMap.put(Constantes.STAT_PROGRESSION, String.valueOf(stats.getProgressionAventure()));
        
        String diff = "1";
        if (stats.getDiffMax() == Sauvegarde.Difficulte.MOYEN) diff = "2";
        else if (stats.getDiffMax() == Sauvegarde.Difficulte.DIFFI) diff = "3";
        statsMap.put(Constantes.STAT_DIFF_MAX, diff);
        
        statsMap.put(Constantes.STAT_SCORE_MAX, String.valueOf(stats.getScore()));
        
        Options opt = new Options();
        if (new File(Constantes.DOSSIER_PROFILS + nomProfil + "/options.ini").exists()) opt.charger(nomProfil);
        
        statsMap.put(Constantes.OPTION_MODE_SOMBRE, String.valueOf(opt.isThemeSombre()));
        if (opt.getAide() == Options.AideAuCalcul.CALCULATRICE) {
            statsMap.put(Constantes.OPTION_AIDE_CALCUL, Constantes.VALEUR_AIDE_CALCULATRICE);
        } else {
            statsMap.put(Constantes.OPTION_AIDE_CALCUL, Constantes.VALEUR_AIDE_COMBINAISONS);
        }

        return statsMap;
    }

    public void mettreAJourStatistique(String nomProfil, String cle, String valeur) {
        if (cle.equals(Constantes.OPTION_MODE_SOMBRE) || cle.equals(Constantes.OPTION_AIDE_CALCUL)) {
            Options opt = new Options();
            opt.charger(nomProfil);
            if (cle.equals(Constantes.OPTION_MODE_SOMBRE)) {
                opt.setThemeSombre(Boolean.parseBoolean(valeur));
            } else if (cle.equals(Constantes.OPTION_AIDE_CALCUL)) {
                opt.setAide(valeur.equals(Constantes.VALEUR_AIDE_CALCULATRICE) ? Options.AideAuCalcul.CALCULATRICE : Options.AideAuCalcul.COMBINAISONS);
            }
            opt.enreg(nomProfil);
        } else if (cle.equals(Constantes.STAT_PROGRESSION)) {
            Statistiques stats = new Statistiques();
            stats.charger(nomProfil);
            try {
                stats.setProgressionAventure(Integer.parseInt(valeur));
            } catch (Exception e) {}
            stats.enreg(nomProfil);
        } else if (cle.equals(Constantes.STAT_PARTIES_JOUEES)) {
            Statistiques stats = new Statistiques();
            stats.charger(nomProfil);
            try {
                stats.setPartiesJouees(Integer.parseInt(valeur));
            } catch (Exception e) {}
            stats.enreg(nomProfil);
        } else if (cle.equals(Constantes.STAT_VICTOIRES)) {
            Statistiques stats = new Statistiques();
            stats.charger(nomProfil);
            try {
                stats.setVictoires(Integer.parseInt(valeur));
            } catch (Exception e) {}
            stats.enreg(nomProfil);
        } // Ajoutez d'autres statistiques si besoin
    }
}