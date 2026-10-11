# Corrigé raisonné

## 1. Propriétaire d’une relation

`Intervention.reparateur` possède la relation : sa table porte `reparateur_id`. `Reparateur.interventions` est inverse grâce à `mappedBy = "reparateur"`. Pour les compétences, `Reparateur.competences` possède l’association et décrit la table de jointure.

## 2. REMOVE et orphanRemoval

`CascadeType.REMOVE` propage la suppression d’une entité source aux entités associées. `orphanRemoval=true` supprime une entité enfant retirée d’une relation de dépendance, même si son parent n’est pas supprimé. Ici, retirer Sac supprime seulement cette fiche ; supprimer Clara supprime également sa fiche Radio.

Pour un `OneToMany` avec suppression orpheline, supprimer le parent entraîne aussi la suppression de ses enfants. Le code utilise `ALL` pour illustrer l’ensemble du cycle de vie : on ne peut donc pas isoler expérimentalement la contribution de `REMOVE` en supprimant seulement cette cascade tout en conservant `orphanRemoval=true`.

## 3. Choix des cascades

Les fiches ont un cycle de vie dépendant du carnet ; `ALL` regroupe notamment `PERSIST`, `MERGE` et `REMOVE`. Les compétences sont partagées : elles doivent survivre à la suppression d’un réparateur. Seules les cascades `PERSIST` et `MERGE` leur sont appliquées. Les cascades JPA sont des opérations de l’ORM, distinctes d’une clause SQL `ON DELETE CASCADE`.

## 4. Compétence sans réparateur

Couture reste dans `competences`, mais son association avec Clara est supprimée de `reparateur_competence`. Ce n’est pas une fiche orpheline : c’est une entrée indépendante du catalogue. La supprimer demanderait une action explicite et une vérification préalable de ses usages.

## 5. Deux côtés à synchroniser

Les deux collections/références Java sont indépendantes. Hibernate s’appuie sur le propriétaire pour enregistrer une association ; il ne corrige pas automatiquement toutes les références de votre modèle objet. Les méthodes métier rendent la navigation cohérente avant et après le flush. Les méthodes de `Competence` délèguent à celles de `Reparateur` pour modifier le propriétaire même depuis le côté inverse.

## 6. État après merge

L’instance retournée par `merge` est gérée par l’`EntityManager`. L’objet passé en argument reste détaché. Le commit enregistre les changements de l’instance gérée ; les modifications ultérieures de l’ancienne instance ne sont pas suivies automatiquement.

## 7. Visibilité de H2

L’URL `jdbc:h2:mem:reparlab` désigne une base dans le processus de la démonstration. Un autre processus utilisant la même chaîne n’accède pas à cette instance. `DB_CLOSE_DELAY=-1` conserve la base pendant la vie de la JVM lorsque les connexions sont fermées ; il ne la transforme pas en base persistante. `create-drop` supprime les tables à la fermeture de la factory.

## 8. Vérifier après commit

Un contexte de persistance garde les objets gérés en mémoire. Relire la collection du même objet ne prouve pas une écriture en base. Les tests valident la transaction puis ouvrent un nouvel `EntityManager` afin d’observer les données enregistrées. `flush` envoie les modifications au moteur SQL, mais un rollback peut encore les annuler avant le commit.

## Résultats de la démonstration

| Phase | Réparateurs | Interventions | Compétences | Associations |
|---|---:|---:|---:|---:|
| Persistance | 2 | 3 | 3 | 4 |
| Fusion | 2 | 3 | 3 | 4 |
| Retrait Sac | 2 | 2 | 3 | 4 |
| Retrait Couture | 2 | 2 | 3 | 3 |
| Suppression Clara | 1 | 1 | 3 | 2 |

## Limites du modèle pédagogique

Une fiche persistée retirée du carnet est destinée à être supprimée : ce modèle n’implémente pas le transfert de fiches entre réparateurs. Une application qui doit réaffecter ou archiver les interventions demandera d’autres règles de cycle de vie. Le test de suppression directe d’une fiche montre l’absence de cascade vers le parent ; dans le code métier courant, la méthode `retirerIntervention` est préférable pour conserver les deux côtés synchronisés en mémoire.
