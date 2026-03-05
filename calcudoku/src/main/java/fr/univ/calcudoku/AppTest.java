package fr.univ.calcudoku;

import fr.univ.calcudoku.utils.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class AppTest extends Application {

    @Override
    public void start(Stage primaryStage){  
        GestionnaireJeu.chargerPartie(primaryStage, "1.json");
    }

    public static void main(String[] args) {
        launch(args);
    }
}