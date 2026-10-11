# TP — Le carnet de réparation partagé

**Durée indicative : 2 à 3 heures.**

## Situation de départ

L’association Répar’Lab organise des ateliers de remise en état d’objets. Elle veut suivre les fiches de travail de ses bénévoles et leur catalogue commun de compétences. Une compétence peut être déclarée par plusieurs réparateurs ; une fiche appartient à un seul carnet.

Le responsable pose deux règles : retirer une fiche d’un carnet doit effacer cette fiche ; retirer une compétence d’un réparateur doit conserver la compétence. Votre travail consiste à traduire ces règles en associations JPA et à les vérifier dans H2.

## Compétences visées

- Transformer les cardinalités métier en annotations JPA.
- Identifier le côté propriétaire d’une association bidirectionnelle.
- Synchroniser les objets en mémoire avant leur enregistrement.
- Justifier les cascades `PERSIST`, `MERGE`, `REMOVE` et la suppression orpheline.
- Manipuler les états transitoire, géré et détaché avec `EntityManager`.
- Vérifier un résultat après commit et annuler une opération par rollback.

## Prérequis

Java 17, IntelliJ IDEA, Maven intégré ou installé, bases de Java/SQL et premières notions de JPA. Le projet fourni utilise Jakarta Persistence et Hibernate sans serveur d’applications. Consultez le README pour l’import et l’exécution.

## Activité 1 — Dessiner le modèle

Définissez les entités suivantes :

| Entité | Attributs simples | Relations |
|---|---|---|
| `Reparateur` | `id`, `nom` | Fiches d’intervention ; compétences |
| `Intervention` | `id`, `objet`, `statut` | Un réparateur obligatoire |
| `Competence` | `id`, `libelle` unique | Réparateurs utilisant la compétence |

Le statut prend les valeurs `A_DIAGNOSTIQUER`, `EN_COURS` et `TERMINEE` et doit être enregistré sous forme de chaîne. Dessinez les cardinalités et placez les clés étrangères. Expliquez pourquoi une table d’association est nécessaire pour les compétences.

**À produire :** un schéma et une justification des deux règles de cycle de vie.

## Activité 2 — Préparer la persistance

Examinez le `pom.xml`, puis `src/main/resources/META-INF/persistence.xml`. Identifiez le fournisseur, le nom de l’unité, l’URL JDBC et la stratégie de création du schéma.

Expliquez le rôle de `RESOURCE_LOCAL` et la raison pour laquelle le TP peut être exécuté par une méthode `main`. Décrivez ce qui arrive aux données lorsque le programme se termine.

**À produire :** capture C01 et court commentaire sur la configuration.

## Activité 3 — Mapper et synchroniser

Étudiez les trois entités fournies, ou reproduisez-les dans une copie de travail :

1. Placez `@OneToMany(mappedBy = "reparateur", cascade = ALL, orphanRemoval = true)` sur les fiches du réparateur.
2. Placez `@ManyToOne(fetch = LAZY, optional = false)` et `@JoinColumn` dans `Intervention`.
3. Déclarez `Reparateur.competences` propriétaire du `ManyToMany`, avec la table `reparateur_competence`.
4. Limitez ses cascades à `PERSIST` et `MERGE`.
5. Déclarez le côté inverse dans `Competence` avec `mappedBy = "competences"`.
6. Vérifiez que chaque méthode d’ajout et de retrait met à jour les deux côtés.

**À produire :** capture C02 et explication de `mappedBy`. N’ajoutez pas de setter public permettant de contourner la synchronisation.

## Activité 4 — Enregistrer un graphe

Construisez les données de Clara et Sami décrites dans le README. Réutilisez **le même objet** Électronique pour les deux réparateurs. Appelez `persist` uniquement sur les réparateurs, puis validez la transaction.

Avant de lancer le programme, prédisez le nombre de lignes dans chacune des quatre tables. Comparez avec la première étape de `DemoReparLab`.

**À produire :** capture C03 et explication du nombre de compétences.

## Activité 5 — Détacher puis fusionner

Chargez Clara et initialisez ses collections dans une transaction. Après fermeture de l’`EntityManager`, marquez la Radio comme terminée. Fusionnez ensuite le carnet dans une nouvelle transaction.

Comparez l’instance détachée à l’instance retournée par `merge`. Relisez le statut depuis un autre contexte de persistance.

**À produire :** capture C04 et réponse : pourquoi la modification faite hors transaction n’a-t-elle pas été enregistrée immédiatement ?

## Activité 6 — Comparer trois suppressions

Exécutez les étapes suivantes dans l’ordre, chacune dans une transaction différente :

1. Retirez la fiche Sac du carnet de Clara, sans appeler `remove` sur la fiche.
2. Retirez Couture des compétences de Clara.
3. Supprimez Clara après avoir synchronisé le retrait de ses liens de compétences.

Pour chaque opération, relevez les lignes supprimées et celles conservées. Repérez les `DELETE` dans la console SQL. Ne vous fiez pas à l’ordre exact des requêtes : comparez leur effet final.

**À produire :** captures C05 à C07 et tableau des quatre compteurs avant/après.

## Activité 7 — Prouver le comportement

Lancez les neuf tests. Dans les tests d’intégration, repérez les nouvelles transactions utilisées pour relire les données. Expliquez pourquoi vérifier seulement une collection Java avant commit ne prouve pas que la base a été mise à jour.

Étudiez également le test de rollback : l’insertion est envoyée à H2 par `flush`, puis annulée. Expliquez pourquoi `flush` ne signifie pas `commit`.

**À produire :** capture C08 et commentaire sur une assertion qui protège une donnée partagée.

## Questions de synthèse

1. Quel attribut possède la relation entre réparateur et intervention ?
2. Quelle différence faites-vous entre `REMOVE` et `orphanRemoval` ?
3. Pourquoi `ALL` convient-il aux fiches mais pas aux compétences ?
4. Que devient Couture si aucun réparateur ne l’utilise ?
5. Pourquoi une méthode d’ajout doit-elle modifier les deux côtés ?
6. Quel objet devient géré après un `merge` ?
7. Pourquoi une console H2 lancée dans un autre processus n’affiche-t-elle pas cette base en mémoire ?
8. Pourquoi les tests relisent-ils les données avec un nouvel `EntityManager` ?

Les réponses sont dans [le corrigé](docs/CORRIGE.md). Essayez d’abord de les formuler à partir des observations.

## Critères de réussite

- Le projet s’ouvre et s’exécute dans IntelliJ avec Java 17.
- Les cinq bilans correspondent au tableau du README.
- Les neuf tests réussissent.
- La suppression de Clara conserve Sami et les compétences.
- Les captures proviennent de votre exécution et sont accompagnées d’une explication.
- La conclusion distingue suppression d’entité, suppression d’association et suppression orpheline.
