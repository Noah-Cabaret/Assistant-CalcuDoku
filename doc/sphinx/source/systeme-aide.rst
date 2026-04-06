***************
Système d'aide
***************

L'Assistant-CalcuDoku intègre plusieurs outils d'aide à la résolution.

Aide au calcul
==============

Un outil d'aide au calcul est disponible pendant la partie. Deux modes sont proposés (configurable dans les paramètres du profil) :

Combinaisons
------------

Mode par défaut. Lorsque vous sélectionnez une case, l'assistant affiche toutes les **combinaisons de chiffres valides** pour le bloc correspondant.

*Exemple :* pour un bloc de 2 cases marqué « 12× », l'aide affiche :

.. code-block:: text

   12× : [3, 4] | [2, 6]

Cela signifie que les deux chiffres du bloc sont soit 3 et 4, soit 2 et 6.

Calculatrice
------------

Mode alternatif. Affiche une calculatrice basique (+, −, ×, ÷) intégrée à l'interface, utile pour vérifier vos calculs manuellement.

Aide intelligente
=================

Le bouton **"?"** (aide) fournit des indices progressifs basés sur l'état actuel de la grille.

**Fonctionnement :**

1. Cliquez sur le bouton d'aide : l'assistant analyse la grille et propose un indice.
2. Utilisez les flèches **◀ / ▶** pour naviguer entre les différents indices disponibles.
3. Utilisez le bouton **"+"** pour obtenir un indice plus détaillé.
4. Chaque indice est lié à une **technique de résolution** ; un lien permet de consulter la documentation de la technique correspondante.

.. note::
   Chaque utilisation de l'aide entraîne une pénalité de 50 points sur le score final.

.. warning::
   En mode **Sans aide** (aventure), cette fonctionnalité est désactivée.

Techniques de résolution
========================

L'application documente 8 techniques de résolution, consultables depuis le menu « Règles et Techniques » :

.. list-table::
   :header-rows: 1
   :widths: 5 25 70

   * - #
     - Technique
     - Description
   * - 0
     - La base
     - Remplir la dernière case vide d'une ligne, colonne ou bloc.
   * - 1
     - Bloc unique
     - Identifier la seule combinaison possible pour un bloc.
   * - 2
     - Candidat unique
     - Un chiffre ne peut aller qu'à un seul endroit dans une ligne ou colonne.
   * - 3
     - Reste grille
     - Éliminer des candidats par déduction sur les lignes et colonnes.
   * - 4
     - Intra-bloc
     - Éliminer des candidats au sein d'un bloc par analyse des interactions ligne/colonne.
   * - 5
     - Verrouillage
     - Quand un chiffre est contraint à une seule ligne/colonne dans un bloc, l'éliminer ailleurs.
   * - 6
     - Incontournable
     - Identifier un chiffre présent dans toutes les combinaisons possibles d'un bloc.
   * - 7
     - Unique caché
     - Trouver un chiffre qui n'apparaît qu'une seule fois parmi les candidats d'une ligne/colonne.

Chaque technique est illustrée par des exemples visuels dans l'application.
