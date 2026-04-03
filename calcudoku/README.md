# *Assistant CalcuDoku*

L'_Assistant CalcuDoku_ est une application _desktop_ permettant de résoudre des puzzles de [CalcuDoku](https://www.conceptispuzzles.com/index.aspx?uri=puzzle/calcudoku), construite autour d'un système d'assistance s'adaptant à l'état courant d'une partie.

L'application est écrite en [Java](https://www.oracle.com/java/technologies/), utilise [Apache Maven](https://maven.apache.org/) comme système d'automatisation de _build_ et est documentée via [Javadoc](https://www.oracle.com/java/technologies/javase/javadoc.html) (code source) ainsi que [Sphinx](https://www.sphinx-doc.org/) (manuel utilisateur).

## Contexte

Cette application est un projet étudiant et le fruit de travail réalisé dans la cadre d'une unité d'enseignement de *Licence Informatique*. Au sein du groupe de travail (**n°2**), [@Ekamyl](https://github.com/Ekamyl) et [@Noah-Cabaret](https://github.com/Noah-Cabaret) ont successivement assuré le rôle de **chef de projet** ; [@nth-univ](https://github.com/nth-univ) était chargé du rôle de documentaliste.

## Installation/Usage

### Option n°1: _Java Archives_ distribuées sur la plateforme distante.

Afin de pouvoir tourner de manière [_cross-platform_](https://fr.wiktionary.org/wiki/cross-platform) et éviter de rendre la compilation depuis le code source mandatoire, des [_Java Archives_](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jarGuide.html) (fichiers `.jar`) sont distribuées sur GitHub, la plateforme hôte du code source de l'_Assistant CalcuDoku_. Vous pouvez en retrouver la dernière version en vous rendant sur [l'onglet _Releases_ du dépôt du projet](https://github.com/Noah-Cabaret/Assistant-CalcuDoku/releases).

### Option n°2: Compilation à partir du code source

#### Compilation en elle-même

Si désiré, il est évidemment posible de compiler le code source de ce projet en un fichier exécutable. Pour ce faire, il suffit de suivre la procédure Maven standard, [détaillée dans sa documentation officielle](https://maven.apache.org/run.html).

Pour compiler une archive Java (`.jar`) permettant d'appliquer le principe du *Write Once, Run Everywhere*, positionnez un terminal dans le répertoire du `pom.xml` du projet et lancez la commande suivante:

```sh
# Cette commande part du principe que Apache Maven est correctement
# installé sur votre machine.
mvn clean deploy site-deploy
```

## Documentation

### Manuel utilisateur

Un manuel utilisateur pour ce logiciel est disponible à la génération via des fichiers source trouvables dans le répertoire [`doc/sphinx/`](doc/sphinx/). Ce manuel est écrit via Sphinx ; un guide de génération du manuel formatté (HTML) a été rédigé spécialement pour le projet et est disponible à [`doc/sphinx/README.md`](doc/sphinx/README.md).

### Code source

Le code source de l'_Assistant CalcuDoku_ est documenté via Javadoc. Cette dernière peut être consultée à même le code, mais une version web (HTML) formattée peut être générée en lançant la commande suivante à la racine du projet:

```sh
# Cette commande part du principe que votre "Current Working Directory"
# est celle du `pom.xml` Maven.
# Voir aussi: <https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html>
mvn javadoc::javadoc
```

Après quoi la documentation (sous forme de pages HTML) peut être trouvée dans son dossier au sein du répertoire [`calcudoku/target`](calcudoku/target/).
