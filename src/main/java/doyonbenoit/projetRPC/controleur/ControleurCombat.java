package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.entite.Combat;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.SalleCombatAndroid;
import doyonbenoit.projetRPC.enumeration.CouleurCombatant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Combat combat;
        if (!role.contains("arbitre")){
            compteRouge = compteOad.findByCourriel(courriel);
            compteBlanc = compteOad.findByCourriel("s1@dojo");
            compteArbitre = compteOad.findByCourriel("v1@dojo");
            combat = new Combat(compteArbitre, compteBlanc, compteRouge, compteBlanc.getGroupe(), compteRouge.getGroupe());
            combat.setDate(Calendar.getInstance().getTime().getTime());
            switch (role) {
                case "rouge":
                    combat.setIntGainPertePointRouge(10);
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "blanc":
                    combat.setIntGainPertePointBlanc(10);
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "null":
                    combat.setIntGainPertePointBlanc(5);
                    combat.setIntGainPertePointRouge(5);
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
                    combat.setIntGainPertePointRouge(10);
                    combat.setIntGainPerteCreditArbite(1);
                    break;
                case "arbitreFaute":
                    combat.setIntGainPertePointBlanc(5);
                    combat.setIntGainPertePointRouge(5);
                    combat.setIntGainPerteCreditArbite(-5);
                    break;

            }
        }
        combatOad.save(combat);
        System.out.println(combat.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/Historique/{courriel}")
    public List<String> afficheCombat(@PathVariable String courriel) {
        Compte compte = compteOad.findByCourriel(courriel);
        List<Combat> lstCombat = Stream
                .concat(Stream.concat(combatOad.findByCmBlanc(compte).stream(),combatOad.findByCmRouge(compte).stream()),combatOad.findByCmArbite(compte).stream())
                .distinct()
                .sorted(Comparator.comparing(Combat::getDate))
                .collect(Collectors.toList());

        List<String> lstRetour = new ArrayList<>();

        lstRetour.add(StringUtils.rightPad("|Date",25) + StringUtils.rightPad("|Arbite", 12) +
                StringUtils.rightPad("|Crédits",10) + StringUtils.rightPad("|Rouge",12) +
                StringUtils.rightPad("|Ceinture", 12) + StringUtils.rightPad("|Points",10) +
                StringUtils.rightPad("|Blanc",12) + StringUtils.rightPad("|Ceinture",12) +
                StringUtils.rightPad("|Points",10));

        SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        SalleCombatAndroid salleCombatAndroid = new SalleCombatAndroid();

        lstCombat.stream()
                .forEach(combat ->  lstRetour.add("|" + StringUtils.rightPad(formater.format(new Date(combat.getDate())), 24)
                + "|" + StringUtils.rightPad(combat.getCmArbite().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getIntGainPerteCreditArbite().toString(),9) + "|" +
                        StringUtils.rightPad(combat.getCmRouge().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getCmRouge().getGroupe().getGroupe().name(),11) + "|" +
                        StringUtils.rightPad(salleCombatAndroid.calculePointPourCombat(combat).get(CouleurCombatant.ROUGE).toString(),9) + "|" +
                        StringUtils.rightPad(combat.getCmBlanc().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getCmBlanc().getGroupe().getGroupe().name(),11) + "|" +
                        StringUtils.rightPad(salleCombatAndroid.calculePointPourCombat(combat).get(CouleurCombatant.BLANC).toString(),9)));

        return lstRetour;
    }
}
