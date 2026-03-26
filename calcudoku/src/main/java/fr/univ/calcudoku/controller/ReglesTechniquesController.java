package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.CacheRessources;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ReglesTechniquesController {

    @FXML private ToggleGroup groupeOnglets;
    @FXML private Label lblTitreHaut;
    @FXML private Label lblTexte;
    @FXML private ImageView imgExemple;
    @FXML private Label lblLegendeImage;
    
    // Les nouveaux boutons de pagination
    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;

    // --- SYSTÈME DE PAGINATION ---
    private List<PageContenu> pagesDeLaSection = new ArrayList<>();
    private int indexPageActuelle = 0;

    // Petite classe interne pour stocker les infos d'une page
    private class PageContenu {
        String texte;
        String cheminImage;
        String legende;

        PageContenu(String texte, String cheminImage, String legende) {
            this.texte = texte;
            this.cheminImage = cheminImage;
            this.legende = legende;
        }
    }

    @FXML
    public void initialize() {
        // Appliquer une couleur grise à l'onglet sélectionné
        groupeOnglets.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                groupeOnglets.selectToggle(oldVal); 
                return;
            }
            for (javafx.scene.control.Toggle t : groupeOnglets.getToggles()) {
                ToggleButton btn = (ToggleButton) t;
                if (btn.isSelected()) {
                    btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #999999; -fx-border-radius: 10; -fx-background-color: #d3d3d3;");
                } else {
                    btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #999999; -fx-border-radius: 10; -fx-background-color: transparent;");
                }
            }
        });

        // --- FORCER LA COULEUR DU TEXTE (Sécurité anti-CSS Mode Sombre) ---
        if (MainApp.modeSombreActif) {
            lblTitreHaut.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 10; -fx-padding: 10 30 10 30; -fx-text-fill: white;");
            lblTexte.setStyle("-fx-font-size: 16px; -fx-line-spacing: 5px; -fx-text-fill: white;");
            lblLegendeImage.setStyle("-fx-font-size: 12px; -fx-text-fill: lightgray;");
            btnPrecedent.setStyle("-fx-background-color: transparent; -fx-font-size: 40px; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: white;");
            btnSuivant.setStyle("-fx-background-color: transparent; -fx-font-size: 40px; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            lblTitreHaut.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10 30 10 30; -fx-text-fill: black;");
            lblTexte.setStyle("-fx-font-size: 16px; -fx-line-spacing: 5px; -fx-text-fill: black;");
            lblLegendeImage.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        }

        // Charger la première section par défaut
        afficherRegles();
    }

    // --- ACTIONS DES FLÈCHES ---
    @FXML
    private void onPagePrecedente() {
        if (indexPageActuelle > 0) {
            indexPageActuelle--;
            afficherPageCourante();
        }
    }

    @FXML
    private void onPageSuivante() {
        if (indexPageActuelle < pagesDeLaSection.size() - 1) {
            indexPageActuelle++;
            afficherPageCourante();
        }
    }

    // --- MOTEUR D'AFFICHAGE ---
    private void chargerSection(String titre, List<PageContenu> pages) {
        lblTitreHaut.setText(titre);
        this.pagesDeLaSection = pages;
        this.indexPageActuelle = 0; // On repart à la page 1 de la section
        afficherPageCourante();
    }

    private void afficherPageCourante() {
        if (pagesDeLaSection.isEmpty()) return;

        PageContenu page = pagesDeLaSection.get(indexPageActuelle);

        // Mise à jour du texte et légende
        lblTexte.setText(page.texte);
        lblLegendeImage.setText(page.legende);

        // Mise à jour de l'image
        if (page.cheminImage != null && !page.cheminImage.isEmpty()) {
            imgExemple.setImage(CacheRessources.getImage(page.cheminImage));
            imgExemple.setVisible(true);
        } else {
            imgExemple.setImage(null);
            imgExemple.setVisible(false);
        }

        // Cacher/Afficher les flèches selon la position
        btnPrecedent.setVisible(indexPageActuelle > 0);
        btnSuivant.setVisible(indexPageActuelle < pagesDeLaSection.size() - 1);
    }

    // --- DÉFINITION DE VOS ONGLETS ---

    @FXML
    private void afficherRegles() {
        List<PageContenu> pages = new ArrayList<>();
        
        // PAGE 1
        pages.add(new PageContenu(
            "Le Calcudoku est une grille mathématique.\n\nRègle 1 : Remplir la grille avec les chiffres de 1 à N (N étant la taille de la grille).", 
            "/grilles/images/regle1.png", 
            "Exemple de grille 4x4 basique"
        ));
        
        // PAGE 2 (Sans image)
        pages.add(new PageContenu(
            "Règle 2 : Chaque chiffre ne doit apparaître qu'une seule fois par ligne et par colonne.\nMais un nombre peut être utilisé plus d'une fois dans le même bloc", 
            "/grilles/images/regle2.png", 
            "les chiffres ne sont pas identiques"
        ));
        
        // PAGE 3
        pages.add(new PageContenu(
            "Règle 3 : Les cages (blocs entourés en gras) doivent respecter l'opération mathématique indiquée en haut à gauche pour obtenir le résultat cible.", 
            "/grilles/images/regle3.png", // Mettez une image ici si vous en avez une !
            "Attention à l'opération (+, -, *, /)"
        ));

        chargerSection("Règles du Calcudoku", pages);
    }

    @FXML
    private void afficherFonctionnalites() {
        List<PageContenu> pages = new ArrayList<>();
        
        // PAGE 1 : Les aides au calcul depuis profil
        pages.add(new PageContenu(
            "Le jeu propose des aides pour vous assister dans vos calculs.\n\n" +
            "Dans votre Profil, vous pouvez choisir entre :\n" +
            "- Combinaisons : Affiche toutes les additions/multiplications possibles pour une cage donnée.\n" +
            "- Calculatrice : Ouvre une petite calculatrice classique pour faire vos propres essais.\n" +
            "De même, vous trouvez les statistiques liées à votre profil et les parties en cours depuis l'onglet de Profile.", 
            null, // Vous pourrez mettre une capture d'écran de la calculatrice ici plus tard !
            "Les aides au calcul"
        ));
        
        // PAGE 2 : Les contrôles et annotations
        pages.add(new PageContenu(
            "Vous pouvez personnaliser la façon dont vous remplissez la grille :\n\n" +
            "- Déplacement : Jouez principalement au clavier en naviguant avec les flèches.\n" +
            "- Placer chiffre : Cliquez sur une case pour ouvrir un menu circulaire et choisir votre chiffre.\n" +
            "- Annotation : Permet d'écrire des petits chiffres 'brouillon' dans les coins d'une case.", 
            null, // Une capture d'écran d'une case avec des annotations serait top ici
            "Les modes de saisie"
        ));

        // PAGE 3 : plein partie du jeu
        pages.add(new PageContenu(
            "Depuis une partie du jeu, vous possedez le menu roulant qui permet de :\n\n" +
            "- Changer le mode d'aide au calcul : combinaisons, calculatrice.\n" +
            "- Retour à Règles & Techniques : Consulter les règles du jeu, les fonctionnalités et les techniques.\n",
            null, // Une capture d'écran d'une case avec des annotations serait top ici
            "Menu roulant"
        ));

        chargerSection("Fonctionnalités et Outils", pages);
    }

    @FXML
    private void afficherTechnique1() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu("Si une cage ne contient qu'une seule case, elle ne possède aucune opération mathématique. Le petit chiffre inscrit en haut à gauche est donc directement la solution pour cette case !", null, ""));
        chargerSection("Techniques : Les Cages Uniques", pages);
    }

    @FXML
    private void afficherTechnique2() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu("Dans une petite grille, repérez les sommes extrêmes. Par exemple, dans une grille 4x4, si une cage de 2 cases demande '7+', les seuls chiffres possibles sont 3 et 4.", null, ""));
        pages.add(new PageContenu("De la même manière, pour un '12x' dans une grille 4x4 avec 2 cases, les seuls chiffres possibles sont 3 et 4 !", null, ""));
        chargerSection("Techniques : Déductions simples", pages);
    }

    @FXML
    private void afficherTechnique3() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu("Contenu de la technique 4 à remplir...", null, ""));
        chargerSection("Technique 4", pages);
    }

    @FXML
    private void afficherTechnique4() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu("Contenu de la technique 5 à remplir...", null, ""));
        chargerSection("Technique 5", pages);
    }

    @FXML
    private void onRetourClick() {
        MainApp.changerScene("/fxml/menu.fxml");
    }
}