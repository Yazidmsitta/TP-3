package fr.reparlab;

import fr.reparlab.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SynchronisationTest {
    @Test
    void synchroniserLeCarnetEtRefuserUnAutreProprietaire() {
        Reparateur clara = new Reparateur("Clara");
        Reparateur sami = new Reparateur("Sami");
        Intervention radio = new Intervention("Radio");
        clara.ajouterIntervention(radio);
        clara.ajouterIntervention(radio);
        assertEquals(1, clara.getInterventions().size());
        assertSame(clara, radio.getReparateur());
        assertThrows(IllegalArgumentException.class, () -> sami.ajouterIntervention(radio));
        assertTrue(sami.getInterventions().isEmpty());
        clara.retirerIntervention(radio);
        assertNull(radio.getReparateur());
        assertTrue(clara.getInterventions().isEmpty());
    }

    @Test
    void synchroniserDepuisLeCoteInverseManyToMany() {
        Reparateur clara = new Reparateur("Clara");
        Competence couture = new Competence("Couture");
        couture.ajouterReparateur(clara);
        couture.ajouterReparateur(clara);
        assertEquals(1, clara.getCompetences().size());
        assertTrue(couture.getReparateurs().contains(clara));
        couture.retirerReparateur(clara);
        assertTrue(clara.getCompetences().isEmpty());
        assertTrue(couture.getReparateurs().isEmpty());
    }
}
