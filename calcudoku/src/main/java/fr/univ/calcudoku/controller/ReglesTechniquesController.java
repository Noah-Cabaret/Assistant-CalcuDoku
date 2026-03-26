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

        // --- FORCER LA COULEUR DU TEXTE ---
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

    // --- DÉFINITION DES ONGLETS ---

    @FXML
    private void afficherRegles() {
        List<PageContenu> pages = new ArrayList<>();
        
        // PAGE 1
        pages.add(new PageContenu(
            "Le Calcudoku est une grille mathématique.\n\nRègle 1 : Remplir la grille avec les chiffres de 1 à N (N étant la taille de la grille).", 
            "/grilles/images/regle1.png", 
            "Exemple de grille 4x4 basique"
        ));
        
        // PAGE 2
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
    private void afficherBlocsUniques() {
        List<PageContenu> pages = new ArrayList<>();
        
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique (Addition)\n\n" +
            " Vous rencontrez un bloc avec une opération dont la solution est mathématiquement unique.\n" +
            " - L'Application : Par exemple, un bloc de deux cases avec la somme '4' ne peut contenir que la combinaison 1+3. Même si vous ne connaissez pas encore l'ordre, vous pouvez annoter ces deux cases avec un '1' et un '3'.", 
            "/grilles/images/blocUnique1.png", // 
            "Déduction d'une addition unique"
        ));
        
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique (Multiplication & Division)\n\n" +
            " La même logique s'applique aux autres opérations.\n" +
            " -L'Application : Un bloc de deux cases avec un produit de '2' ne peut être que 1x2. Pour un bloc de trois cases avec un produit de '6', la seule combinaison autorisée est 1x2x3. Pour une division '2÷', les seules combinaisons sont 2÷1 ou 4÷2. Notez ces combinaisons dans vos brouillons !", 
            "/grilles/images/blocUnique2.png", // 
            "Déduction des produits et quotients"
        ));

        chargerSection("Techniques : Blocs Uniques", pages);
    }

    @FXML
    private void afficherCandidatUnique() {
        List<PageContenu> pages = new ArrayList<>();
        
        pages.add(new PageContenu(
            "Technique : Le Candidat Unique\n\n" +
            "- La Situation : Une ligne ou une colonne est presque pleine.\n" +
            "- L'Application : Selon les règles du jeu, un nombre n'apparaît qu'une seule fois par ligne et colonne. Si les chiffres 1, 2, 4 et 5 sont déjà placés dans la ligne ou la colonne croisant votre case, le seul candidat restant est obligatoirement le 3.", 
            "/grilles/images/candidat1.png", // 
            "Élimination croisée classique"
        ));

        chargerSection("Technique : Candidat Unique", pages);
    }

    @FXML
    private void afficherResteGrille() {
        List<PageContenu> pages = new ArrayList<>();
        
        pages.add(new PageContenu(
            "Technique : Le Reste de Grille (Le Principe)\n\n" +
            "- La Situation : Vous devez trouver la valeur d'une case isolée.\n" +
            "- L'Application : La somme totale d'une ligne ou colonne est toujours identique. Dans une grille 5x5, la somme d'une ligne est toujours 15 (1+2+3+4+5). De même, le produit total d'une ligne 5x5 est toujours 120 (1x2x3x4x5).", 
            "/grilles/images/rest1.png", 
            "La constante mathématique des lignes"
        ));
        
        pages.add(new PageContenu(
            "Technique : La case qui rentre\n\n" +
            "- La Situation : Une colonne contient plusieurs blocs complets, et UNE seule case vide appartenant à un bloc voisin.\n" +
            "- L'Application : Additionnez les blocs complets. Si deux blocs totalisent 10 (ex: 2 + 8), et que la colonne doit faire 15 au total, la différence (15 - 10 = 5) correspond obligatoirement à la case vide restante.", 
            "/grilles/images/rest1.png", // 
            "Soustraction sur une colonne"
        ));

        pages.add(new PageContenu(
            "Technique : La case qui déborde (Multiplication)\n\n" +
            "- La Situation : Deux blocs multiplicatifs sont sur une colonne, mais l'un d'eux déborde d'une case sur la colonne voisine.\n" +
            "- L'Application : Multipliez les valeurs des blocs (ex: 8 x 60 = 480). Puisque la colonne doit valoir 120 au total, le résultat excédentaire divisé par le total (480 ÷ 120 = 4) vous donne la valeur exacte de la case qui a débordé !", 
            "/grilles/images/rest2.png", // 
            "Division de l'excédent multiplicatif"
        ));

        chargerSection("Technique : Reste de Grille", pages);
    }

    @FXML
    private void afficherIntraBloc() {
        List<PageContenu> pages = new ArrayList<>();
        
        pages.add(new PageContenu(
            "Technique : L'Intra Bloc et les Diagonales\n\n" +
            "- La Situation : Vous faites face à un bloc en forme de 'L' de 3 cases.\n" +
            "- L'Application : Si ce bloc a une somme de 14 (dans une grille 5x5), la seule combinaison possible est 4+5+5. En plus de connaître les chiffres, vous connaissez leur position : les deux '5' doivent obligatoirement être placés en diagonale pour ne pas enfreindre la règle d'unicité sur les lignes/colonnes !", 
            "/grilles/images/intra1.png", // 
            "Placement diagonal forcé"
        ));

        pages.add(new PageContenu(
            "Technique : Le bloc saturé\n\n" +
            "- La Situation : Vous avez un bloc de 4 cases (somme 7), mais 3 de ses cases sont alignées sur la même colonne.\n" +
            "- L'Application : La somme minimale pour 3 cases dans une colonne est 1+2+3 = 6. Puisque la somme totale du bloc est 7, il est mathématiquement impossible de faire plus que 6 dans la colonne. La 4ème case isolée vaut donc obligatoirement 1 (7 - 6).", 
            "/grilles/images/intra2.png", // 
            "Déduction par somme minimale"
        ));

        chargerSection("Technique : Intra Bloc", pages);
    }

    @FXML
    private void onRetourClick() {
        MainApp.changerScene("/fxml/menu.fxml");
    }
}