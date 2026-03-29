package fr.univ.calcudoku.utils;

/**
 * Dictionnaire centralisé de toutes les constantes (chaînes de caractères) du jeu.
 * Évite les "Magic Strings" éparpillées dans le code.
 */
public class Constantes {

    // FXML
    public static final String VUE_ACCUEIL = "/fxml/accueil.fxml";
    public static final String VUE_MENU = "/fxml/menu.fxml";
    public static final String VUE_MENU_LIBRE = "/fxml/menu_libre.fxml";
    public static final String VUE_MENU_AVENTURE = "/fxml/menu_aventure.fxml";
    public static final String VUE_PROFIL = "/fxml/profil.fxml";
    public static final String VUE_REGLES = "/fxml/reglesTechniques.fxml";
    public static final String VUE_PARTIE = "/fxml/partie.fxml";
    public static final String VUE_CALCULATRICE = "/fxml/VueCalculatrice.fxml";



    // Chemins
    public static final String DOSSIER_PROFILS = "profils/";
    public static final String SOUS_DOSSIER_PARTIES = "/parties/";
    public static final String SOUS_DOSSIER_AVENTURE = "aventure/";
    public static final String SOUS_DOSSIER_IMAGES = "/jeu/images/";

    // Clés du dictionnaire de statistiques (statistiques.ini / options.ini)
    public static final String STAT_PARTIES_JOUEES = "parties_jouees";
    public static final String STAT_VICTOIRES = "victoires";
    public static final String STAT_TEMPS_MOYEN = "temps_moyen";
    public static final String STAT_RATIO = "ratio_parties";
    public static final String STAT_PROGRESSION = "progression";
    public static final String STAT_DIFF_MAX = "difficulte_max";
    public static final String STAT_SCORE_MAX = "score_max";
    
    // Clés des options
    public static final String OPTION_MODE_SOMBRE = "mode_sombre";
    public static final String OPTION_AIDE_CALCUL = "aide_calcul";
    public static final String VALEUR_AIDE_CALCULATRICE = "calculatrice";
    public static final String VALEUR_AIDE_COMBINAISONS = "combinaisons";

    // DESIGN ET CSS 
    public static final String CSS_CASE_SELECTIONNEE = "case-selectionnee";
    public static final String CSS_CASE_ERREUR = "case-erreur";
    
    // Couleurs et Icônes des Popups
    public static final String ICONE_VICTOIRE = "fas-trophy";
    public static final String COULEUR_VICTOIRE = "#f1c40f"; // Jaune/Or
    public static final String ICONE_DEFAITE = "fas-times-circle";
    public static final String COULEUR_DEFAITE = "#e74c3c"; // Rouge
}