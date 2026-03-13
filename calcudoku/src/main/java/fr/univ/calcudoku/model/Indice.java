package fr.univ.calcudoku.model;

import java.util.List;
import java.util.Map;

/**
 * Représente un indice pour aider le joueur à résoudre un groupement.
 * Contient un message explicatif, des cases à surbriller et les solutions.
 */
public class Indice {
    /** Nom de la technique d'indice utilisée */
    private final String nomTechnique; 
    
    /** Message d'explication pour l'indice */
    private final String messageExplicatif; 
    
    /** Cases à surbriller pour l'indice visuel */
    private final List<Case> casesASurbriller; 
    
    /** Solutions proposées associées à chaque case */
    private final Map<Case, Integer> solutions; 

    /**
     * Constructeur d'un indice complet.
     * @param nomTechnique le nom de la technique
     * @param messageExplicatif le message d'explication
     * @param casesASurbriller les cases à mettre en évidence
     * @param solutions les solutions pratiques
     */
    public Indice(String nomTechnique, String messageExplicatif, List<Case> casesASurbriller, Map<Case, Integer> solutions) {
        this.nomTechnique = nomTechnique;
        this.messageExplicatif = messageExplicatif;
        this.casesASurbriller = casesASurbriller;
        this.solutions = solutions;
    }

    /**
     * Retourne le nom de la technique utilisée.
     * @return le nom technique
     */
    public String getNomTechnique() { 
        return nomTechnique; 
    }
    
    /**
     * Retourne le message d'explication de l'indice.
     * @return le message
     */
    public String getMessageExplicatif() { 
        return messageExplicatif; 
    }
    
    /**
     * Retourne les cases à surbriller.
     * @return la liste des cases
     */
    public List<Case> getCasesASurbriller() { 
        return casesASurbriller; 
    }
    
    /**
     * Retourne les solutions proposées.
     * @return la map case->valeur
     */
    public Map<Case, Integer> getSolutions() { 
        return solutions; 
    }
}