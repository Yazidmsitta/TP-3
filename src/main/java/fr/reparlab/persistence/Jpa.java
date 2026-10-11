package fr.reparlab.persistence;

import jakarta.persistence.*;
import java.util.Map;
import java.util.function.Function;

/** Une factory pour l'application, un EntityManager par transaction. */
public final class Jpa implements AutoCloseable {
    private final EntityManagerFactory factory;

    public Jpa() { this(Map.of()); }

    public Jpa(Map<String, ?> proprietes) {
        factory = Persistence.createEntityManagerFactory("reparlab", proprietes);
    }

    public <T> T transaction(Function<EntityManager, T> action) {
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultat = action.apply(em);
            tx.commit();
            return resultat;
        } catch (RuntimeException | Error erreur) {
            if (tx.isActive()) {
                try { tx.rollback(); }
                catch (RuntimeException rollbackErreur) { erreur.addSuppressed(rollbackErreur); }
            }
            throw erreur;
        } finally {
            em.close();
        }
    }

    @Override
    public void close() { factory.close(); }
}
