package fr.univ.calcudoku.service;

import fr.univ.calcudoku.model.*; 
import java.util.Map;

/**
 * Adaptateur de conversion des données JSON vers le modèle objet.
 * Transforme les données brutes JSON en objets métier (Grille, GroupementCases, Case).
 */
public class JsonToModelAdapter {

    /**
     * Convertit les données JSON en un objet Grille compatible.
     */
    public static Grille convertir(DonneesNiveau data) {
        Grille grille = new Grille(data.dim);

        if (data.blocs != null) {
            for (BlocData blocJson : data.blocs) {
                Operation op = traduireOperation(blocJson.op);
                GroupementCases groupement = new GroupementCases(op, blocJson.result);

                for (Map.Entry<String, Integer> entry : blocJson.nums.entrySet()) {
                    String[] coords = entry.getKey().split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int solutionAttendue = entry.getValue();

                    Case nouvelleCase = new Case(x, y, solutionAttendue);
                    grille.setCase(x, y, nouvelleCase);
                    groupement.ajouterCase(nouvelleCase);
                }
                
                grille.ajouterGroupement(groupement);
            }
        }

        return grille;
    }

    private static Operation traduireOperation(String opJson) {
        if (opJson == null) return Operation.RIEN;
        switch (opJson) {
            case "+": return Operation.ADDITION;
            case "-": return Operation.SOUSTRACTION;
            case "x": 
            case "*": return Operation.MULTIPLICATION; 
            case "/":
            case "∕": 
            case "÷": return Operation.DIVISION;       
            default: return Operation.RIEN;
        }
    }
}