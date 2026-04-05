package fr.univ.calcudoku.service;

import fr.univ.calcudoku.save.Options;
import fr.univ.calcudoku.save.Statistiques;
import fr.univ.calcudoku.save.Sauvegarde;
import fr.univ.calcudoku.save.GestionnaireRecords;
import fr.univ.calcudoku.utils.Constantes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère la création, la suppression, le chargement et la gestion des profils utilisateurs.
 * Inclut la persistance des statistiques et des options spécifiques à chaque profil.
 */
public class ProfileManager {

    /** Le nom du profil actuellement actif. */
    private String profilActif;

    /**
     * Constructeur du ProfileManager.
     * S'assure que le dossier racine des profils existe.
     */
    public ProfileManager() {
        File dossier = new File(Constantes.DOSSIER_PROFILS);
        if (!dossier.exists()) dossier.mkdir();
    }

    /**
     * Crée un nouveau profil utilisateur avec le nom spécifié.
     * @param nom Le nom du profil à créer.
     * @return true si le profil a été créé avec succès, false si un profil avec ce nom existe déjà ou si la création des dossiers échoue.
     */
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

    /**
     * Supprime un profil utilisateur et tous les fichiers associés.
     * @param nom Le nom du profil à supprimer.
     * @return true si le profil a été supprimé avec succès, false sinon.
     */
    public boolean supprimerProfil(String nom) {
        GestionnaireRecords.supprimerRecordsDuJoueur(nom);
        File dossier = new File(Constantes.DOSSIER_PROFILS, nom);
        if (dossier.exists()) return supprimerRecursif(dossier);
        return false;
    }

    /**
     * Méthode utilitaire récursive pour supprimer un fichier ou un répertoire et son contenu.
     * @param fichierOuDossier Le fichier ou le dossier à supprimer.
     * @return true si la suppression a été effectuée avec succès, false sinon.
     */
    private boolean supprimerRecursif(File fichierOuDossier) {
        if (fichierOuDossier.isDirectory()) {
            File[] enfants = fichierOuDossier.listFiles();
            if (enfants != null) {
                for (File enfant : enfants) supprimerRecursif(enfant);
            }
        }
        return fichierOuDossier.delete();
    }

    /**
     * Liste tous les noms de profils existants.
     * @return Une liste de chaînes de caractères représentant les noms des profils.
     */
    public List<String> listerProfils() {
        List<String> noms = new ArrayList<>();
        File dossier = new File(Constantes.DOSSIER_PROFILS);
        File[] sousDossiers = dossier.listFiles(File::isDirectory);
        if (sousDossiers != null) {
            for (File f : sousDossiers) noms.add(f.getName());
        }
        return noms;
    }

    /**
     * Définit le profil utilisateur actif.
     * @param nom Le nom du profil à activer.
     */
    public void chargerProfil(String nom) {
        this.profilActif = nom;
    }

    /**
     * Retourne le nom du profil actuellement actif.
     * @return Le nom du profil actif.
     */
    public String getProfilActif() {
        return profilActif;
    }
    
    /**
     * Enregistre les statistiques de fin de partie pour le profil actif.
     * Met à jour le nombre de parties jouées, les victoires, le score, la progression en mode Aventure,
     * la difficulté maximale et le temps moyen.
     */
    public void enregistrerFinDePartie(String nomProfil, boolean victoire, double temps, long score, Sauvegarde.Difficulte diff, String idGrille) {
        Statistiques stats = new Statistiques();
        stats.charger(nomProfil);

        stats.setPartiesJouees(stats.getPartiesJouees() + 1);
        
        if (victoire) {
            stats.setVictoires(stats.getVictoires() + 1);
            if (score > stats.getScore()) stats.setScore(score);
            if (diff != null && diff.ordinal() > stats.getDiffMax().ordinal()) stats.setDiffMax(diff);
            
            if (idGrille != null) {
                if (idGrille.startsWith("aventure_")) {
                    try {
                        int niveauGagne = Integer.parseInt(idGrille.replace("aventure_", ""));
                        if (niveauGagne >= stats.getProgressionAventure()) {
                            stats.setProgressionAventure(niveauGagne + 1);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur parsing progression aventure: " + e.getMessage());
                    
                        System.err.println("Erreur parsing progression aventure: " + e.getMessage());
                    }
                } else if (idGrille.startsWith("libre_")) {
                    fr.univ.calcudoku.save.GestionnaireRecords.enregistrerSiMeilleur(idGrille, score, (int) temps, nomProfil);
                }
            }
            
            double ancienneMoyenne = stats.getMoyenne() != null ? stats.getMoyenne() : 0.0;
            int nbVictoires = stats.getVictoires(); 
            double nouvelleMoyenne = ancienneMoyenne + ((temps - ancienneMoyenne) / nbVictoires);
            stats.setMoyenne(nouvelleMoyenne);
        }
        
        stats.setRatioVictoires();
        stats.enreg(nomProfil);
    }

    /**
     * Lit et retourne les statistiques et options d'un profil sous forme de Map.
     * @param nomProfil Le nom du profil dont on veut lire les statistiques.
     * @return Une Map où les clés sont les noms des statistiques/options et les valeurs sont leurs représentations textuelles.
     */
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

    /**
     * Met à jour une statistique ou une option spécifique pour un profil donné.
     * @param nomProfil Le nom du profil à modifier.
     * @param cle La clé de la statistique ou de l'option (ex: "mode_sombre", "progression").
     * @param valeur La nouvelle valeur à assigner à la clé.
     */
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
            } catch (Exception e) {
                System.err.println("Erreur parsing progression: " + e.getMessage());
            }
            stats.enreg(nomProfil);
        } else if (cle.equals(Constantes.STAT_PARTIES_JOUEES)) {
            Statistiques stats = new Statistiques();
            stats.charger(nomProfil);
            try {
                stats.setPartiesJouees(Integer.parseInt(valeur));
            } catch (Exception e) {
                System.err.println("Erreur parsing parties jouées: " + e.getMessage());
            }
            stats.enreg(nomProfil);
        } else if (cle.equals(Constantes.STAT_VICTOIRES)) {
            Statistiques stats = new Statistiques();
            stats.charger(nomProfil);
            try {
                stats.setVictoires(Integer.parseInt(valeur));
            } catch (Exception e) {
                System.err.println("Erreur parsing victoires: " + e.getMessage());
            }
            stats.enreg(nomProfil);
        }
    }
}