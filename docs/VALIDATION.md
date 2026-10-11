# Relevé de validation du projet fourni

Validation exécutée le **11 octobre 2026**, en ligne de commande avec le JDK Microsoft OpenJDK **17.0.20.1** et le Maven intégré à IntelliJ IDEA. Les dépendances ont été résolues dans un cache de travail distinct. Ce relevé décrit l’exécution technique du projet livré ; les captures de l’exécution utilisateur dans IntelliJ seront ajoutées ensuite.

## Compilation et tests

Objectifs Maven exécutés : `clean test`.

```text
RelationsJpaTest    : Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
SynchronisationTest: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Total              : Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Démonstration

Objectifs Maven exécutés : `compile exec:java`.

```text
=== 1 - PERSIST en cascade ===
Reparateurs=2 | Interventions=3 | Competences=3 | Liens=4
MERGE retourne une autre instance : true
Radio de poche : TERMINEE

=== 2 - MERGE en cascade ===
Reparateurs=2 | Interventions=3 | Competences=3 | Liens=4

=== 3 - Suppression orpheline ===
Reparateurs=2 | Interventions=2 | Competences=3 | Liens=4

=== 4 - Retrait du lien Couture ===
Reparateurs=2 | Interventions=2 | Competences=3 | Liens=3

=== 5 - REMOVE en cascade ===
Reparateurs=1 | Interventions=1 | Competences=3 | Liens=2

REPARLAB : TOUS LES SCENARIOS SONT VALIDES.
BUILD SUCCESS
```

Extraits limités aux bilans : les requêtes SQL et les journaux Hibernate intermédiaires sont omis. Chaque bilan a été contrôlé après commit par la démonstration.

## Mode console web H2

Après ajout de `DemoH2Navigateur` et `ConsoleH2`, les neuf tests et la démonstration automatique ont été réexécutés avec succès. Une vérification HTTP a ensuite démarré le mode interactif, ouvert la page H2 sur `localhost:8082`, soumis le formulaire avec `jdbc:h2:mem:reparlab`, `sa` et un mot de passe vide, puis exécuté une requête de comptage via la console web à chaque pause.

Les cinq résultats correspondent respectivement à `2/3/3/4`, `2/3/3/4`, `2/2/3/4`, `2/2/3/3` et `1/1/3/2`. La dernière entrée clavier ferme correctement le programme et le serveur. Cette vérification technique ne remplace pas les captures utilisateur attendues.

## Captures utilisateur reçues

Quatre captures réelles ont été ajoutées à la section 9 du README : création du schéma dans IntelliJ, deux réparateurs dans H2, trois interventions dont la Radio terminée, et trois compétences. Elles documentent ces vues ; les résultats des suppressions et des tests restent décrits par la validation technique ci-dessus.
