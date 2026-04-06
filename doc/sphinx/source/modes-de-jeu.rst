**************
Modes de jeu
**************

L'application propose deux modes de jeu, accessibles depuis le menu principal.

Mode Libre
==========

Le mode Libre permet de jouer des grilles à votre rythme, sans contrainte particulière.

**Configuration d'une partie :**

1. **Taille de la grille** : choisissez parmi 5×5, 6×6, 7×7, 8×8 ou 9×9.
2. **Difficulté** : trois niveaux disponibles.

   - **Facile** (1) — blocs simples, opérations élémentaires
   - **Moyenne** (2) — multiplicateur de score ×1.5
   - **Difficile** (3) — multiplicateur de score ×2.0

3. **Variante** : 3 grilles différentes par combinaison taille/difficulté (45 grilles au total).

Cliquez sur une carte de grille pour lancer la partie. Si une sauvegarde existe, la partie reprendra là où vous l'aviez laissée.

Mode Aventure
=============

Le mode Aventure propose une progression linéaire à travers **5 niveaux** de difficulté croissante.

Chaque niveau impose un **défi** obligatoire :

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - Défi
     - Description
   * - **Survie**
     - Nombre de vies limité. Chaque erreur lors d'une vérification coûte une vie. La partie est perdue si toutes les vies sont épuisées.
   * - **Chrono**
     - Temps limité pour résoudre la grille. La partie est perdue si le temps s'écoule.
   * - **Sans aide**
     - Les fonctionnalités d'aide sont désactivées. Vous devez résoudre la grille par vous-même.

**Progression :**

- Résolvez le niveau actuel pour débloquer le suivant.
- Le bouton **Réinitialiser** permet de repartir au niveau 1 (confirmation demandée).

Système de score
================

Un score est calculé à la fin de chaque partie victorieuse :

.. code-block:: text

   Base       = (taille de la grille)² × 100
   Pénalités  = min(secondes×2, base×0.5) + erreurs×50 + aides×50
   Score      = max(100, base − pénalités) × multiplicateur de difficulté

**Multiplicateurs de difficulté :**

- Facile : ×1.0
- Moyenne : ×1.5
- Difficile : ×2.0

Les meilleurs scores sont enregistrés dans le tableau des **records**, partagé entre tous les profils.

Sauvegarde automatique
======================

Vos parties sont **sauvegardées automatiquement** lorsque vous :

- Retournez au menu
- Fermez la fenêtre de jeu

La sauvegarde conserve l'état complet de la partie : valeurs placées, annotations, hypothèses, chronomètre, historique d'actions, et un aperçu visuel de la grille.

Pour reprendre une partie sauvegardée, cliquez simplement sur sa carte dans le menu du mode correspondant, ou depuis la section « Parties en cours » de votre profil.
