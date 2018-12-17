package doyonbenoit.projetRPC.OAD;

import doyonbenoit.projetRPC.entite.Combat;
import doyonbenoit.projetRPC.entite.Compte;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombatOad extends CrudRepository<Combat,Integer> {
    List<Combat> findByCmArbite(Compte arbite);
    List<Combat> findByDateGreaterThanEqualAndCmBlanc(Long date, Compte blanc);
    List<Combat> findByDateGreaterThanEqualAndCmRouge(Long date, Compte rouge);
    List<Combat> findByCmBlanc(Compte blanc);
    List<Combat> findByCmRouge(Compte rouge);
}
