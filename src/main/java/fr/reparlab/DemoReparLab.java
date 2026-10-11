package fr.reparlab;

import fr.reparlab.model.*;
import fr.reparlab.persistence.Jpa;
import fr.reparlab.persistence.ConsoleH2;
import jakarta.persistence.EntityManager;
import java.util.Arrays;

public final class DemoReparLab {
    public static void main(String[] args) throws Exception {
        boolean modeConsole = Arrays.asList(args).contains("--console");
        try (Jpa jpa = new Jpa(); ConsoleH2 console = new ConsoleH2(modeConsole)) {
            Long[] ids = jpa.transaction(em -> {
                Competence electronique = new Competence("Electronique");
                Competence couture = new Competence("Couture");
                Competence cycles = new Competence("Cycles");
                Reparateur clara = new Reparateur("Clara");
                Reparateur sami = new Reparateur("Sami");
                Intervention radio = new Intervention("Radio de poche");
                Intervention sac = new Intervention("Sac de randonnee");
                Intervention velo = new Intervention("Velo pliant");
                clara.ajouterIntervention(radio);
                clara.ajouterIntervention(sac);
                sami.ajouterIntervention(velo);
                clara.ajouterCompetence(electronique);
                clara.ajouterCompetence(couture);
                sami.ajouterCompetence(electronique);
                sami.ajouterCompetence(cycles);
                // Aucun persist explicite sur les interventions ou competences.
                em.persist(clara);
                em.persist(sami);
                return new Long[]{clara.getId(), sami.getId(), sac.getId(), couture.getId()};
            });
            verifierBilan(jpa, "1 - PERSIST en cascade", 2, 3, 3, 4);
            console.pausePourCapture("1 - Donnees initiales : 2 / 3 / 3 / 4");

            // Chargement explicite des collections avant de fermer l'EntityManager.
            Reparateur detache = jpa.transaction(em -> {
                Reparateur r = em.find(Reparateur.class, ids[0]);
                r.getInterventions().size();
                r.getCompetences().size();
                return r;
            });
            detache.getInterventions().stream()
                    .filter(i -> i.getObjet().equals("Radio de poche"))
                    .findFirst().orElseThrow().changerStatut(StatutIntervention.TERMINEE);
            jpa.transaction(em -> {
                Reparateur gere = em.merge(detache);
                System.out.println("MERGE retourne une autre instance : " + (gere != detache));
                return null;
            });
            jpa.transaction(em -> {
                StatutIntervention statut = em.createQuery(
                        "select i.statut from Intervention i where i.objet = :objet", StatutIntervention.class)
                        .setParameter("objet", "Radio de poche").getSingleResult();
                if (statut != StatutIntervention.TERMINEE) throw new IllegalStateException("MERGE non applique");
                System.out.println("Radio de poche : " + statut);
                return null;
            });
            verifierBilan(jpa, "2 - MERGE en cascade", 2, 3, 3, 4);
            console.pausePourCapture("2 - Radio terminee apres MERGE");

            jpa.transaction(em -> {
                Reparateur clara = em.find(Reparateur.class, ids[0]);
                clara.retirerIntervention(em.find(Intervention.class, ids[2]));
                return null;
            });
            verifierBilan(jpa, "3 - Suppression orpheline", 2, 2, 3, 4);
            console.pausePourCapture("3 - Fiche Sac supprimee : 2 / 2 / 3 / 4");

            jpa.transaction(em -> {
                em.find(Reparateur.class, ids[0]).retirerCompetence(em.find(Competence.class, ids[3]));
                return null;
            });
            verifierBilan(jpa, "4 - Retrait du lien Couture", 2, 2, 3, 3);
            console.pausePourCapture("4 - Couture conservee sans lien : 2 / 2 / 3 / 3");

            jpa.transaction(em -> {
                Reparateur clara = em.find(Reparateur.class, ids[0]);
                clara.retirerToutesLesCompetences();
                em.remove(clara);
                return null;
            });
            verifierBilan(jpa, "5 - REMOVE en cascade", 1, 1, 3, 2);
            System.out.println("\nREPARLAB : TOUS LES SCENARIOS SONT VALIDES.");
            console.pausePourCapture("5 - Bilan final : 1 / 1 / 3 / 2. Entree ferme ensuite la base et la console H2.");
        }
    }

    private static void verifierBilan(Jpa jpa, String etape, long r, long i, long c, long l) {
        // Nouveau contexte apres commit : on relit effectivement la base.
        jpa.transaction(em -> {
            long reparateurs = compter(em, "Reparateur");
            long interventions = compter(em, "Intervention");
            long competences = compter(em, "Competence");
            long liens = ((Number) em.createNativeQuery("select count(*) from reparateur_competence")
                    .getSingleResult()).longValue();
            System.out.printf("%n=== %s ===%nReparateurs=%d | Interventions=%d | Competences=%d | Liens=%d%n",
                    etape, reparateurs, interventions, competences, liens);
            if (reparateurs != r || interventions != i || competences != c || liens != l) {
                throw new IllegalStateException("Bilan inattendu a l'etape : " + etape);
            }
            System.out.println("[OK] Bilan confirme apres commit.");
            return null;
        });
    }

    private static long compter(EntityManager em, String entite) {
        return em.createQuery("select count(e) from " + entite + " e", Long.class).getSingleResult();
    }
}
