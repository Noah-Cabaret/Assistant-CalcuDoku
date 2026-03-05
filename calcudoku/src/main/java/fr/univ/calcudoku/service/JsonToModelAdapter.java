package fr.univ.calcudoku.service;

import fr.univ.calcudoku.model.*; 
import java.util.Map;

public class JsonToModelAdapter {

    /**
     * Convertit les données JSON en un objet Grille compatible
     */
    public static Grille convertir(DonneesNiveau data) {
        
        // contournement du constructeur de la grille
        int[][] dummySolution = new int[data.dim][data.dim];
        int[][] dummyDepart = new int[data.dim][data.dim];
        
        // On instancie la Grille avec ces fausses données
        Grille grille = new Grille(data.dim, dummySolution, dummyDepart);

        // reconstruction des groupements (CAGES)
        if (data.blocs != null) {
            for (BlocData blocJson : data.blocs) {
                
                // traduction operation (JSON -> ENUM)
                Operation op = traduireOperation(blocJson.op);
                
                // Création du groupement
                GroupementCases groupement = new GroupementCases(op, blocJson.result);

                // remplissage des cases
                for (Map.Entry<String, Integer> entry : blocJson.nums.entrySet()) {
                    // Parsing "x,y"
                    String[] coords = entry.getKey().split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int valeurJouee = entry.getValue();

                    // On récupère la case existante
                    Case c = grille.getCase(x, y);
                    
                    // On injecte la valeur
                    c.setValeur(valeurJouee);
                    
                    // On lie au groupement
                    groupement.ajouterCase(c);
                }
                
                // On ajoute le groupement à la grille
                grille.ajouterGroupement(groupement);
            }
        }

        return grille;
    }

    // Méthode privée pour traduire les symboles
    private static Operation traduireOperation(String opJson) {
        if (opJson == null) return Operation.RIEN;
        switch (opJson) {
            case "+": return Operation.ADDITION;
            case "-": return Operation.SOUSTRACTION;
            case "*": 
            case "x": return Operation.MULTIPLICATION; // Adapte "*" du JSON vers l'Enum
            case "/": 
            case ":": return Operation.DIVISION;       // Adapte "/" du JSON vers l'Enum
            default: return Operation.RIEN;
        }
    }
}