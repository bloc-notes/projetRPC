package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.AvatarOad;
import doyonbenoit.projetRPC.entite.Avatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Avatar")
public class ControleurAvatar {

    @Autowired
    AvatarOad avatarOad;

    @GetMapping(value = "/liste")
    public List<Avatar> listeAvatar() {
        return (List<Avatar>) avatarOad.findAll();
    }

    @GetMapping(value = "/{nom}")
    public Avatar afficheAvatar(@PathVariable String nom) {
        return avatarOad.findByNom(nom).get(0);
    }
}
