package doyonbenoit.projetRPC.OAD;

import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Groupe;
import doyonbenoit.projetRPC.entite.Role;
import doyonbenoit.projetRPC.enumeration.EnumGroupe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompteOad extends CrudRepository<Compte,Long> {
    Compte findByCourriel(String courriel);
    List<Compte> findByRole(Role role);
    List<Compte> findAll();
    List<Compte> findByGroupe(Groupe groupe);
}
