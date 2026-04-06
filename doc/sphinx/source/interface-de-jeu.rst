******************
Interface de jeu
******************

L'écran de jeu est composé de plusieurs zones.

La grille
=========

La grille occupe la partie centrale de l'écran. Chaque case peut contenir :

- Un **chiffre** (valeur placée par le joueur)
- Des **annotations** (candidats possibles, affichés en petit)
- Une valeur en **mode hypothèse** (affichée différemment)

**Sélection :** Cliquez sur une case pour la sélectionner. La ligne, la colonne et les cases contenant le même chiffre sont mises en surbrillance.

**Blocs :** Chaque bloc est délimité par des bordures épaisses. L'indication (résultat et opération) est affichée dans le coin supérieur gauche de la première case du bloc.

Placer un chiffre
=================

1. Sélectionnez une case en cliquant dessus.
2. Cliquez sur un chiffre dans la barre de nombres en bas de l'écran, ou appuyez sur la touche correspondante du clavier.
3. Pour effacer un chiffre, appuyez sur **Retour arrière (⟵)** ou cliquez de nouveau sur le même chiffre.

Mode annotation
===============

Le mode annotation permet de noter les candidats possibles dans une case sans placer de valeur définitive.

- Appuyez sur **A** ou cliquez sur le bouton **Annotation** pour basculer en mode annotation.
- En mode annotation, les chiffres saisis sont ajoutés comme petites notes dans la case.
- Cliquez de nouveau sur un chiffre déjà annoté pour le retirer.
- Lorsqu'un chiffre est placé définitivement dans une case, les annotations correspondantes sont automatiquement retirées des autres cases de la même ligne et colonne.

Mode hypothèse
==============

Le mode hypothèse permet de tester une piste sans engager votre progression :

1. Activez le mode hypothèse via le bouton dédié.
2. Les valeurs placées en hypothèse sont visuellement distinctes.
3. Deux choix s'offrent à vous :

   - **Valider** : les hypothèses deviennent des valeurs définitives.
   - **Annuler** : toutes les hypothèses sont effacées et la grille revient à son état précédent.

Annuler / Rétablir
===================

- **Annuler** (↶) : revient en arrière d'une action.
- **Rétablir** (↷) : rétablit l'action annulée.

L'historique complet est conservé, y compris les annotations et hypothèses.

Vérification
============

Le bouton **Vérifier** contrôle les chiffres placés :

- Les cases **correctes** restent inchangées.
- Les cases **incorrectes** sont brièvement mises en rouge (3 secondes).
- Chaque vérification avec erreurs entraîne une pénalité sur le score.

.. warning::
   En mode **Survie** (aventure), chaque vérification avec erreurs coûte une vie.

Menu de partie
==============

Le menu déroulant en haut de l'écran donne accès à :

- **Recommencer** : remet la grille à zéro (chrono et pénalités réinitialisés).
- **Retour au menu** : sauvegarde et retourne au menu principal.
- **Abandonner** : abandonne la partie (compte comme une défaite, supprime la sauvegarde).

Chronomètre
============

Le chronomètre en haut de l'écran indique le temps écoulé depuis le début de la partie. En mode **Chrono** (aventure), il affiche le temps restant et un décompte.
