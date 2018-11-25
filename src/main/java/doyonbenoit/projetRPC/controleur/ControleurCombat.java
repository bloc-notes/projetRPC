package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.entite.Combat;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
@RestController
@RequestMapping("/Combat")
public class ControleurCombat {
    @Autowired
    CompteOad compteOad;

    @Autowired
    CombatOad combatOad;

    @GetMapping(value = "/{courriel}.{role}")
    public ResponseEntity<Void> afficheComptePointCredit(@PathVariable String courriel, @PathVariable String role) {
        Compte compteRouge;
        Compte compteBlanc;
        Compte compteArbitre;
        Combat combat = new Combat();
        if (!role.contains("arbitre")){
            compteRouge = compteOad.findByCourriel(courriel);
            compteBlanc = compteOad.findByCourriel("s1@dojo");
            compteArbitre = compteOad.findByCourriel("v1@dojo");
            combat = new Combat(compteArbitre, compteBlanc, compteRouge, compteBlanc.getGroupe(), compteRouge.getGroupe());
            combat.setDate(Calendar.getInstance().getTime().getTime());
            switch (role) {
                case "rouge":
                    combat.setIntGainPertePointRouge(compteRouge.getGroupe().getGroupe().nbPointSelonCeinture(compteBlanc.getGroupe().getGroupe()));
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "blanc":
                    combat.setIntGainPertePointBlanc(compteBlanc.getGroupe().getGroupe().nbPointSelonCeinture(compteRouge.getGroupe().getGroupe()));
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "null":
                    int intNbPointBlanc = Math.round(compteBlanc.getGroupe().getGroupe().nbPointSelonCeinture(compteRouge.getGroupe().getGroupe()) / 2);
                    int intNbPointRouge = Math.round(compteRouge.getGroupe().getGroupe().nbPointSelonCeinture(compteBlanc.getGroupe().getGroupe()) / 2);

                    combat.setIntGainPertePointBlanc(intNbPointBlanc);
                    combat.setIntGainPertePointRouge(intNbPointRouge);
                    combat.setIntGainPerteCreditArbite(1);
                    break;
            }
        }else{
            compteRouge = compteOad.findByCourriel("v1@dojo");
            compteBlanc = compteOad.findByCourriel("s1@dojo");
            compteArbitre = compteOad.findByCourriel(courriel);
            combat = new Combat(compteArbitre, compteBlanc, compteRouge, compteBlanc.getGroupe(), compteRouge.getGroupe());
            combat.setDate(Calendar.getInstance().getTime().getTime());
            switch (role) {
                case "arbitre":
                    combat.setIntGainPertePointRouge(compteRouge.getGroupe().getGroupe().nbPointSelonCeinture(compteBlanc.getGroupe().getGroupe()));
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "arbitreFaute":
                    int intNbPointBlanc = Math.round(compteBlanc.getGroupe().getGroupe().nbPointSelonCeinture(compteRouge.getGroupe().getGroupe()) / 2);
                    int intNbPointRouge = Math.round(compteRouge.getGroupe().getGroupe().nbPointSelonCeinture(compteBlanc.getGroupe().getGroupe()) / 2);

                    combat.setIntGainPertePointBlanc(intNbPointBlanc);
                    combat.setIntGainPertePointRouge(intNbPointRouge);
                    combat.setIntGainPerteCreditArbite(-5);
                    break;

            }
        }
        combatOad.save(combat);
        System.out.println(combat.toString());
        return ResponseEntity.ok().build();
    }
}
