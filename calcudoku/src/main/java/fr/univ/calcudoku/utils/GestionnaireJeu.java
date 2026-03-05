package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.controller.JeuController;
import fr.univ.calcudoku.model.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Random;

public class GestionnaireJeu {
    //sera remplacer par le chargement d'un fichier pour initialiser une partie
    public static void lancerNouvellePartie(Stage stage, int taille) {
        try {
            int[][] sol = new int[taille][taille];
            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    sol[y][x] = (x + y) % taille + 1;
                }
            }

            Grille grilleModele = new Grille(taille, sol, null);

            boolean[][] visite = new boolean[taille][taille];
            Random rand = new Random();
            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    if (!visite[x][y]) {
                        GroupementCases cage = new GroupementCases(Operation.ADDITION, 0);
                        int somme = 0;
                        int currX = x, currY = y;
                        for (int i = 0; i < 3; i++) {
                            if (currX < taille && currY < taille && !visite[currX][currY]) {
                                visite[currX][currY] = true;
                                Case c = grilleModele.getCase(currX, currY);
                                cage.ajouterCase(c);
                                somme += c.getSolution();
                                if (rand.nextBoolean()) currX++; else currY++;
                            }
                        }
                        cage.setResultatCible(somme);
                        grilleModele.ajouterGroupement(cage);
                    }
                }
            }

            FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource("/fxml/VuePartie.fxml"));
            Parent root = loader.load();

            JeuController controller = loader.getController();
            controller.initialiserPartie(grilleModele);

            Scene scene = new Scene(root, 1000, 800); 

            if (GestionnaireJeu.class.getResource("/style.css") != null) {
                scene.getStylesheets().add(GestionnaireJeu.class.getResource("/style.css").toExternalForm());
            }

            stage.setTitle("Calcudoku - Partie en cours");
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