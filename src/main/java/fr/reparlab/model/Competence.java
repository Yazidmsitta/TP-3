package fr.reparlab.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "competences")
public class Competence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String libelle;

    @ManyToMany(mappedBy = "competences")
    private Set<Reparateur> reparateurs = new LinkedHashSet<>();

    protected Competence() { }

    public Competence(String libelle) { renommer(libelle); }

    public void renommer(String libelle) { this.libelle = Objects.requireNonNull(libelle); }

    public void ajouterReparateur(Reparateur reparateur) {
        Objects.requireNonNull(reparateur).ajouterCompetence(this);
    }

    public void retirerReparateur(Reparateur reparateur) {
        Objects.requireNonNull(reparateur).retirerCompetence(this);
    }

    void lier(Reparateur reparateur) { reparateurs.add(reparateur); }
    void delier(Reparateur reparateur) { reparateurs.remove(reparateur); }

    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public Set<Reparateur> getReparateurs() { return Collections.unmodifiableSet(reparateurs); }
}
