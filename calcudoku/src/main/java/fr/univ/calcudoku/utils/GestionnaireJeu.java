package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.controller.JeuController;
import fr.univ.calcudoku.model.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.FileReader;
import java.util.Map;

public class GestionnaireJeu {
    public static void chargerPartie(Stage stage, String fichierJson) {
        try {
            Gson gson = new Gson();
            String path = GestionnaireJeu.class.getResource("/json/" + fichierJson).getPath();
            JsonObject json = gson.fromJson(new FileReader(path), JsonObject.class);

            int dim = json.get("dim").getAsInt();
            JsonArray blocs = json.getAsJsonArray("blocs");

            int[][] matriceSolution = new int[dim][dim];

            for (JsonElement blocElem : blocs) {
                JsonObject bloc = blocElem.getAsJsonObject();
                JsonObject nums = bloc.getAsJsonObject("nums");

                for (Map.Entry<String, JsonElement> entry : nums.entrySet()) {
                    String pos = entry.getKey();
                    String[] coords = pos.split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int val = entry.getValue().getAsInt();
                    matriceSolution[x][y] = val;
                }
            }

            Grille grille = new Grille(dim, matriceSolution, null);

            for (JsonElement blocElem : blocs) {
                JsonObject bloc = blocElem.getAsJsonObject();
                String opStr = bloc.get("op").getAsString();
                Operation op = switch (opStr) {
                    case "+" -> Operation.ADDITION;
                    case "-" -> Operation.SOUSTRACTION;
                    case "*" -> Operation.MULTIPLICATION;
                    case "/" -> Operation.DIVISION;
                    default -> Operation.RIEN;
                };
                int result = bloc.get("result").getAsInt();
                JsonObject nums = bloc.getAsJsonObject("nums");

                GroupementCases group = new GroupementCases(op, result);

                for (Map.Entry<String, JsonElement> entry : nums.entrySet()) {
                    String pos = entry.getKey();
                    String[] coords = pos.split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    Case c = grille.getCase(x, y);
                    group.ajouterCase(c);
                }

                grille.ajouterGroupement(group);
            }

            lancerPartie(stage, grille, "Calcudoku - Partie chargée");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la partie !");
        }
    }

    private static void lancerPartie(Stage stage, Grille grille, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource("/fxml/VuePartie.fxml"));
            Parent root = loader.load();

            JeuController controller = loader.getController();
            controller.initialiserPartie(grille);

            Scene scene = new Scene(root, 1000, 800);

            if (GestionnaireJeu.class.getResource("/style.css") != null) {
                scene.getStylesheets().add(GestionnaireJeu.class.getResource("/style.css").toExternalForm());
            }

            stage.setTitle(titre);
            stage.setScene(scene);
            
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du lancement de la partie !");
        }
    }
}