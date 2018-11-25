package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
//import doyon.projetRPCA.entite.*;
import doyonbenoit.projetRPC.entite.*;
import doyonbenoit.projetRPC.enumeration.ActionDeplacement;
import doyonbenoit.projetRPC.enumeration.Attaque;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class ControleurKumite {

    @Autowired
    CompteOad compteOad;

    @Autowired
    CombatOad combatOad;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/seConnectKumite")
    @SendTo("/kumite/connexion")
    public SalleCombat kumConnection (String strCourriel) {
        return new SalleCombat();
    }

    public void enterSalle(String strCourriel) {
        Compte compte =  compteOad.findByCourriel(strCourriel);
            SalleCombat.getLstSpectateur().add(0,compte);
    }

    @MessageMapping("/majPositionKumite.{depart}.{arrive}")
    public void majPosition(@Payload Compte compte, @DestinationVariable("depart") String strDepart, @DestinationVariable("arrive") String strArrive) {
        if (strDepart.equalsIgnoreCase(ActionDeplacement.SPECTATEUR.name())) {
            if (strArrive.equalsIgnoreCase(ActionDeplacement.ATTENTECOMBAT.name())) {
                SalleCombat.getLstAttenteCombat().add(0,compte);
                simpMessagingTemplate.convertAndSend("/kumite/majAttenteCombat", new ReponseKumite(compte,"AJOUT"));
            }

            SalleCombat.getLstSpectateur().remove(compte);
            simpMessagingTemplate.convertAndSend("/kumite/majSpectateur", new ReponseKumite(compte, "RETRAIT"));
        }
        else if(strDepart.equalsIgnoreCase(ActionDeplacement.ATTENTECOMBAT.name())) {
            if (strArrive.equalsIgnoreCase(ActionDeplacement.SPECTATEUR.name())) {
                SalleCombat.getLstSpectateur().add(0,compte);
                simpMessagingTemplate.convertAndSend("/kumite/majSpectateur", new ReponseKumite(compte, "AJOUT"));
            }

            SalleCombat.getLstAttenteCombat().remove(compte);
            simpMessagingTemplate.convertAndSend("/kumite/majAttenteCombat", new ReponseKumite(compte, "RETRAIT"));
        }
    }

    @MessageMapping("/demandeArbite")
    public void demandeArbite(Compte compte){
        if (SalleCombat.getCompteArbite() == null) {
            //peut etre arbite
            //sera transfere dans arbite(tatami)
            //block l acces au role d arbite

            //compte pas néssaisaire quand pas liste (a revoir ... polymor...)

            //ajout arbite
            SalleCombat.ajoutALaSalle(ActionDeplacement.ARBITE, compte);
            simpMessagingTemplate.convertAndSend("/kumite/majArbite", new ReponseKumite(compte, "AJOUT"));

            //retire de son lieu d'origine
            ActionDeplacement actionDeplacementArbiteAvant = SalleCombat.estDansSalle(compte.getCourriel());
            SalleCombat.retireDeLaSalle(actionDeplacementArbiteAvant,compte);
            simpMessagingTemplate.convertAndSend("/kumite/" + actionDeplacementArbiteAvant.getStrCheminRetourMaj(), new ReponseKumite(compte, "RETRAIT"));
        }
        else {
            //pas sur encore

            SalleCombat.retireDeLaSalle(ActionDeplacement.ARBITE, compte);
            simpMessagingTemplate.convertAndSend("/kumite/majArbite", new ReponseKumite(compte, "RETRAIT"));

            //Remettre par défault l'utilisateur dans les gradins des spectateurs
            SalleCombat.ajoutALaSalle(ActionDeplacement.SPECTATEUR, compte);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.SPECTATEUR.getStrCheminRetourMaj(), new ReponseKumite(compte, "AJOUT"));
        }
    }


    @MessageMapping("/commenceCombat")
    public void faireEntrerCombatant() {
        //S'il y a asser de personne pour commencer le combat
        int intNbAttente = SalleCombat.getLstAttenteCombat().size();
        if (intNbAttente> 1) {

            //Combatant blanc (au hasard)
            Random ran = new Random();
            Compte cmBlanc = SalleCombat.getLstAttenteCombat().get(ran.nextInt(intNbAttente));

            //retire le compte de la liste serveur
            SalleCombat.retireDeLaSalle(ActionDeplacement.ATTENTECOMBAT,cmBlanc);

            //Combatant rouge (au hasard) temporaire
            ran = new Random();
            Compte cmRouge = SalleCombat.getLstAttenteCombat().get(ran.nextInt(intNbAttente -1));

            //Initialise combat
            Combat combat = new Combat(SalleCombat.getCompteArbite(), cmBlanc, cmRouge, cmBlanc.getGroupe(), cmRouge.getGroupe());
            SalleCombat.setCombatEnCour(combat);

            //retire le compte de la liste serveur
            SalleCombat.retireDeLaSalle(ActionDeplacement.ATTENTECOMBAT, cmRouge);

            //Ajout des combatants dans leur nouvelle affectation serveur
            SalleCombat.ajoutALaSalle(ActionDeplacement.COMBATANTBLANC, cmBlanc);
            SalleCombat.ajoutALaSalle(ActionDeplacement.COMBATANTROUGE, cmRouge);

            //Applique le déplacement au niveau client
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTBLANC.getStrCheminRetourMaj(), new ReponseKumite(cmBlanc, "AJOUT"));
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.ATTENTECOMBAT.getStrCheminRetourMaj(), new ReponseKumite(cmBlanc, "RETRAIT"));

            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTROUGE.getStrCheminRetourMaj(), new ReponseKumite(cmRouge, "AJOUT"));
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.ATTENTECOMBAT.getStrCheminRetourMaj(), new ReponseKumite(cmRouge, "RETRAIT"));
        }
    }

    @MessageMapping("/envoyerAttaque.{attaque}")
    public void receptionAttaque(@Payload Compte compte, @DestinationVariable("attaque") String strAttaque) {
        Attaque attaque = Attaque.valueOf(strAttaque);

        System.out.println(SalleCombat.getCombatEnCour());
        if (SalleCombat.getCombatEnCour().getCmBlanc().equals(compte)) {
            SalleCombat.setAttaqueBlanc(attaque);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTBLANC.getStrCheminRetourMaj(), new ReponseKumite(compte, attaque.name()));
        }
        else {
            SalleCombat.setAttaqueRouge(attaque);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTROUGE.getStrCheminRetourMaj(), new ReponseKumite(compte, attaque.name()));
        }
    }

    @MessageMapping("/finCombat.{terminaison}")
    public void finCombat(@Payload String strVerdict, @DestinationVariable("terminaison") String strScenarioFin) {
        Combat combat = SalleCombat.getCombatEnCour();
        combat.setDate(Calendar.getInstance().getTime().getTime());

        //Déroulement nominal (personne a quitté et l'arbite a rendu son verdique)
        if (strScenarioFin.equals("NORMAL")) {
            if (strVerdict.equalsIgnoreCase("BLANC")) {
                Compte cmGagnant = combat.getCmBlanc();
                Compte cmPerdant = combat.getCmRouge();

                combat.setIntGainPertePointBlanc(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));
                combat.setIntGainPerteCreditArbite(1);
            }
            else if (strVerdict.equalsIgnoreCase("ROUGE")) {
                Compte cmGagnant = combat.getCmRouge();
                Compte cmPerdant = combat.getCmBlanc();

                combat.setIntGainPertePointRouge(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));
                combat.setIntGainPerteCreditArbite(1);
            }
            //Égalité
            else {
                Compte cmBlanc = combat.getCmBlanc();
                Compte cmRouge = combat.getCmRouge();

                int intNbPointBlanc = Math.round(cmBlanc.getGroupe().getGroupe().nbPointSelonCeinture(cmRouge.getGroupe().getGroupe()) / 2);
                int intNbPointRouge = Math.round(cmRouge.getGroupe().getGroupe().nbPointSelonCeinture(cmBlanc.getGroupe().getGroupe()) / 2);

                combat.setIntGainPertePointBlanc(intNbPointBlanc);
                combat.setIntGainPertePointRouge(intNbPointRouge);

                combat.setIntGainPerteCreditArbite(1);
            }

            //le client félicite le vaiqueur .... et affiche le résultat

        }
        //Une personne à quitter le combat, celui-ci est annulé et les pénalités sont appliquées
        else {
            System.out.println("Quelqun a quitter");
            if (strVerdict.equals("ARBITEQUITTER")) {
                Compte cmBlanc = combat.getCmBlanc();
                Compte cmRouge = combat.getCmRouge();

                int intNbPointBlanc = Math.round(cmBlanc.getGroupe().getGroupe().nbPointSelonCeinture(cmRouge.getGroupe().getGroupe()) / 2);
                int intNbPointRouge = Math.round(cmRouge.getGroupe().getGroupe().nbPointSelonCeinture(cmBlanc.getGroupe().getGroupe()) / 2);

                combat.setIntGainPertePointBlanc(intNbPointBlanc);
                combat.setIntGainPertePointRouge(intNbPointRouge);

                combat.setIntGainPerteCreditArbite(-5);

                //Un chronometre niveau client va faire quitter après 5 secondes, car l'arbite ne peut pas le faire ...

            }
            else if (strVerdict.equals("BLANCQUITTER")) {
                Compte cmGagnant = combat.getCmRouge();
                Compte cmPerdant = combat.getCmBlanc();

                combat.setIntGainPertePointRouge(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));

                combat.setIntGainPerteCreditArbite(1);
            }
            else {
                Compte cmGagnant = combat.getCmBlanc();
                Compte cmPerdant = combat.getCmRouge();

                combat.setIntGainPertePointBlanc(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));

                combat.setIntGainPerteCreditArbite(1);
            }
        }
        //Ajout le combat a la bd ...
        combatOad.save(combat);

        simpMessagingTemplate.convertAndSend("/kumite/conclusionCombat", strVerdict);
    }

    @MessageMapping("/expulseCombatant")
    public void expulserCombatant() {
        if (SalleCombat.getCompteBlanc() != null) {
            Compte cmBlanc = SalleCombat.getCompteBlanc();

            SalleCombat.retireDeLaSalle(ActionDeplacement.COMBATANTBLANC,cmBlanc);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTBLANC.getStrCheminRetourMaj(), new ReponseKumite(cmBlanc, "RETRAIT"));

            SalleCombat.ajoutALaSalle(ActionDeplacement.ATTENTECOMBAT, cmBlanc);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.ATTENTECOMBAT.getStrCheminRetourMaj(), new ReponseKumite(cmBlanc, "AJOUT"));
        }
        if (SalleCombat.getCompteRouge() != null) {
            Compte cmRouge = SalleCombat.getCompteRouge();

            SalleCombat.retireDeLaSalle(ActionDeplacement.COMBATANTROUGE,cmRouge);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.COMBATANTROUGE.getStrCheminRetourMaj(), new ReponseKumite(cmRouge, "RETRAIT"));

            SalleCombat.ajoutALaSalle(ActionDeplacement.ATTENTECOMBAT, cmRouge);
            simpMessagingTemplate.convertAndSend("/kumite/" + ActionDeplacement.ATTENTECOMBAT.getStrCheminRetourMaj(), new ReponseKumite(cmRouge, "AJOUT"));
        }
    }

    @GetMapping(value = "/kumite")
    public String pageSaleCombat(Map<String, Object> model) {
        Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Compte compte = null;

        if (objPrincipal instanceof Utilisateur) {
            compte = ((Utilisateur) objPrincipal).getCompte();
        }

        Compte compteMAJ = compteOad.findByCourriel(compte.getCourriel());


        model.put("profile", compteMAJ);
        return "publique/kumite";
    }

    public void quitterSalle(ActionDeplacement acDep, String strCourriel) {
        Compte compte =  compteOad.findByCourriel(strCourriel);
        SalleCombat.retireDeLaSalle(acDep, compte);
        simpMessagingTemplate.convertAndSend("/kumite/" + acDep.getStrCheminRetourMaj(), new ReponseKumite(compte,"RETRAIT"));
    }

    @MessageMapping("/positionAilleur.{courriel}")
    public void majPositionAndroidAilleur(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans ailleur");
        SalleCombatAndroid.lstAilleur.add(0, courriel);
        try{SalleCombatAndroid.lstAttente.remove(courriel);}catch (Exception e){};
        try{SalleCombatAndroid.lstSpectateur.remove(courriel);}catch (Exception e){};
        System.out.println(SalleCombatAndroid.lstAilleur.toString());
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur );
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur );
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente );
    }
    @MessageMapping("/positionSpectateur.{courriel}")
    public void majPositionAndroidSpectateur(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans Spectateur");
        SalleCombatAndroid.lstSpectateur.add(0, courriel);
        try{SalleCombatAndroid.lstAttente.remove(courriel);}catch (Exception e){};
        try{SalleCombatAndroid.lstAilleur.remove(courriel);}catch (Exception e){};
        System.out.println(SalleCombatAndroid.lstSpectateur.toString());
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur );
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur );
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente );
    }
    @MessageMapping("/positionAttente.{courriel}")
    public void majPositionAndroidAttente(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans attente");
        SalleCombatAndroid.lstAttente.add(0, courriel);
        try{SalleCombatAndroid.lstSpectateur.remove(courriel);}catch (Exception e){};
        try{SalleCombatAndroid.lstAilleur.remove(courriel);}catch (Exception e){};
        System.out.println(SalleCombatAndroid.lstAttente.toString());
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur );
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur );
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente );
    }
    @MessageMapping("/positionArbitre.{courriel}.{action}")
    public void majPositionAndroidArbitre(@DestinationVariable("courriel") String courriel, @DestinationVariable("action") boolean booAction){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans arbitre");
        if (booAction) {
            SalleCombatAndroid.lstArbitre.add(0, courriel);
        }
        //Retire le courriel de la liste du serveur
        else {
            SalleCombatAndroid.lstArbitre.remove(courriel);
        }
        System.out.println(SalleCombatAndroid.lstArbitre.toString());
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur );
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur );
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente );
    }
}
