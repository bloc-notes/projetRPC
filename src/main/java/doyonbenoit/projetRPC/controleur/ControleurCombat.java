package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.entite.Combat;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Examen;
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

    @Autowired
    ExamenOad examenOad;

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

        List<Examen> lstExamen = examenOad.findByCmJuger(compte);

        List<String> lstRetour = new ArrayList<>();

        SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        ListIterator<Examen> itExamen = lstExamen.listIterator();
        ListIterator<Combat> itCombat = lstCombat.listIterator();

        String strEnteteCombat = "\nCombats\n" +
                StringUtils.rightPad("|Date",25) + StringUtils.rightPad("|Arbite", 12) +
                StringUtils.rightPad("|Crédits",10) + StringUtils.rightPad("|Rouge",12) +
                StringUtils.rightPad("|Ceinture", 12) + StringUtils.rightPad("|Points",10) +
                StringUtils.rightPad("|Blanc",12) + StringUtils.rightPad("|Ceinture",12) +
                StringUtils.rightPad("|Points",10);

        String strEnteteExamen = "\nExamen";

        SalleCombatAndroid salleCombatAndroid = new SalleCombatAndroid();

        int intNbPoint = 0;
        int intNbCredit = 0;

        //Pré-condition: La première tâche (temporel) est un examen (examen d'entré)
        do {
            //Ajoute examen
            Examen examen = itExamen.next();

            lstRetour.add(strEnteteExamen);
            lstRetour.add("Date: " + formater.format(new Date(examen.getDate())) + " Points: " + intNbPoint +
                    " Crédits: " + intNbCredit + " Ceinture: " + examen.getCeinture().getGroupe().name() +
                    " Statut: " + (examen.getBooReussit() ? "Réussi" : "Échoué"));


            if (examen.getBooReussit()) {
                intNbPoint = 0;
            }

            intNbCredit -= examen.getBooReussit() ? 10 : 5;

            int intNbElement = lstRetour.size();

            //Ajout un combat si date plus petite que le prochain examen
            while (itCombat.hasNext() &&
                    (itExamen.hasNext() &&
                            (lstCombat.get(itCombat.nextIndex()).getDate() < lstExamen.get(itExamen.nextIndex()).getDate()))) {
                Combat combat = itCombat.next();

                //Si premier élement, ajoute en-tete
                if (intNbElement == lstRetour.size()) {
                    lstRetour.add(strEnteteCombat);
                }


                Integer intNbPointBlanc = salleCombatAndroid.calculePointPourCombat(combat).get(CouleurCombatant.BLANC);
                Integer intNbPointRouge = salleCombatAndroid.calculePointPourCombat(combat).get(CouleurCombatant.ROUGE);

                //ajoute combat
                lstRetour.add("|" + StringUtils.rightPad(formater.format(new Date(combat.getDate())), 24)
                        + "|" + StringUtils.rightPad(combat.getCmArbite().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getIntGainPerteCreditArbite().toString(),9) + "|" +
                        StringUtils.rightPad(combat.getCmRouge().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getCeintureRouge().getGroupe().name(),11) + "|" +
                        StringUtils.rightPad(intNbPointRouge.toString(),9) + "|" +
                        StringUtils.rightPad(combat.getCmBlanc().getCourriel(),11) + "|" +
                        StringUtils.rightPad(combat.getCeintureBanc().getGroupe().name(),11) + "|" +
                        StringUtils.rightPad(intNbPointBlanc.toString(),9));

                //Incrémente les crédits et les points
                if (combat.getCmArbite().equals(compte)) {
                    intNbCredit += combat.getIntGainPerteCreditArbite();
                }

                if (combat.getCmRouge().equals(compte)) {
                    intNbPoint += intNbPointRouge;
                }
                else if (combat.getCmBlanc().equals(compte)) {
                    intNbPoint += intNbPointBlanc;
                }

            }
        } while (itExamen.hasNext());

        return lstRetour;
    }
}
