package doyonbenoit.projetRPC.securite;

import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.entite.Compte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UtilisateurDetailService implements UserDetailsService {
    @Autowired
    private CompteOad compteOAD;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String courriel) throws UsernameNotFoundException {

        Optional<Compte> compte = Optional.ofNullable(compteOAD.findByCourriel(courriel));
        Compte c;
        if (compte.isPresent())
            c = compte.get();
        else
        {
            //Mot de passe anonyme est JaimeLesPatates!!!
            c = new Compte("anonyme","$2a$10$v3EXvAFWCsyUNWmKaw6uI.TanoXeQ9h0zq4mxurfdSow.dvCvOovO",
                    "anonyme");
        }
        return new Utilisateur(c);
    }
}
