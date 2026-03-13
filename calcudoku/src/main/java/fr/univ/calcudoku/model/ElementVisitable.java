package fr.univ.calcudoku.model;

import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

public interface ElementVisitable {
    void accepter(VisiteurGrille visiteur); // Par défaut public et abstract dans une interface
}