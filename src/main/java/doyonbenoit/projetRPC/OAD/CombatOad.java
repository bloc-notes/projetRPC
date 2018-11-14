package doyonbenoit.projetRPC.OAD;

import doyonbenoit.projetRPC.entite.Combat;
import doyonbenoit.projetRPC.entite.Compte;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CombatOad extends CrudRepository<Combat,Integer> {
    List<Combat> findByDateLessThanEqualAndDateGreaterThanEqualAndAndCmBlancOrCmRouge(Date dateFin, Date dateDebut, Compte blanc, Compte rouge);
    List<Combat> findByCmBlancOrAndCmRouge(Compte blanc, Compte rouge);
    List<Combat> findByCmArbite(Compte arbite);
}
