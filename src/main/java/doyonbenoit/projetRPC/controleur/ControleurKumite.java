package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.entite.*;
import doyonbenoit.projetRPC.enumeration.ActionDeplacement;
import doyonbenoit.projetRPC.enumeration.Attaque;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
/*
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
*/
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
        Compte compteMAJ = null;

        if (objPrincipal instanceof Utilisateur) {
            compte = ((Utilisateur) objPrincipal).getCompte();
            compteMAJ = compteOad.findByCourriel(compte.getCourriel());
        }
        else {
            compteMAJ = new Compte();
        }

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
        if (SalleCombatAndroid.lstAttente.contains(courriel)){SalleCombatAndroid.lstAttente.remove(courriel);}
        if (SalleCombatAndroid.lstSpectateur.contains(courriel)){SalleCombatAndroid.lstSpectateur.remove(courriel);}
        System.out.println(SalleCombatAndroid.lstAilleur.toString());
        envoyerMessages();
    }
    @MessageMapping("/positionSpectateur.{courriel}")
    public void majPositionAndroidSpectateur(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans Spectateur");
        SalleCombatAndroid.lstSpectateur.add(0, courriel);
        if (SalleCombatAndroid.lstAttente.contains(courriel)){SalleCombatAndroid.lstAttente.remove(courriel);}
        if (SalleCombatAndroid.lstAilleur.contains(courriel)){SalleCombatAndroid.lstAilleur.remove(courriel);}

        envoyerMessages();
    }
    @MessageMapping("/positionAttente.{courriel}")
    public void majPositionAndroidAttente(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans attente");
        SalleCombatAndroid.lstAttente.add(0, courriel);
        if (SalleCombatAndroid.lstAilleur.contains(courriel)){SalleCombatAndroid.lstAilleur.remove(courriel);}
        if (SalleCombatAndroid.lstSpectateur.contains(courriel)){SalleCombatAndroid.lstSpectateur.remove(courriel);}
        envoyerMessages();
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
        envoyerMessages();
    }
    @MessageMapping("/positionDelete.{courriel}")
    public void majPositionAndroidRetraitPartout(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        if (SalleCombatAndroid.lstArbitre.contains(courriel)){SalleCombatAndroid.lstArbitre.remove(courriel);}
        if (SalleCombatAndroid.lstAilleur.contains(courriel)){SalleCombatAndroid.lstAilleur.remove(courriel);}
        if (SalleCombatAndroid.lstSpectateur.contains(courriel)){SalleCombatAndroid.lstSpectateur.remove(courriel);}
        if (SalleCombatAndroid.lstAttente.contains(courriel)){SalleCombatAndroid.lstAttente.remove(courriel);}
        envoyerMessages();
    }
    @MessageMapping("/positionAfficher")
    public void majPositionAndroidAfficherTout(){
        envoyerMessages();
    }

    private void envoyerMessages(){
        System.out.println("Envoie du message");
        CombatOuNon();
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre.toString() );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur.toString() );
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur.toString() );
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente.toString() );
    }


    public void combatAndroid(Compte compteRouge,Compte compteBlanc,Compte compteArbitre) {

        Combat combat = new Combat(compteArbitre,compteRouge, compteBlanc, compteBlanc.getGroupe(), compteRouge.getGroupe());
        combat.setDate(Calendar.getInstance().getTime().getTime());
        System.out.println("Envoie des positions des combatants et de l'arbitre");
        simpMessagingTemplate.convertAndSend("/CombatAndroid/1",combat.toString());
        try{
            Thread.sleep(2000);
        }catch (Exception e){System.out.println(e);}

        Random random = new Random();
        Attaque ChoixRouge = Attaque.values()[random.nextInt(2)+0];
        Attaque ChoixBlanc = Attaque.values()[random.nextInt(2)+0];
        System.out.println("Envoie des attaques des combatants");
        simpMessagingTemplate.convertAndSend("/CombatAndroid/2",combat.toString());
        try{
            Thread.sleep(2000);
        }catch (Exception e){System.out.println(e);}

        if (ChoixRouge.equals(Attaque.ROCHE)){
            if (ChoixBlanc.equals(Attaque.PAPIER)){
                gagnantBlanc(combat);
            }else if (ChoixBlanc.equals(Attaque.CISEAU)){
                gagnantRouge(combat);
            }else{
                gagnantEgalite(combat);
            }
        }else{
            if (ChoixBlanc.equals(Attaque.PAPIER)){
                gagnantEgalite(combat);
            }else if (ChoixBlanc.equals(Attaque.CISEAU)){
                gagnantBlanc(combat);
            }else{
                gagnantRouge(combat);
            }
        }
        System.out.println("Envoie du résultat du combat: Rouge:"+ChoixRouge+" Blanc:"+ChoixBlanc);
        simpMessagingTemplate.convertAndSend("/CombatAndroid/3",combat.toString());
        try{
            Thread.sleep(2000);
        }catch (Exception e){System.out.println(e);}
        combatOad.save(combat);

    }
    public void gagnantBlanc(Combat combat){
        Compte cmGagnant = combat.getCmBlanc();
        Compte cmPerdant = combat.getCmRouge();

        combat.setIntGainPertePointBlanc(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));
        combat.setIntGainPerteCreditArbite(1);
    }public void gagnantRouge(Combat combat){
        Compte cmGagnant = combat.getCmRouge();
        Compte cmPerdant = combat.getCmBlanc();

        combat.setIntGainPertePointRouge(cmGagnant.getGroupe().getGroupe().nbPointSelonCeinture(cmPerdant.getGroupe().getGroupe()));
        combat.setIntGainPerteCreditArbite(1);
    }public void gagnantEgalite(Combat combat){
        Compte cmBlanc = combat.getCmBlanc();
        Compte cmRouge = combat.getCmRouge();

        int intNbPointBlanc = Math.round(cmBlanc.getGroupe().getGroupe().nbPointSelonCeinture(cmRouge.getGroupe().getGroupe()) / 2);
        int intNbPointRouge = Math.round(cmRouge.getGroupe().getGroupe().nbPointSelonCeinture(cmBlanc.getGroupe().getGroupe()) / 2);

        combat.setIntGainPertePointBlanc(intNbPointBlanc);
        combat.setIntGainPertePointRouge(intNbPointRouge);

        combat.setIntGainPerteCreditArbite(1);
    }
    @GetMapping(value = "/PeutCombattre1")
    public void CombatO() {
        SalleCombatAndroid.lstAttente.add("b8@dojo");
        SalleCombatAndroid.lstAttente.add("v1@dojo");
        SalleCombatAndroid.lstAttente.add("b14@dojo");
        SalleCombatAndroid.lstArbitre.add("b13@dojo");
        SalleCombatAndroid.lstAttente.add("s2@dojo");
        SalleCombatAndroid.lstAttente.add("v1@dojo");
    }
    public void CombatOuNon() {

        Random random = new Random();
        if ((SalleCombatAndroid.lstAttente.size()>1)&&(SalleCombatAndroid.lstArbitre.size()>0)){
            if (Stream.concat(SalleCombatAndroid.lstArbitre.stream(),SalleCombatAndroid.lstAttente.stream()).distinct().count() >2){
                ArrayList<String> tmpAttente = SalleCombatAndroid.lstAttente;
                ArrayList<String> tmpArbitre = SalleCombatAndroid.lstArbitre;
                System.out.println(SalleCombatAndroid.lstArbitre);
                System.out.println(SalleCombatAndroid.lstAttente);
                Collections.sort(tmpAttente);
                Collections.sort(tmpArbitre);
                int nombre1 = 0;
                int nombre2 = 0;
                int arbitre = -1;
                if (SalleCombatAndroid.lstArbitre.size()==1){
                    System.out.println("un");
                    arbitre=0;
                    while (nombre1==nombre2){
                        nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        if (compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()||
                                compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()){
                            nombre1=0;
                            nombre2=0;
                        }
                    }
                    //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(0)));
                    //System.out.println("1 nombre1:"+nombre1+" nombre2:"+nombre2+" arbitre:"+arbitre);

                    //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                }else if (SalleCombatAndroid.lstAttente.size()==2){
                    System.out.println("2");
                    arbitre=(random.nextInt(SalleCombatAndroid.lstAttente.size()));
                    if ((random.nextInt(2)+1)==1){
                        nombre2=1;
                        //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                        //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(0)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(1)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get((random.nextInt(SalleCombatAndroid.lstAttente.size())))));
                    }else{
                        nombre1=1;
                        //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                        //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(0)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get((random.nextInt(SalleCombatAndroid.lstAttente.size())))));
                    }
                    //System.out.println("2");

                }else if (tmpAttente.equals(tmpArbitre)){
                    arbitre = random.nextInt(SalleCombatAndroid.lstArbitre.size());
                    System.out.println("3");
                    while (nombre1==nombre2){
                        nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        if (compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()||
                                compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()){
                            nombre1=0;
                            nombre2=0;
                        }
                    }
                    //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                    //System.out.println("Rouge:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()+" Blanc:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                    System.out.println("3");
                }else{
                    System.out.println("4");
                    //System.out.println(SalleCombatAndroid.lstArbitre.size());
                    arbitre = random.nextInt(SalleCombatAndroid.lstArbitre.size());
                    while (nombre1==nombre2){
                        nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                        if (compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()||
                                compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()==compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel()){
                            nombre1=0;
                            nombre2=0;
                        }
                    }

                    //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                    //System.out.println("Rouge:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()+" Blanc:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                    //System.out.println("4");
                }

                //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                System.out.println("nombre1:"+nombre1+" nombre2:"+nombre2+" arbitre:"+arbitre);
                System.out.println("Rouge:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()+" Blanc:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));

            }else{
                System.out.println("pas de combat car liste différentes:"+(Stream.concat(SalleCombatAndroid.lstArbitre.stream(),SalleCombatAndroid.lstAttente.stream()).collect(Collectors.toList()).stream().distinct()).toArray().length);
            }
        }else{
            System.out.println("pas de combat car attente:"+SalleCombatAndroid.lstAttente.size()+" et arbitre:"+SalleCombatAndroid.lstArbitre.size());
        }
    }

}
