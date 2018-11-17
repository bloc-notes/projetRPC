package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OTD.CompteOtd;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Groupe;
import doyonbenoit.projetRPC.enumeration.EnumGroupe;
import doyonbenoit.projetRPC.service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Compte")
public class ControleurCompte {

    @Autowired
    CompteOad compteOad;

    @Autowired
    CompteService compteService;

    @PostMapping(value = "/inscription")
    public ResponseEntity<Void> ajoutCompte(@RequestBody CompteOtd compteOtd) {
        ResponseEntity<Void> valeurRetour = null;

        Compte compte = compteService.nouveauCompte(compteOtd);

        valeurRetour = (!compte.equals(null)) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build() ;

        return valeurRetour;
    }

    @GetMapping(value = "/{courriel}")
    public Compte afficheCompte(@PathVariable String courriel){
        return  compteOad.findByCourriel(courriel);
    }

    @GetMapping(value = "/ceinture/{strCeinture}")
    public List<Compte> afficheCompteParCeinture(@PathVariable String strCeinture){
        EnumGroupe ceinture = EnumGroupe.valueOf(strCeinture.toUpperCase());

        return compteOad.findByGroupe(new Groupe(ceinture.ordinal() + 1, ceinture));
    }


}
