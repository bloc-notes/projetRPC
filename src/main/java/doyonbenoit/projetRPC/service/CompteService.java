package doyonbenoit.projetRPC.service;

import doyonbenoit.projetRPC.OAD.AvatarOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OTD.CompteOtd;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Groupe;
import doyonbenoit.projetRPC.entite.Role;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompteService {

    private final CompteOad compteOad;
    private final AvatarOad avatarOad;
    private PasswordEncoder mdpCrypter = new BCryptPasswordEncoder();


    public CompteService(CompteOad compteOad, AvatarOad avatarOad) {
        this.compteOad = compteOad;
        this.avatarOad = avatarOad;
    }

    public Compte nouveauCompte(CompteOtd compteOtd){
        Compte compte = null;

        //Validation Serveur
        if (EmailValidator.getInstance().isValid(compteOtd.getCourriel()) && (compteOtd.getMdp().length() >= 6)) {
            compte = new Compte();

            compte.setCourriel(compteOtd.getCourriel());
            compte.setMotDePasse(mdpCrypter.encode(compteOtd.getMdp()));
            compte.setAlias(compteOtd.getAlias());
            compte.setAvatar(avatarOad.findByNom(compteOtd.getAvatar()).get(0));
            compte.setRole(Role.NOUVEAU);
            compte.setGroupe(Groupe.BLANC);

            compteOad.save(compte);
        }

        return compte;
    }
}
