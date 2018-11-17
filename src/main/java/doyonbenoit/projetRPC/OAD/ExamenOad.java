package doyonbenoit.projetRPC.OAD;

import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Examen;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenOad extends CrudRepository<Examen,Integer> {
    List<Examen> findByCmJugerAndBooReussitOrderByDateDesc(Compte compte, Boolean booReussit);
    Examen findFirstByCmJugerOrderByDateDesc(Compte compte);
}
