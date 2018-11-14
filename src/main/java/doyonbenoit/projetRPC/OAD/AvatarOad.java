package doyonbenoit.projetRPC.OAD;

import doyonbenoit.projetRPC.entite.Avatar;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvatarOad extends CrudRepository<Avatar,String> {
    List<Avatar> findByNom(String nom);
}
