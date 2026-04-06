************
Installation
************

Prérequis
=========

- **Java 17** ou version supérieure (JDK ou JRE)
- Vérifiez votre version avec : ``java -version``

Option 1 : Télécharger le JAR (recommandé)
===========================================

Le fichier ``.jar`` distribuable est fourni avec le rendu du projet. Si nécessaire, il peut être régénéré à partir des sources (voir Option 2).

Pour lancer l'application :

.. code-block:: bash

   java -jar calcudoku-1.0-SNAPSHOT.jar

Ce fichier JAR fonctionne sur **Windows, macOS et Linux** sans installation supplémentaire.

Option 2 : Compiler depuis les sources
=======================================

Si vous souhaitez compiler le projet vous-même :

**Prérequis supplémentaires :**

- `Apache Maven <https://maven.apache.org/>`_ installé
- JDK 17+

**Étapes :**

.. code-block:: bash

   # Cloner le dépôt
   git clone https://github.com/Noah-Cabaret/Assistant-CalcuDoku.git
   cd Assistant-CalcuDoku/calcudoku

   # Construire le fat JAR distribuable
   mvn clean package -Pfatjar

Le JAR généré se trouve dans ``target/calcudoku-1.0-SNAPSHOT.jar``.
