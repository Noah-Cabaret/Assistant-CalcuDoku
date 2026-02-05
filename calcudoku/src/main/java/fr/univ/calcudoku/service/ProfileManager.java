package fr.univ.calcudoku.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String DOSSIER_ROOT = "profils";

    // Fichiers à la racine du profil
    private static final String FICHIER_OPTIONS = "options.ini";
    private static final String FICHIER_STATS = "statistiques.ini";
    
    // Dossier et fichiers de sauvegardes
    private static final String DOSSIER_PARTIES = "parties";
    private static final String SAVE_AVENTURE = "aventure.ini";
    private static final String SAVE_LIBRE = "libre.ini";

    private String profilActif;

    public ProfileManager() {
        // Initialisation du dossier racine
        File dossier = new File(DOSSIER_ROOT);
        if (!dossier.exists()) {
            dossier.mkdir();
        }
    }

    /**
     * Crée la structure de donnés de profil : Dossiers + 5 fichiers vides (.ini)
     */
    public boolean creerProfil(String nom) {
        File dossierProfil = new File(DOSSIER_ROOT, nom);
        if (dossierProfil.exists()) {
            return false;
        }

        if (dossierProfil.mkdirs()) {
            try {
                // Fichiers généraux du profil
                new File(dossierProfil, FICHIER_OPTIONS).createNewFile();
                new File(dossierProfil, FICHIER_STATS).createNewFile();

                // Dossier des parties
                File dossierParties = new File(dossierProfil, DOSSIER_PARTIES);
                dossierParties.mkdirs();

                // Les fichiers de sauvegarde pour chaque mode
                // Aventure : pour stocker le niveau actuel et la progression
                new File(dossierParties, SAVE_AVENTURE).createNewFile();
                
                // Libre : pour reprendre une partie libre en cours
                new File(dossierParties, SAVE_LIBRE).createNewFile();

                System.out.println(" Profil créé avec succès : " + nom);
                return true;

            } catch (IOException e) {
                System.err.println("Erreur création fichiers : " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // Suppression, Listing, Chargement

    public boolean supprimerProfil(String nom) {
        File dossier = new File(DOSSIER_ROOT, nom);
        if (dossier.exists()) {
            return supprimerRecursif(dossier);
        }
        return false;
    }

    private boolean supprimerRecursif(File fichierOuDossier) {
        if (fichierOuDossier.isDirectory()) {
            File[] enfants = fichierOuDossier.listFiles();
            if (enfants != null) {
                for (File enfant : enfants) {
                    supprimerRecursif(enfant);
                }
            }
        }
        return fichierOuDossier.delete();
    }

    public List<String> listerProfils() {
        List<String> noms = new ArrayList<>();
        File dossier = new File(DOSSIER_ROOT);
        File[] sousDossiers = dossier.listFiles(File::isDirectory);
        if (sousDossiers != null) {
            for (File f : sousDossiers) {
                noms.add(f.getName());
            }
        }
        return noms;
    }

    public void chargerProfil(String nom) {
        this.profilActif = nom;
        System.out.println("Profil chargé : " + nom);
    }

    public String getProfilActif() {
        return profilActif;
    }
}