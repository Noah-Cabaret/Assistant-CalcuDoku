package fr.univ.calcudoku.model;

import java.util.List;
import java.util.Map;

public class Indice {
    private final String nomTechnique; 
    
    private final String messageExplicatif; 
    
    private final List<Case> casesASurbriller; 
    
    private final Map<Case, Integer> solutions; 

    public Indice(String nomTechnique, String messageExplicatif, List<Case> casesASurbriller, Map<Case, Integer> solutions) {
        this.nomTechnique = nomTechnique;
        this.messageExplicatif = messageExplicatif;
        this.casesASurbriller = casesASurbriller;
        this.solutions = solutions;
    }

    public String getNomTechnique() { 
        return nomTechnique; 
    }
    
    public String getMessageExplicatif() { 
        return messageExplicatif; 
    }
    
    public List<Case> getCasesASurbriller() { 
        return casesASurbriller; 
    }
    
    public Map<Case, Integer> getSolutions() { 
        return solutions; 
    }
}