package fr.reparlab.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "interventions")
public class Intervention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String objet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutIntervention statut = StatutIntervention.A_DIAGNOSTIQUER;

    // Cote proprietaire : cette table porte la cle etrangere.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reparateur_id", nullable = false)
    private Reparateur reparateur;

    protected Intervention() { }

    public Intervention(String objet) {
        this.objet = Objects.requireNonNull(objet);
    }

    // Visibilite package pour imposer l'usage des methodes de Reparateur.
    void rattacherA(Reparateur reparateur) { this.reparateur = reparateur; }

    public void changerStatut(StatutIntervention statut) {
        this.statut = Objects.requireNonNull(statut);
    }

    public Long getId() { return id; }
    public String getObjet() { return objet; }
    public StatutIntervention getStatut() { return statut; }
    public Reparateur getReparateur() { return reparateur; }
}
