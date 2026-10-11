# Captures de l’exécution utilisateur

Quatre captures fournies par l’utilisateur sont intégrées à la section 9 du README principal. Les fichiers sont conservés tels qu’ils ont été reçus.

| Fichier intégré | Contenu visible |
|---|---|
| `intellij-creation-schema.png` | Instructions de création du schéma et contraintes Hibernate dans IntelliJ |
| `h2-reparateurs.png` | Clara et Sami dans REPARATEURS |
| `h2-interventions-merge.png` | Trois interventions, dont Radio de poche au statut TERMINEE |
| `h2-competences.png` | Catalogue de trois compétences |

## Suggestions pour compléter le compte rendu

| Fichier prévu | Vue attendue |
|---|---|
| `01-projet-intellij.png` | Projet importé, structure Maven et JDK |
| `02-relations-jpa.png` | Annotations de Reparateur |
| `03-persist.png` | Première étape et compteurs |
| `04-merge.png` | Copie gérée et statut TERMINEE |
| `05-orpheline.png` | Suppression de la fiche Sac |
| `06-dissociation.png` | Retrait du lien Couture |
| `07-suppression.png` | Dernier bilan et validation |
| `08-tests.png` | Neuf tests réussis |
| `09-connexion-h2.png` | Formulaire localhost:8082 avec URL JDBC et utilisateur |
| `10-tables-h2.png` | Tables et résultats SQL à la première pause |

Pour les captures sur localhost, lancez `DemoH2Navigateur`, ouvrez `http://localhost:8082`, puis connectez-vous avec `org.h2.Driver`, `jdbc:h2:mem:reparlab`, utilisateur `sa` et mot de passe vide. Exécutez les SELECT du fichier [requetes-h2.sql](requetes-h2.sql). Appuyez sur Entrée dans la console Run d’IntelliJ uniquement après chaque capture, puis réexécutez les requêtes dans le navigateur.

Envoyez les captures dans la conversation, si possible avec le numéro de l’étape. Des images JPEG conviennent aussi : les noms et liens seront adaptés aux fichiers reçus.

Pour une insertion manuelle, ajoutez une image Markdown accompagnée d’une légende dans la section 9 du README principal, par exemple :

```markdown
![C03 — Persistance en cascade : deux réparateurs, trois interventions et trois compétences](docs/captures/03-persist.png)
```

Gardez le texte de console lisible. Vous pouvez prendre plusieurs captures d’une même étape si le SQL et le bilan ne tiennent pas à l’écran.
