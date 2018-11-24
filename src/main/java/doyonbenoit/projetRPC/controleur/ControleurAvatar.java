package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.AvatarOad;
import doyonbenoit.projetRPC.entite.Avatar;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/Avatar"})
public class ControleurAvatar {

    @Autowired
    AvatarOad avatarOad;

    //@Autowired
    //private SessionRegistry sessionRegistry;

    @GetMapping(value = "/liste")
    public List<Avatar> listeAvatar() {
        System.out.println("Test pour le fun de s'assurer que ...");
        //sessionRegistry.getAllPrincipals().forEach(System);

        Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utilisateur utilisateur1 = ((Utilisateur) objPrincipal);
        System.out.println("Utilisateur authentifier: => " + utilisateur1.getUsername());
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

        return (List<Avatar>) avatarOad.findAll();
    }

    @GetMapping(value = "/{nom}")
    public Avatar afficheAvatar(@PathVariable String nom) {
        return avatarOad.findByNom(nom).get(0);
    }
}
