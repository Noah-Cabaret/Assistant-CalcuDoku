package fr.univ.calcudoku.service;

import fr.univ.calcudoku.save.Options;
import fr.univ.calcudoku.save.Statistiques;
import fr.univ.calcudoku.save.Sauvegarde.Difficulte;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire des profils utilisateur.
 * Gère la création, la sélection et la récupération des profils.
 */
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
            // 1. Création stricte des dossiers utiles
            new File(dossierProfil, "parties/aventure").mkdirs();
            new File(dossierProfil, "jeu/images").mkdirs();
            new File(dossierProfil, "jeu/json").mkdirs();
            new File(dossierProfil, "jeu/ini").mkdirs();

            // 2. L'ASTUCE : On force tes classes à générer leurs fichiers formatés avec des valeurs à 0 !
            new Options().enreg(nom);
            new Statistiques().enreg(nom);

            System.out.println("Profil créé et formaté avec succès : " + nom);
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
        System.out.println("Profil actif : " + nom);
    }

    public String getProfilActif() {
        return profilActif;
    }

    /**
     * Utilise TA vraie classe Statistiques pour nourrir l'interface visuelle du profil !
     */
    public Map<String, String> lireStatistiques(String nomProfil) {
        Map<String, String> statsMap = new HashMap<>();
        Statistiques stats = new Statistiques();
        
        File fichierStats = new File("profils/" + nomProfil + "/statistiques.ini");
        if (fichierStats.exists()) {
            stats.charger(nomProfil);
        }

        // On convertit les attributs de ta classe pour l'affichage
        statsMap.put("temps_total", String.valueOf((int)(stats.getMoyenne() != null ? stats.getMoyenne() : 0.0))); 
        statsMap.put("ratio_parties", String.valueOf(stats.getRatioVictoires() != null ? stats.getRatioVictoires() : 0.0));
        statsMap.put("progression", String.valueOf(stats.getProgressionAventure()));
        
        String diff = "1";
        if (stats.getDiffMax() == Difficulte.MOYEN) diff = "2";
        else if (stats.getDiffMax() == Difficulte.DIFFI) diff = "3";
        statsMap.put("difficulte_max", diff);
        
        statsMap.put("score_max", String.valueOf(stats.getScore()));
        
        // On lit aussi le thème depuis Options
        Options opt = new Options();
        if (new File("profils/" + nomProfil + "/options.ini").exists()) opt.charger(nomProfil);
        statsMap.put("mode_sombre", String.valueOf(opt.isThemeSombre()));

        return statsMap;
    }

    /**
     * Traducteur Universel : Permet au reste de ton jeu de continuer à mettre à jour
     * les statistiques sans crasher, en utilisant tes nouvelles classes !
     */
    public void mettreAJourStatistique(String nomProfil, String cle, String valeur) {
        
        // 1. Si on modifie une option (comme le mode sombre)
        if (cle.equals("mode_sombre")) {
            Options opt = new Options();
            opt.charger(nomProfil);
            opt.setThemeSombre(Boolean.parseBoolean(valeur));
            opt.enreg(nomProfil);
            return;
        }
        
        // 2. Si on modifie une statistique de jeu
        Statistiques stats = new Statistiques();
        stats.charger(nomProfil);
        
        try {
            if (cle.equals("progression") || cle.equals("progression_aventure")) {
                stats.setProgressionAventure(Integer.parseInt(valeur));
            } else if (cle.equals("parties_jouees")) {
                stats.setPartiesJouees(Integer.parseInt(valeur));
            } else if (cle.equals("victoires")) {
                stats.setVictoires(Integer.parseInt(valeur));
            } else if (cle.equals("score_max") || cle.equals("score")) {
                stats.setScore(Long.parseLong(valeur));
            } else if (cle.equals("temps_total") || cle.equals("temps_moyen")) {
                stats.setMoyenne(Double.parseDouble(valeur));
            }
            
            // On recalcule le ratio automatiquement pour éviter les bugs
            stats.setRatioVictoires();
            
            // On sauvegarde le tout proprement dans le fichier .ini
            stats.enreg(nomProfil);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour stat : " + e.getMessage());
        }
    }
}