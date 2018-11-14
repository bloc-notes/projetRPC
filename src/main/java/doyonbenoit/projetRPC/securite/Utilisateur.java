package doyonbenoit.projetRPC.securite;

import com.google.common.collect.ImmutableSet;
import doyonbenoit.projetRPC.entite.Compte;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class Utilisateur extends User {
    private final Compte compte;

    public Utilisateur(Compte compte) {
        super(compte.getCourriel(),
                compte.getMotDePasse(),
                true,
                true,
                true,
                true,
                ImmutableSet.of(new SimpleGrantedAuthority(compte.getRole().name())));

        this.compte = compte;
    }

    public Compte getCompte() {
        return compte;
    }
}
