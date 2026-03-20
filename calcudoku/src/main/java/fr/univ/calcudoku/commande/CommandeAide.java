package fr.univ.calcudoku.commande;

/**
 * Interface pour les commandes d'aide accessible par le joueur.
 * Permet d'afficher, masquer et améliorer progressivement les indications.
 */
public interface CommandeAide {
    /**
     * Affiche l'aide actuellement déverrouillée.
     */
    void afficher();

    /**
     * Masque l'aide actuellement affichée.
     */
    void masquer();

    /**
     * Améliore le niveau d'aide en déverrouillant la prochaine étape.
     */
    void ameliorerNiveau();

    /**
     * Vérifie si l'aide peut être améliorée.
     * @return true s'il y a une prochaine étape d'aide
     */
    boolean peutEtreAmeliore();
}