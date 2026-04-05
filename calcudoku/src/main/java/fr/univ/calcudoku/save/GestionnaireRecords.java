package fr.univ.calcudoku.save;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.univ.calcudoku.utils.Constantes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère les records (meilleurs scores) de toutes les grilles.
 * Les records sont stockés dans un fichier JSON partagé entre tous les profils.
 */
public class GestionnaireRecords {
    
    private static final String CHEMIN_RECORDS = Constantes.DOSSIER_PROFILS + Constantes.FICHIER_RECORDS;
    private static Map<String, Record> records = null;

    /** Charge les records depuis le fichier JSON. */
    public static void charger() {
        File fichier = new File(CHEMIN_RECORDS);
        if (!fichier.exists()) {
            records = new HashMap<>();
            return;
        }
        try (FileReader reader = new FileReader(fichier)) {
            Type type = new TypeToken<HashMap<String, Record>>(){}.getType();
            records = new Gson().fromJson(reader, type);
            if (records == null) records = new HashMap<>();
        } catch (Exception e) {
            records = new HashMap<>();
        }
    }

    private static void sauvegarder() {
        if (records == null) return;
        try (FileWriter writer = new FileWriter(CHEMIN_RECORDS)) {
            new Gson().toJson(records, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère le record d'une grille.
     * @param idGrille l'identifiant de la grille
     * @return le record ou null si aucun
     */
    public static Record getRecord(String idGrille) {
        if (records == null) charger();
        return records.get(idGrille);
    }

    /**
     * Enregistre un nouveau record si le score est meilleur (ou le temps en cas d'égalité).
     * @param idGrille l'identifiant de la grille
     * @param score le score obtenu
     * @param temps le temps en secondes
     * @param joueur le nom du joueur
     */
    public static void enregistrerSiMeilleur(String idGrille, long score, int temps, String joueur) {
        if (records == null) charger();
        
        Record actuel = records.get(idGrille);
        boolean nouveauRecord = false;

        if (actuel == null) {
            nouveauRecord = true;
        } else if (score > actuel.score) {
            nouveauRecord = true;
        } else if (score == actuel.score && temps < actuel.temps) {
            nouveauRecord = true;
        }

        if (nouveauRecord) {
            records.put(idGrille, new Record(score, temps, joueur));
            sauvegarder();
        }
    }

    /**
     * Supprime tous les records détenus par un joueur.
     * @param joueur le nom du joueur dont les records doivent être retirés
     */
    public static void supprimerRecordsDuJoueur(String joueur) {
        if (records == null) charger();
        boolean modifie = records.entrySet().removeIf(e -> joueur.equals(e.getValue().joueur));
        if (modifie) sauvegarder();
    }
}