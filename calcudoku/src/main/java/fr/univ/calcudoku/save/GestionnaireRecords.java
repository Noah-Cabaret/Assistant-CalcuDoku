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

public class GestionnaireRecords {
    
    private static final String CHEMIN_RECORDS = Constantes.DOSSIER_PROFILS + "/records.json";
    private static Map<String, Record> records = null;

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

    public static Record getRecord(String idGrille) {
        if (records == null) charger();
        return records.get(idGrille);
    }

    public static void enregistrerSiMeilleur(String idGrille, long score, int temps, String joueur) {
        if (records == null) charger();
        
        Record actuel = records.get(idGrille);
        boolean nouveauRecord = false;

        if (actuel == null) {
            nouveauRecord = true;
        } else if (score > actuel.score) {
            nouveauRecord = true;
        } else if (score == actuel.score && temps < actuel.temps) {
            nouveauRecord = true; // Égalité de score, mais meilleur temps
        }

        if (nouveauRecord) {
            records.put(idGrille, new Record(score, temps, joueur));
            sauvegarder();
        }
    }
}