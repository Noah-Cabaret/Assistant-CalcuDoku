package fr.univ.calcudoku.model;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Représente un indice fourni au joueur pour l'aider à résoudre la grille.
 * Un indice peut désormais contenir une progression de messages d'aide.
 */
public class Indice {
    private final String nomTechnique;
    /** Liste des messages explicatifs progressifs (du plus flou au plus précis) */
    private final List<String> messagesExplicatifs;
    private final List<Case> casesASurbriller;
    private final Map<Case, Integer> solutions;
    private final boolean contientErreur;
    
    private String niveauAide;

    /**
     * Nouveau constructeur permettant de fournir plusieurs niveaux de messages d'aide.
     * * @param nomTechnique        Le nom de la technique utilisée
     * @param messagesExplicatifs Liste de messages allant du moins précis au plus précis
     * @param casesASurbriller    Liste des cases concernées (généralement surbrillées au dernier message)
     * @param solutions           Solutions éventuelles liées à cet indice
     * @param contientErreur      Vrai si l'indice signale une erreur du joueur
     */
    public Indice(String nomTechnique, List<String> messagesExplicatifs, List<Case> casesASurbriller, Map<Case, Integer> solutions, boolean contientErreur) {
        this.nomTechnique = nomTechnique;
        this.messagesExplicatifs = messagesExplicatifs;
        this.casesASurbriller = casesASurbriller;
        this.solutions = solutions;
        this.contientErreur = contientErreur;
    }

    /**
     * Ancien constructeur (rétrocompatibilité). Transforme le message unique en une liste à un élément.
     */
    public Indice(String nomTechnique, String messageExplicatif, List<Case> casesASurbriller, Map<Case, Integer> solutions, boolean contientErreur) {
        this(nomTechnique, Arrays.asList(messageExplicatif), casesASurbriller, solutions, contientErreur);
    }

    /**
     * Ancien constructeur court (rétrocompatibilité).
     */
    public Indice(String nomTechnique, String messageExplicatif, List<Case> casesASurbriller, Map<Case, Integer> solutions) {
        this(nomTechnique, messageExplicatif, casesASurbriller, solutions, false);
    }
    
    /**
     * Nouveau constructeur court pour les messages progressifs (sans erreur par défaut).
     */
    public Indice(String nomTechnique, List<String> messagesExplicatifs, List<Case> casesASurbriller, Map<Case, Integer> solutions) {
        this(nomTechnique, messagesExplicatifs, casesASurbriller, solutions, false);
    }

    public String getNomTechnique() {
        return nomTechnique;
    }

    /**
     * Récupère la liste complète des messages progressifs.
     * @return La liste des messages d'aide.
     */
    public List<String> getMessagesExplicatifs() {
        return messagesExplicatifs;
    }

    /**
     * Rétrocompatibilité : retourne le dernier message explicatif de la liste (le plus précis).
     * @return Le message final.
     */
    public String getMessageExplicatif() {
        return (messagesExplicatifs != null && !messagesExplicatifs.isEmpty()) 
               ? messagesExplicatifs.get(messagesExplicatifs.size() - 1) 
               : "";
    }

    public List<Case> getCasesASurbriller() {
        return casesASurbriller;
    }

    public Map<Case, Integer> getSolutions() {
        return solutions;
    }

    public boolean aUneErreur() {
        return contientErreur;
    }

    public void setNiveauAide(String niveauAide) {
        this.niveauAide = niveauAide;
    }

    public String getNiveauAide() {
        return niveauAide;
    }
}