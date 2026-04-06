package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.Constantes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour la vue des règles et techniques.
 * Gère l'affichage de contenu paginé dans différentes sections (onglets).
 */
public class ReglesTechniquesController {

    /** Groupe de bascule pour les onglets de section. */
    @FXML private ToggleGroup groupeOnglets;
    /** Bouton pour retourner à l'écran précédent. */
    @FXML private Button btnRetour;
    /** Label pour le titre de la section. */
    @FXML private Label lblTitreHaut;
    /** Label pour afficher le contenu textuel de la page. */
    @FXML private Label lblTexte;
    /** Vue pour afficher l'image d'exemple. */
    @FXML private ImageView imgExemple;
    /** Label pour la légende de l'image. */
    @FXML private Label lblLegendeImage;
    
    /** Bouton pour la pagination vers la page précédente. */
    @FXML private Button btnPrecedent;
    /** Bouton pour la pagination vers la page suivante. */
    @FXML private Button btnSuivant;
    /** Icône pour le bouton de pagination précédent. */
    @FXML private FontIcon iconPrecedent;
    /** Icône pour le bouton de pagination suivant. */
    @FXML private FontIcon iconSuivant;
    /**
     * Action personnalisée à exécuter lors du clic sur le bouton retour.
     * Permet de revenir à une partie en cours. Si null, le retour se fait vers le menu.
     */
    public static Runnable actionRetour = null;
    public static String techniqueCiblee = null;

    /** Liste des pages pour la section actuellement affichée. */
    private List<PageContenu> pagesDeLaSection = new ArrayList<>();
    /** Index de la page courante dans la liste {@link #pagesDeLaSection}. */
    private int indexPageActuelle = 0;

    /** Chemin FXML de la page précédente (utilisé si {@link #actionRetour} est null). */
    public static String pagePrecedente = Constantes.VUE_MENU;

    /**
     * Classe interne représentant le contenu d'une seule page dans la section des règles/techniques.
     */
    private class PageContenu {
        /** Le contenu textuel principal de la page. */
        String texte;
        /** Le chemin vers l'image d'illustration dans les ressources. */
        String cheminImage;
        /** La légende de l'image. */
        String legende;

        /**
         * Constructeur pour une page de contenu.
         * @param texte Le texte à afficher.
         * @param cheminImage Le chemin de l'image.
         * @param legende La légende de l'image.
         */
        PageContenu(String texte, String cheminImage, String legende) {
            this.texte = texte;
            this.cheminImage = cheminImage;
            this.legende = legende;
        }
    }

    /**
     * Méthode d'initialisation appelée après le chargement du FXML.
     * Configure les listeners pour les onglets, applique le thème (sombre/clair)
     * et charge la première section par défaut (les règles).
     */
    @FXML
    public void initialize() {
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

        if (MainApp.isModeSombre()) {
            lblTitreHaut.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #3b3b3b; -fx-background-radius: 10; -fx-border-color: #999999; -fx-border-radius: 10; -fx-padding: 10 30 10 30;");
            
            lblTexte.setStyle("-fx-font-size: 16px; -fx-line-spacing: 5px; -fx-text-fill: white;");
            lblLegendeImage.setStyle("-fx-font-size: 12px; -fx-text-fill: lightgray;");
            btnPrecedent.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnSuivant.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            
            if (iconPrecedent != null) iconPrecedent.setIconColor(Color.WHITE);
            if (iconSuivant != null) iconSuivant.setIconColor(Color.WHITE);
            if (btnRetour != null && btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(Color.WHITE);
            }
        } else {
            // TITRE : On ajoute un fond clair et on arrondit le fond
            lblTitreHaut.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black; -fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-border-color: #999999; -fx-border-radius: 10; -fx-padding: 10 30 10 30;");
            
            lblTexte.setStyle("-fx-font-size: 16px; -fx-line-spacing: 5px; -fx-text-fill: black;");
            lblLegendeImage.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
            btnPrecedent.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnSuivant.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            
            if (iconPrecedent != null) iconPrecedent.setIconColor(Color.BLACK);
            if (iconSuivant != null) iconSuivant.setIconColor(Color.BLACK);
            if (btnRetour != null && btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(Color.BLACK);
            }
        }

        // Redirection intelligente si on vient de la bulle d'aide
        if (techniqueCiblee != null) {
            afficherTechniqueParNom(techniqueCiblee);
            techniqueCiblee = null;
        } else {
            afficherRegles();
        }
    }

    /**
     * Gère le clic sur le bouton 'Précédent'. Affiche la page précédente de la section.
     */
    @FXML
    private void onPagePrecedente() {
        if (indexPageActuelle > 0) {
            indexPageActuelle--;
            afficherPageCourante();
        }
    }
    private void afficherTechniqueParNom(String nom) {
        if (nom == null) nom = "";
        String n = nom.toLowerCase();
        if (n.contains("vérification") || n.contains("bloc unique") || n.contains("combinaison")) { selectionnerBouton("Techniques 1"); afficherBlocsUniques(); }
        else if (n.contains("candidat") || n.contains("place unique")) { selectionnerBouton("Techniques 2"); afficherCandidatUnique(); }
        else if (n.contains("reste")) { selectionnerBouton("Techniques 3"); afficherResteGrille(); }
        else if (n.contains("intra")) { selectionnerBouton("Techniques 4"); afficherIntraBloc(); }
        else if (n.contains("verrouillage")) { selectionnerBouton("Techniques 5"); afficherVerrouillage(); }
        else if (n.contains("incontournable")) { selectionnerBouton("Techniques 6"); afficherIncontournable(); }
        else if (n.contains("caché") || n.contains("cache")) { selectionnerBouton("Techniques 7"); afficherUniqueCache(); }
        else if (n.contains("base") || n.contains("case unique") || n.contains("dernière case")) { selectionnerBouton("Techniques 0"); afficherBase(); }
        else { afficherRegles(); }
    }

    private void selectionnerBouton(String debutTexte) {
        for (javafx.scene.control.Toggle t : groupeOnglets.getToggles()) {
            ToggleButton btn = (ToggleButton) t;
            if (btn.getText().startsWith(debutTexte)) { 
                btn.setSelected(true); 
                break; 
            }
        }
    }


    /**
     * Gère le clic sur le bouton 'Suivant'. Affiche la page suivante de la section.
     */
    @FXML
    private void onPageSuivante() {
        if (indexPageActuelle < pagesDeLaSection.size() - 1) {
            indexPageActuelle++;
            afficherPageCourante();
        }
    }

    private void chargerSection(String titre, List<PageContenu> pages) {
        lblTitreHaut.setText(titre);
        this.pagesDeLaSection = pages;
        this.indexPageActuelle = 0; 
        afficherPageCourante();
    }

    /**
     * Affiche le contenu de la page actuelle (texte, image, légende) et met à jour
     * la visibilité des boutons de pagination.
     */
    private void afficherPageCourante() {
        if (pagesDeLaSection.isEmpty()) return;
        PageContenu page = pagesDeLaSection.get(indexPageActuelle);
        lblTexte.setText(page.texte);
        lblLegendeImage.setText(page.legende);
        if (page.cheminImage != null && !page.cheminImage.isEmpty()) {
            imgExemple.setImage(CacheRessources.getImage(page.cheminImage));
            imgExemple.setVisible(true);
        } else {
            imgExemple.setImage(null);
            imgExemple.setVisible(false);
        }
        btnPrecedent.setVisible(indexPageActuelle > 0);
        btnSuivant.setVisible(indexPageActuelle < pagesDeLaSection.size() - 1);
    }

    @FXML
    private void afficherRegles() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Le Calcudoku est une grille mathématique.\n\nRègle 1 : Remplir la grille avec les chiffres de 1 à N (N étant la taille de la grille).", 
            "/grilles/images/regle1.png", 
            "Exemple de grille 4x4 basique"
        ));
        pages.add(new PageContenu(
            "Règle 2 : Chaque chiffre ne doit apparaître qu'une seule fois par ligne et par colonne.\nMais un nombre peut être utilisé plus d'une fois dans le même bloc", 
            "/grilles/images/regle2.png", 
            "les chiffres ne sont pas identiques"
        ));
        pages.add(new PageContenu(
            "Règle 3 : Les cages (blocs entourés en gras) doivent respecter l'opération mathématique indiquée en haut à gauche pour obtenir le résultat cible.", 
            "/grilles/images/regle3.png", 
            "Attention à l'opération (+, -, *, /)"
        ));
        chargerSection("Règles du Calcudoku", pages);
    }

    /**
     * Charge le contenu de la section "Fonctionnalités et Outils".
     */
    @FXML
    private void afficherFonctionnalites() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Contrôles de base\n\n" +
            "Vous pouvez remplir la grille avec la souris et le clavier :\n\n" +
            "- Déplacement : cliquez sur une case ou utilisez les flèches ↑ ↓ ← →\n" +
            "- Placer un chiffre : cliquez sur le bouton correspondant ou appuyez sur la touche numérique (1-9)\n" +
            "- Effacer : appuyez sur la touche Retour arrière (⟵)\n" +
            "- La case sélectionnée met en surbrillance sa ligne, sa colonne et toutes les cases contenant le même chiffre.",
            "/grilles/images/touches.png",
            "Contrôles du jeu"
        ));
        pages.add(new PageContenu(
            "Mode Annotation\n\n" +
            "Le mode annotation permet de noter les candidats possibles dans une case sans placer de valeur définitive.\n\n" +
            "- Appuyez sur la touche 'A' ou cliquez sur le bouton Annotation pour activer/désactiver ce mode.\n" +
            "- En mode annotation, les chiffres saisis apparaissent en petit dans la case.\n" +
            "- Cliquez à nouveau sur un chiffre annoté pour le retirer.\n" +
            "- Lorsqu'un chiffre est placé définitivement, ses annotations sont automatiquement retirées de la même ligne et colonne.",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Mode Hypothèse\n\n" +
            "Le mode hypothèse permet de tester une piste sans perdre votre progression actuelle.\n\n" +
            "- Activez le mode hypothèse via le bouton dédié.\n" +
            "- Les valeurs placées en hypothèse sont visuellement distinctes (couleur différente).\n" +
            "- Valider : transforme toutes les hypothèses en valeurs définitives.\n" +
            "- Annuler : efface toutes les hypothèses et restaure la grille à son état précédent.\n\n" +
            "C'est idéal pour explorer une piste incertaine sans risque !",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Vérification\n\n" +
            "Le bouton Vérifier contrôle les chiffres que vous avez placés :\n\n" +
            "- Les cases correctes restent inchangées.\n" +
            "- Les cases incorrectes sont brièvement mises en rouge pendant 3 secondes.\n" +
            "- Chaque vérification avec des erreurs entraîne une pénalité de 50 points sur le score.\n\n" +
            "⚠ En mode Survie (aventure), chaque vérification avec erreurs coûte une vie !",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Annuler / Rétablir\n\n" +
            "L'application conserve un historique complet de vos actions :\n\n" +
            "- Annuler (↶) : revient en arrière d'une action.\n" +
            "- Rétablir (↷) : rétablit l'action annulée.\n\n" +
            "L'historique inclut les placements de chiffres, les annotations et les hypothèses. Il est conservé même après une sauvegarde.",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Aide au calcul\n\n" +
            "Deux modes d'aide au calcul sont disponibles (configurable dans votre Profil) :\n\n" +
            "- Combinaisons : affiche toutes les combinaisons de chiffres valides pour le bloc sélectionné.\n" +
            "  Exemple : pour un bloc « 12× » → [3, 4] | [2, 6]\n\n" +
            "- Calculatrice : ouvre une calculatrice intégrée (+, −, ×, ÷) pour vos propres calculs.\n\n" +
            "Vous pouvez changer de mode depuis le menu déroulant en partie.",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Aide intelligente (bouton ?)\n\n" +
            "Le bouton d'aide analyse la grille et propose des indices progressifs :\n\n" +
            "- Cliquez sur '?' : l'assistant détecte une déduction possible et vous donne un indice.\n" +
            "- Flèches ◀ / ▶ : naviguez entre les différents indices disponibles.\n" +
            "- Bouton '+' : obtenez un indice plus détaillé sur la même déduction.\n" +
            "- Chaque indice est lié à une technique de résolution ; un lien permet de la consulter.\n\n" +
            "⚠ Chaque utilisation coûte 50 points de pénalité sur le score.\n" +
            "⚠ Désactivé en mode Sans Aide (aventure).",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Menu de partie\n\n" +
            "Le menu déroulant en haut de l'écran donne accès à :\n\n" +
            "- Abandonner : abandonne la partie (compte comme défaite, supprime la sauvegarde).\n" +
            "- Recommencer : remet la grille à zéro avec le même puzzle.\n" +
            "- Changer le mode d'aide au calcul.\n" +
            "- Retour à Règles & Techniques : consulter ce guide depuis la partie.\n" +
            "- Retour au menu : sauvegarde automatiquement et retourne au menu principal.",
            "/grilles/images/menu.png",
            "Menu déroulant"
        ));
        pages.add(new PageContenu(
            "Sauvegarde automatique\n\n" +
            "Vos parties sont sauvegardées automatiquement lorsque vous :\n\n" +
            "- Retournez au menu principal.\n" +
            "- Fermez la fenêtre de l'application.\n\n" +
            "La sauvegarde conserve l'état complet : valeurs, annotations, hypothèses, chronomètre, historique d'actions, et un aperçu visuel de la grille.\n\n" +
            "Pour reprendre, cliquez sur la carte de la partie dans le menu ou depuis votre Profil.",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Système de score\n\n" +
            "Un score est calculé à chaque victoire :\n\n" +
            "- Score de base = (taille de la grille)² × 100\n" +
            "- Pénalités : temps écoulé, erreurs de vérification (×50), aides utilisées (×50)\n" +
            "- Multiplicateur de difficulté : Facile ×1, Moyenne ×1.5, Difficile ×2\n\n" +
            "Les meilleurs scores sont enregistrés dans le tableau des records (🏆), partagé entre tous les profils.",
            null,
            ""
        ));
        pages.add(new PageContenu(
            "Défis du mode Aventure\n\n" +
            "Chaque niveau du mode Aventure impose un défi spécial :\n\n" +
            "- Survie : nombre de vies limité. Chaque vérification avec erreurs coûte une vie.\n" +
            "- Chrono : temps limité pour résoudre la grille.\n" +
            "- Sans aide : le bouton d'aide et les combinaisons sont désactivés.\n\n" +
            "Résolvez le niveau actuel pour débloquer le suivant. Vous pouvez réinitialiser votre progression depuis le menu Aventure.",
            null,
            ""
        ));
        chargerSection("Fonctionnalités et Outils", pages);
    }

    /**
     * Charge le contenu de la section "Techniques de base".
     */
    @FXML
    private void afficherBase() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Certains puzzles CalcuDoku, particulièrement faciles, contiennent des blocs constitués d'un seul carré. Ceux-ci sont en fait donnés des indices et le nombre à placer est simplement le nombre dans le coin supérieur gauche du bloc carré unique, quelles que soient les opérations mathématiques indiquées au coin de cage.",
            "/grilles/images/tech0.png", 
            "une case unique"
        ));
        chargerSection("Techniques de base", pages);
    }

    /**
     * Charge le contenu de la section "Techniques : Blocs Uniques".
     */
    @FXML
    private void afficherBlocsUniques() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 1\n\n" +
            "Dans la colonne de gauche du puzzle SingleOp CalcuDoku ci-dessous, nous voyons un bloc de deux carrés avec la somme 4. Puisque les règles CalcuDoku ne permettent pas au même nombre d’apparaître plus d’une fois dans une ligne ou une colonne, la seule combinaison pour satisfaire cette exigence est 1+3 bien que nous ne sachions pas encore dans quel ordre les nombres sont placés. On peut donc placer 4 dans le carré inférieur de la colonne de gauche, et 3, le reste du bloc, dans le carré adjacent.",
            "/grilles/images/bloc1.png", 
            "Déduction d'une addition unique"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 2\n\n" +
            "Nous pouvons utiliser la même technique pour les blocs avec multiplication. Dans la rangée inférieure de l'exemple ci-dessous se trouve un bloc de deux carrés avec le produit de multiplication 2. Cela signifie que la seule combinaison peut être 1x2 bien que nous ne sachions pas encore dans quel ordre les numéros sont placés. Comme nous en avons également 4 dans la rangée inférieure, le seul candidat pour le carré sur le côté droit de la rangée inférieure est 3. Après avoir placé 3, nous pouvons également placer 1, le reste du bloc, dans le carré au-dessus.",
            "/grilles/images/bloc2.png", 
            "Déduction d'une addition unique"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 3\n\n" +
            "Dans la rangée inférieure de l'exemple ci-dessous, nous voyons un bloc de deux carrés avec la somme 4. Puisque les règles CalcuDoku ne permettent pas au même nombre d’apparaître plus d’une fois dans une ligne ou une colonne, la seule combinaison peut être 1+3. Cependant, le carré supérieur gauche contient déjà 3, ce qui signifie qu'il n'y a qu'un seul moyen de placer 1 et 3 dans la ligne inférieure comme le montre le diagramme à droite.",
            "/grilles/images/bloc3.png", 
            "Déduction d'une addition unique"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 4\n\n" +
            "Cet exemple est similaire à ceux ci-dessus, sauf qu'il y a deux carrés vides en haut de la colonne de gauche et les candidats pour ces carrés sont 4 et 5. Cependant, 5 est trop grand pour être placé dans le bloc haut-gauche, donc 4 reste le seul candidat comme le montre le diagramme de droite ci-dessous. Nous pouvons maintenant placer 1, le reste du bloc, dans le carré adjacent.",
            "/grilles/images/bloc4.png", 
            "Déduction des produits et quotients"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 5\n\n" +
            "Nous pouvons utiliser la même technique pour les blocs avec multiplication. La rangée supérieure de l'exemple ci-dessous contient un bloc de trois carrés avec le produit de multiplication 6. Comme la seule combinaison autorisée est 1x2x3, nous pouvons déduire que 4 et 5 sont dans les deux carrés restants dans la rangée supérieure. Cependant, nous ne pouvons pas placer 5 dans le carré le plus à droite, car 8 ne peut être que le résultat de 2x4. Par conséquent, nous devons placer 4 dans le carré de haut à droite et 2, le reste du bloc, dans la place sous elle.",
            "/grilles/images/bloc5.png", 
            "Déduction des produits et quotients"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 6\n\n" +
            "Vous trouverez ci-dessous un puzzle DualOp CalcuDoku qui utilise des opérations de multiplication et de division. Semblable aux exemples précédents, le seul candidat pour le carré gauche dans la deuxième rangée du puzzle ci-dessous est 3. Après avoir placé 3, nous pouvons également placer 1 dans le carré ci-dessous, car c'est le seul moyen d'obtenir le résultat de division de 3.",
            "/grilles/images/bloc6.png", 
            "Déduction des produits et quotients"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 7\n\n" +
            "Voici un autre exemple d'utilisation de la technique de bloc unique dans un puzzle DualOp CalcuDoku avec multiplication et division. Examinons le bloc avec 2÷ dans la colonne de gauche. Il n'y a que deux combinaisons possibles, 2÷1 et 4÷2 qui font de 1, 2 et 4 les seuls candidats possibles pour ce bloc. Cependant, la quatrième rangée contient déjà 1 et 4 laissant 2 comme seule possibilité pour le carré gauche de la quatrième rangée. Contrairement aux exemples précédents, nous ne pouvons pas encore placer le reste du bloc puisque 1 et 4 sont possibles.",
            "/grilles/images/bloc7.png", 
            "Déduction des produits et quotients"
        ));
        pages.add(new PageContenu(
            "Technique : Le Bloc Unique 8\n\n" +
            "Parfois, il est plus difficile d'identifier des techniques de bloc uniques comme le montre l'exemple ci-dessous. La seule combinaison possible pour le bloc supérieur dans la colonne de droite est 1+2. Cependant, lorsque nous examinons le bloc de trois carrés de la première rangée, nous ne voyons que 2+4+5 est possible, ce qui exclut 2 du carré supérieur dans la colonne de droite et laisse 1 comme son seul candidat. Après avoir placé 1 dans le carré supérieur de la colonne de droite, nous pouvons également placer 2, le reste du bloc, dans le carré en dessous.",
            "/grilles/images/bloc8.png", 
            "Déduction des produits et quotients"
        ));
        chargerSection("Techniques : Blocs Uniques", pages);
    }

    /**
     * Charge le contenu de la section "Technique : Candidat Unique".
     */
    @FXML
    private void afficherCandidatUnique() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Selon la première règle de CalcuDoku, un nombre ne peut apparaître qu'une seule fois dans la même ligne ou colonne, regardons la case avec un cadre rouge dans la grille ci-dessous. Comme 1 et 5 ont déjà été placés dans d'autres carrés de cette rangée, et que 2 et 4 ont déjà été placés dans d'autres carrés dans cette colonne, le seul candidat restant est 3.", 
            "/grilles/images/candidat1.png", 
            "Élimination croisée classique"
        ));
        chargerSection("Technique : Candidat Unique", pages);
    }

    /**
     * Charge le contenu de la section "Technique : Reste de Grille".
     */
    @FXML
    private void afficherResteGrille() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : Le Reste de Grille (Le Principe)\n\n" +
            "- La Situation : Vous devez trouver la valeur d'une case isolée.\n" +
            "- L'Application : La somme (resp le produit) totale d'une ligne (resp colonne) est toujours identique pour chaque ligne (resp colonne) de une grille. Dans la grille 5x5, la somme d'une ligne est toujours 15 (1+2+3+4+5). De même, le produit total d'une ligne 5x5 est toujours 120 (1x2x3x4x5).", 
            "/grilles/images/rest0.png", 
            "Principe"
        ));
        pages.add(new PageContenu(
            "Technique : grille restante 1\n\n" +
            "La rangée supérieure du puzzle ci-dessous a deux blocs entièrement contenus ombragés en gris, l'un avec la somme 2 (qui est en fait une donnée) et l'autre avec la somme 8. Ensemble, ces deux blocs résument jusqu'à 2 + 8 = 10, mais nous savons que n'importe quelle ligne ou toute colonne dans une grille 5x5 doit résumer jusqu'à 1 + 2 + 3 + 4 + 5 = 15 = 15. Cela signifie que la différence 15-10=5 est causée par le carré non ombragé dans la rangée supérieure et nous pouvons donc y placer 5.",
            "/grilles/images/rest1.1.png", 
            "Addition sur une ligne"
        ));
        pages.add(new PageContenu(
            "Technique : grille restante 2\n\n" +
            "Voici un autre exemple avec un twist. La colonne de gauche contient deux blocs ombragés en gris, un avec la somme 10 qui est entièrement contenue dans la colonne de gauche, et un avec la somme 9 qui ne comporte que deux carrés contenus dans la colonne de gauche. Ensemble, ces deux blocs résument jusqu'à 10+9 = 19, mais nous savons que n'importe quelle ligne ou n'importe quelle colonne dans un puzzle CalcuDoku 5x5 doit résumer jusqu'à 1 + 2 + 3 + 4 + 5 = 15. Cela signifie que la différence 19-15 = 4 est causée par le carré dans le bloc inférieur qui n’est pas contenu dans la colonne de gauche et nous pouvons donc y placer 4.",
            "/grilles/images/rest2.1.png", 
            "Le trou du bloc"
        ));
        pages.add(new PageContenu(
            "Technique : grille restante 3\n\n" +
            "La colonne de droite du puzzle ci-dessous contient deux blocs ombragés en gris, un avec le produit 8 qui est entièrement contenu dans la colonne de droite, et un avec le produit 60 qui ne comporte que deux carrés contenus dans la colonne de droite. Ensemble, ces deux blocs ont un produit de 8x60 = 480, mais nous savons que le produit de n'importe quelle ligne ou de n'importe quelle colonne dans un puzzle CalcuDoku 5x5 doit être 1x2x3x4x5 = 120. Cela signifie que le quotient 480÷120=4 est causé par le carré qui n’est pas contenu dans la colonne de droite et nous pouvons donc y placer 4.",
            "/grilles/images/rest3.1.png", 
            "Multiplication sur une colonne"
        ));
        chargerSection("Technique : Reste de Grille", pages);
    }

    /**
     * Charge le contenu de la section "Technique : Intra Bloc".
     */
    @FXML
    private void afficherIntraBloc() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : L'Intra Bloc 1\n\n" +
            "- La Situation : Vous faites face à un bloc en forme de 'L' de 3 cases.\n" +
            "- L'Application : Si ce bloc a une somme de 14 (dans une grille 5x5), la seule combinaison possible est 4+5+5. En plus de connaître les chiffres, vous connaissez leur position : les deux '5' doivent obligatoirement être placés en diagonale pour ne pas enfreindre la règle d'unicité sur les lignes/colonnes !", 
            "/grilles/images/intra1.1.png", 
            "Placement diagonal en L"
        ));
        pages.add(new PageContenu(
            "Technique : L'Intra Bloc 2\n\n" +
            "Dans le puzzle ci-dessous, trois des quatre carrés du bloc ombragé sont contenus dans une colonne. Cela signifie que la seule combinaison possible pour ces trois carrés est 1+2+3=6 car toute autre combinaison atteindra ou dépassera 7, la somme de ce bloc, ne permettant donc aucun nombre dans son quatrième carré. On peut donc placer 1, le reste du bloc calculé de 7-6, dans le quatrième carré.",
            "/grilles/images/intra2.1.png", 
            "Intra 2"
        ));
        pages.add(new PageContenu(
            "Technique : L'Intra Bloc 3\n\n" +
            "Les techniques de bloc intra peuvent être utilisées aussi bien pour les blocs avec multiplication. Dans l'exemple ci-dessous, la seule combinaison pour le produit 80 dans le bloc ombragé est 4x4x5. Cette situation est vraie pour tous les blocs en L de trois carrés d'un puzzle 5x5. En plus de savoir quels nombres doivent être placés, nous savons également où ils devraient être puisque les 4 doivent être en diagonale pour éviter qu’un nombre n’apparaisse dans la même ligne ou la même colonne plus d’une fois.",
            "/grilles/images/intra3.1.png", 
            "Intra 3"
        ));
        pages.add(new PageContenu(
            "Technique : L'Intra Bloc 4\n\n" +
            "Dans les puzzles CalcuDoku 5x5, les seules combinaisons pour le produit 32 en quatre carrés sont 1x2x4x4 et 2x2x2x4. Si nous regardons le bloc en forme de L ombragé en dessous, il est évident que la deuxième combinaison n'est pas possible car peu importe comment nous plaçons les nombres, il y en aura toujours 2 dans la même ligne ou la même colonne. Cela signifie que seul 1x2x4x4 est autorisé et 4 doit être placé dans le carré gauche pour éviter tout conflit avec l'autre 4.",
            "/grilles/images/intra4.1.png", 
            "Intra 4"
        ));
        chargerSection("Technique : Intra Bloc", pages);
    }

    /**
     * Gère le clic sur le bouton de retour. Exécute l'action personnalisée
     * {@link #actionRetour} si elle existe, sinon change la scène vers le menu principal.
     */
    @FXML
    private void afficherVerrouillage() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : Le Verrouillage d'Axe\n\n" +
            "Lorsqu'un bloc aligné sur une seule ligne (ou colonne) ne possède qu'une seule combinaison possible, les chiffres de cette combinaison sont 'verrouillés' sur cet axe.\n\n" +
            "Puisque ces chiffres doivent obligatoirement se trouver dans les cases du bloc, ils ne peuvent apparaître nulle part ailleurs sur la même ligne. Vous pouvez donc effacer ces chiffres des annotations des autres cases de l'axe.",
            "/grilles/images/verrouillage1.png", 
            "Le bloc aligné bloque les chiffres sur toute la ligne"
        ));
        chargerSection("Technique : Verrouillage d'Axe", pages);
    }

    @FXML
    private void afficherIncontournable() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : Le Chiffre Incontournable\n\n" +
            "Parfois, un bloc a plusieurs combinaisons possibles, mais un chiffre précis apparaît dans TOUTES ces options. On dit que ce chiffre est incontournable.\n\n" +
            "Si ce bloc est aligné sur une ligne ou une colonne, ce chiffre obligatoire ne pourra pas être placé ailleurs sur cet axe. C'est une méthode puissante pour nettoyer vos notes même sans connaître la combinaison finale.",
            null,
            "Un chiffre présent dans toutes les combinaisons"
        ));
        chargerSection("Technique : Chiffre Incontournable", pages);
    }

    @FXML
    private void afficherUniqueCache() {
        List<PageContenu> pages = new ArrayList<>();
        pages.add(new PageContenu(
            "Technique : L'Unique Caché\n\n" +
            "Cette technique consiste à regarder une ligne ou une colonne entière. Si vous remarquez qu'un chiffre n'apparaît qu'une seule fois dans les annotations de tout l'axe, alors il va forcément dans cette case.\n\n" +
            "Peu importe si la case contient d'autres notes, le fait que ce soit le seul endroit possible pour ce chiffre sur la ligne (ou la colonne) valide son placement.", 
            "/grilles/images/uniquecache1.png",
            "Un seul emplacement possible sur l'axe"
        ));
        chargerSection("Technique : Unique Caché", pages);
    }

    @FXML
    private void onRetourClick() {
        if (actionRetour != null) {
            Runnable action = actionRetour;
            actionRetour = null;
            action.run();
        } else {
            MainApp.changerScene(Constantes.VUE_MENU);
        }
    }
}