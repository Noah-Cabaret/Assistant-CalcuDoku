package fr.univ.calcudoku.model;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

/**
 * Interface de base pour le pattern Visitor.
 * Permet de visiter les éléments du modèle.
 */


public interface ElementVisitable {
    void accepter(VisiteurGrille visiteur);
}