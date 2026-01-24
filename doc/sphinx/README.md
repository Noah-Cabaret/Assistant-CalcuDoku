# *Assistant-CalcuDoku* - Manuel utilisateur

Bienvenue dans ce guide à la génération du manuel utilisateur de l'*Assistant-CalcuDoku*.

Ce répertoire contient le projet [Sphinx](https://sphinx-doc.org) pour le manuel. Afin d'alléger ce dépôt et le rendre plus portable, il ne contient donc que ses sources en [*reStructuredText*](https://docutils.sourceforge.io/rst.html) et les fichiers utilitaires permettant à Sphinx de mettre en forme le manuel.

Ce guide part du principe que vous utilisez un système d'exploitation de type [Linux](https://kernel.org), car le développement y est bien plus facile, notamment concernant l'installation des dépendances de Sphinx.

## Générer le manuel

### Prérequis : Python et *environnement virtuel*

Afin de générer le manuel, il vous faudra d'abord une installation [Python](https://python.org) valide, pour laquelle vous pourrez trouver des instructions officielles [ici](https://www.python.org/about/gettingstarted/).

Une fois Python installé, vous allez devoir créer un [*environnement virtuel* Python](https://docs.python.org/3/library/venv.html) afin de pouvoir installer les packets nécessaires comme Sphinx lui-même ou ses dépendances. Pour cela, positionnez votre terminal dans le répertoire du fichier que vous êtes en train de lire, et lancez la commande:

```sh
# Cette commande part du principe que votre "Current Working Directory"
# est celle du manuel Sphinx.
python -m venv .venv
```

Notez que vous pouvez substituer `.venv` par n'importe quel nom valide de répertoire: il s'agit simplement du nom que l'environnement virtuel portera.

Si la commande s'est exécutée avec succès, un répertoire `.venv` (ou portant le nom que vous avez choisi ci-dessus) a été créé dans le répertoire courant de votre terminal. Vous pouvez maintenant l'activer en utilisant la commande:

```sh
# Si besoin, remplacez `.venv` par le nom que vous avez choisi pour
# l'environnement virtuel.
source .venv/bin/activate
```

Si tout se passe bien, un petit `(.venv)` ou autre indicateur d'environnement virtuel devrait apparaître autour de votre ligne de commande. Vous pouvez maintenant installer Sphinx.

### Installation de Sphinx

Pour installer Sphinx depuis un environnement virtuel Python, rien de plus simple! Soyez d'abord sûr de `source`r votre environnement virtuel (Cf. étape ci-dessus), puis utilisez simplement le fichier `requirements.txt` avec la commande:

```sh
pip install -r requirements.txt
```

Si vous ne détectez aucune erreur, essayez de vérifier l'installation de Sphinx en lançant la commande:

```sh
sphinx-build --version
```

Si un numéro de version s'affiche, félicitations! Vous avez installé Sphinx! Vous pouvez maintenant générer le manuel utilisateur en utilisant la commande:

```sh
sphinx-build source build
```

#### Note:

Vous pouvez changer le format de sortie du manuel (e.g. HTML, PDF, pages `man`, etc) en spécifiant à Sphinx le `builder` à utiliser. Vous pouvez retrouver une liste des `builder`s supportés par Sphinx [ici](https://www.sphinx-doc.org/en/master/usage/builders/index.html).

Par exemple, pour créer un PDF du manuel utilisateur, positionnez-vous dans le répertoire du fichier que vous êtes en train de lire et lancez la commande:

```sh
sphinx-build -M latexpdf source build
```

Pour tout autre `builder` (supporté par Sphinx), il vous suffit de substituer à la commande ci-dessus `latexpdf` par le nom du `builder`.

## Annexe(s)

### Outil optionnel: `sphinx-autobuild`

Si vous souhaitez modifier le contenu du manuel utilisateur, vous voudrez peut-être vous passer d'avoir à **manuellement** compiler la documentation à chaque modification. Sphinx propose un outil permettant d'automatiser ce processus avec [`sphinx-autobuild`](https://github.com/sphinx-doc/sphinx-autobuild), que vous pouvez installer de la même manière que Sphinx lui-même:

```sh
# Soyez sûr(e) de `source`r votre environnement virtuel au préalable!
pip install sphinx-autobuild
```

Une fois installé, utilisez `sphinx-autobuild` de la même manière que `sphinx-build`:

```sh
sphinx-autobuild source build
```

