package fr.reparlab.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "reparateurs")
public class Reparateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    // Les fiches appartiennent au carnet du reparateur.
    @OneToMany(mappedBy = "reparateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Intervention> interventions = new ArrayList<>();

    // Une competence est partagee : aucune cascade REMOVE ici.
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "reparateur_competence",
            joinColumns = @JoinColumn(name = "reparateur_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id"))
    private Set<Competence> competences = new LinkedHashSet<>();

    protected Reparateur() { }

    public Reparateur(String nom) {
        this.nom = Objects.requireNonNull(nom);
    }

    public void ajouterIntervention(Intervention intervention) {
        Objects.requireNonNull(intervention);
        if (intervention.getReparateur() != null && intervention.getReparateur() != this) {
            throw new IllegalArgumentException("Cette fiche appartient deja a un autre reparateur.");
        }
        if (!interventions.contains(intervention)) {
            interventions.add(intervention);
        }
        intervention.rattacherA(this);
    }

    public void retirerIntervention(Intervention intervention) {
        if (interventions.remove(intervention)) {
            intervention.rattacherA(null);
        }
    }

    public void ajouterCompetence(Competence competence) {
        Objects.requireNonNull(competence);
        if (competences.add(competence)) {
            competence.lier(this);
        }
    }

    public void retirerCompetence(Competence competence) {
        if (competences.remove(competence)) {
            competence.delier(this);
        }
    }

    /** Synchronise le cote inverse avant la suppression de ce reparateur. */
    public void retirerToutesLesCompetences() {
        for (Competence competence : new ArrayList<>(competences)) {
            retirerCompetence(competence);
        }
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public List<Intervention> getInterventions() { return Collections.unmodifiableList(interventions); }
    public Set<Competence> getCompetences() { return Collections.unmodifiableSet(competences); }
}
