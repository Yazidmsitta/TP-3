package fr.reparlab;

import fr.reparlab.model.*;
import fr.reparlab.persistence.Jpa;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RelationsJpaTest {
    private Jpa jpa;
    private Donnees donnees;
    private record Donnees(Long clara, Long sami, Long radio, Long sac,
                           Long velo, Long electronique, Long couture) { }

    @BeforeEach
    void preparerBaseIndependante() {
        jpa = new Jpa(Map.of(
                "jakarta.persistence.jdbc.url", "jdbc:h2:mem:test_" + UUID.randomUUID(),
                "hibernate.show_sql", "false"));
        donnees = jpa.transaction(em -> {
            Reparateur clara = new Reparateur("Clara");
            Reparateur sami = new Reparateur("Sami");
            Intervention radio = new Intervention("Radio");
            Intervention sac = new Intervention("Sac");
            Intervention velo = new Intervention("Velo");
            Competence electronique = new Competence("Electronique");
            Competence couture = new Competence("Couture");
            clara.ajouterIntervention(radio);
            clara.ajouterIntervention(sac);
            sami.ajouterIntervention(velo);
            clara.ajouterCompetence(electronique);
            clara.ajouterCompetence(couture);
            sami.ajouterCompetence(electronique);
            sami.ajouterCompetence(new Competence("Cycles"));
            em.persist(clara);
            em.persist(sami);
            return new Donnees(clara.getId(), sami.getId(), radio.getId(), sac.getId(),
                    velo.getId(), electronique.getId(), couture.getId());
        });
    }

    @AfterEach
    void fermerBase() {
        if (jpa != null) jpa.close();
    }

    @Test
    @DisplayName("PERSIST sauvegarde le graphe et partage une seule competence")
    void persisterLeGraphe() {
        jpa.transaction(em -> {
            assertEquals(2L, compter(em, "Reparateur"));
            assertEquals(3L, compter(em, "Intervention"));
            assertEquals(3L, compter(em, "Competence"));
            assertEquals(4L, liens(em));
            Competence competence = em.find(Competence.class, donnees.electronique());
            assertEquals(2, competence.getReparateurs().size());
            assertEquals(donnees.clara(), em.find(Intervention.class, donnees.radio()).getReparateur().getId());
            return null;
        });
    }

    @Test
    @DisplayName("orphanRemoval supprime la fiche retiree du carnet")
    void supprimerOrpheline() {
        jpa.transaction(em -> {
            em.find(Reparateur.class, donnees.clara())
                    .retirerIntervention(em.find(Intervention.class, donnees.sac()));
            return null;
        });
        jpa.transaction(em -> {
            assertNull(em.find(Intervention.class, donnees.sac()));
            assertNotNull(em.find(Intervention.class, donnees.radio()));
            assertEquals(2L, compter(em, "Intervention"));
            assertEquals(2L, compter(em, "Reparateur"));
            return null;
        });
    }

    @Test
    @DisplayName("Retirer un lien ManyToMany conserve la competence et les autres liens")
    void retirerSeulementUneAssociation() {
        jpa.transaction(em -> {
            em.find(Reparateur.class, donnees.clara())
                    .retirerCompetence(em.find(Competence.class, donnees.electronique()));
            return null;
        });
        jpa.transaction(em -> {
            Competence competence = em.find(Competence.class, donnees.electronique());
            assertNotNull(competence);
            assertEquals(1, competence.getReparateurs().size());
            assertEquals(donnees.sami(), competence.getReparateurs().iterator().next().getId());
            assertEquals(3L, liens(em));
            assertEquals(3L, compter(em, "Competence"));
            return null;
        });
    }

    @Test
    @DisplayName("REMOVE supprime les fiches du parent sans toucher aux competences partagees")
    void supprimerReparateur() {
        jpa.transaction(em -> {
            Reparateur clara = em.find(Reparateur.class, donnees.clara());
            clara.retirerToutesLesCompetences();
            em.remove(clara);
            return null;
        });
        jpa.transaction(em -> {
            assertNull(em.find(Reparateur.class, donnees.clara()));
            assertNull(em.find(Intervention.class, donnees.radio()));
            assertNull(em.find(Intervention.class, donnees.sac()));
            assertNotNull(em.find(Intervention.class, donnees.velo()));
            assertNotNull(em.find(Competence.class, donnees.couture()));
            assertEquals(3L, compter(em, "Competence"));
            assertEquals(2L, liens(em));
            assertEquals(2, em.find(Reparateur.class, donnees.sami()).getCompetences().size());
            return null;
        });
    }

    @Test
    @DisplayName("MERGE propage les modifications detachees aux deux relations")
    void fusionnerGrapheDetache() {
        Reparateur detache = jpa.transaction(em -> {
            Reparateur clara = em.find(Reparateur.class, donnees.clara());
            clara.getInterventions().size();
            clara.getCompetences().size();
            return clara;
        });
        detache.getInterventions().stream().filter(i -> i.getId().equals(donnees.radio()))
                .findFirst().orElseThrow().changerStatut(StatutIntervention.TERMINEE);
        detache.getCompetences().stream().filter(c -> c.getId().equals(donnees.couture()))
                .findFirst().orElseThrow().renommer("Couture textile");
        jpa.transaction(em -> {
            Reparateur gere = em.merge(detache);
            assertNotSame(detache, gere);
            assertTrue(em.contains(gere));
            assertFalse(em.contains(detache));
            return null;
        });
        jpa.transaction(em -> {
            assertEquals(StatutIntervention.TERMINEE, em.find(Intervention.class, donnees.radio()).getStatut());
            assertEquals("Couture textile", em.find(Competence.class, donnees.couture()).getLibelle());
            assertEquals(4L, liens(em));
            return null;
        });
    }

    @Test
    @DisplayName("Un rollback annule meme une insertion deja envoyee a H2")
    void annulerTransaction() {
        assertThrows(IllegalStateException.class, () -> jpa.transaction(em -> {
            em.persist(new Reparateur("Transaction annulee"));
            em.flush();
            throw new IllegalStateException("Echec simule");
        }));
        jpa.transaction(em -> {
            assertEquals(2L, compter(em, "Reparateur"));
            return null;
        });
    }

    @Test
    @DisplayName("Supprimer une intervention ne supprime jamais son reparateur")
    void nePasPropagerRemoveVersLeParent() {
        jpa.transaction(em -> {
            // La collection du parent reste non chargee dans cette transaction.
            em.remove(em.find(Intervention.class, donnees.radio()));
            return null;
        });
        jpa.transaction(em -> {
            assertNotNull(em.find(Reparateur.class, donnees.clara()));
            assertEquals(1, em.find(Reparateur.class, donnees.clara()).getInterventions().size());
            assertEquals(2L, compter(em, "Intervention"));
            return null;
        });
    }

    private long compter(EntityManager em, String entite) {
        return em.createQuery("select count(e) from " + entite + " e", Long.class).getSingleResult();
    }

    private long liens(EntityManager em) {
        return ((Number) em.createNativeQuery("select count(*) from reparateur_competence")
                .getSingleResult()).longValue();
    }
}
