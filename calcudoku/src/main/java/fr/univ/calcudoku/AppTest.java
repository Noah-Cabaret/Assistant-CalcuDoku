package fr.univ.calcudoku;

import fr.univ.calcudoku.utils.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class AppTest extends Application {

    @Override
    public void start(Stage primaryStage){  
        GestionnaireJeu.lancerNouvellePartie(primaryStage, 5);
    }

    public static void main(String[] args) {
        launch(args);
    }
}