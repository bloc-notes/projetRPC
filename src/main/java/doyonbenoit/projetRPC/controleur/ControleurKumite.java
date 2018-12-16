package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.entite.*;
import doyonbenoit.projetRPC.enumeration.ActionDeplacement;
import doyonbenoit.projetRPC.enumeration.Attaque;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
    public void kumConnection (String strCourriel) {
        envoyerMessages();
    }

    public void enterSalle(String strCourriel) {
        Compte compte =  compteOad.findByCourriel(strCourriel);
            SalleCombatAndroid.lstSpectateur.add(0,compte);
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
        Compte compteConnecté = compteOad.findByCourriel(courriel);
        SalleCombatAndroid.lstAilleur.add(0, compteConnecté);
        if (SalleCombatAndroid.lstAttente.contains(compteConnecté)){SalleCombatAndroid.lstAttente.remove(compteConnecté);}
        if (SalleCombatAndroid.lstSpectateur.contains(compteConnecté)){SalleCombatAndroid.lstSpectateur.remove(compteConnecté);}
        System.out.println(SalleCombatAndroid.lstAilleur.toString());
        envoyerMessages();
    }
    @MessageMapping("/positionSpectateur.{courriel}")
    public void majPositionAndroidSpectateur(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans Spectateur");
        Compte compteConnecté = compteOad.findByCourriel(courriel);
        SalleCombatAndroid.lstSpectateur.add(0, compteConnecté);
        if (SalleCombatAndroid.lstAttente.contains(compteConnecté)){SalleCombatAndroid.lstAttente.remove(compteConnecté);}
        if (SalleCombatAndroid.lstAilleur.contains(compteConnecté)){SalleCombatAndroid.lstAilleur.remove(compteConnecté);}

        envoyerMessages();
    }
    @MessageMapping("/positionAttente.{courriel}")
    public void majPositionAndroidAttente(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans attente");
        Compte compteConnecté = compteOad.findByCourriel(courriel);
        SalleCombatAndroid.lstAttente.add(0, compteConnecté);
        if (SalleCombatAndroid.lstAilleur.contains(compteConnecté)){SalleCombatAndroid.lstAilleur.remove(compteConnecté);}
        if (SalleCombatAndroid.lstSpectateur.contains(compteConnecté)){SalleCombatAndroid.lstSpectateur.remove(compteConnecté);}
        envoyerMessages();
    }
    @MessageMapping("/positionArbitre.{courriel}.{action}")
    public void majPositionAndroidArbitre(@DestinationVariable("courriel") String courriel, @DestinationVariable("action") boolean booAction){
        //Ajoute le courriel dans la liste du serveur
        System.out.print("entré dans arbitre");
        Compte compteConnecté = compteOad.findByCourriel(courriel);
        if (booAction) {
            SalleCombatAndroid.lstArbitre.add(0, compteConnecté);
        }
        //Retire le courriel de la liste du serveur
        else {
            SalleCombatAndroid.lstArbitre.remove(compteConnecté);
        }
        System.out.println(SalleCombatAndroid.lstArbitre.toString());
        envoyerMessages();
    }
    @MessageMapping("/positionDelete.{courriel}")
    public void majPositionAndroidRetraitPartout(@DestinationVariable("courriel") String courriel){
        //Ajoute le courriel dans la liste du serveur
        Compte compteConnecté = compteOad.findByCourriel(courriel);
        if (SalleCombatAndroid.lstArbitre.contains(compteConnecté)){SalleCombatAndroid.lstArbitre.remove(compteConnecté);}
        if (SalleCombatAndroid.lstAilleur.contains(compteConnecté)){SalleCombatAndroid.lstAilleur.remove(compteConnecté);}
        if (SalleCombatAndroid.lstSpectateur.contains(compteConnecté)){SalleCombatAndroid.lstSpectateur.remove(compteConnecté);}
        if (SalleCombatAndroid.lstAttente.contains(compteConnecté)){SalleCombatAndroid.lstAttente.remove(compteConnecté);}
        envoyerMessages();
    }
    @MessageMapping("/positionAfficher")
    public void majPositionAndroidAfficherTout(){
        envoyerMessages();
    }

    private void envoyerMessages(){
        System.out.println("Envoie du message");
        simpMessagingTemplate.convertAndSend("/kumite/androidArbitre", SalleCombatAndroid.lstArbitre );
        simpMessagingTemplate.convertAndSend("/kumite/androidAilleur", SalleCombatAndroid.lstAilleur);
        simpMessagingTemplate.convertAndSend("/kumite/androidSpectateur", SalleCombatAndroid.lstSpectateur);
        simpMessagingTemplate.convertAndSend("/kumite/androidAttente", SalleCombatAndroid.lstAttente );
        CombatOuNon();
    }


    public void combatAndroid(Compte compteRouge,Compte compteBlanc,Compte compteArbitre) {
        if (!SalleCombatAndroid.booCombatEnCours) {
            SalleCombatAndroid.booCombatEnCours=true;
            Combat combat = new Combat(compteArbitre, compteRouge, compteBlanc, compteBlanc.getGroupe(), compteRouge.getGroupe());
            combat.setDate(Calendar.getInstance().getTime().getTime());
            System.out.println("Envoie des positions des combatants et de l'arbitre");
            simpMessagingTemplate.convertAndSend("/kumite/CombatAndroid/1", combat);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println(e);
            }

            Random random = new Random();
            Attaque ChoixRouge = Attaque.values()[random.nextInt(3)];
            Attaque ChoixBlanc = Attaque.values()[random.nextInt(3)];

            combat.setAttBlanc(ChoixBlanc);
            combat.setAttRouge(ChoixRouge);

            System.out.println("Envoie des attaques des combatants: Rouge:" + ChoixRouge + " Blanc:" + ChoixBlanc);
            simpMessagingTemplate.convertAndSend("/kumite/CombatAndroid/2", combat);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (ChoixRouge.equals(Attaque.ROCHE)) {

                if (ChoixBlanc.equals(Attaque.PAPIER)) {
                    gagnantBlanc(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant blanc");
                } else if (ChoixBlanc.equals(Attaque.CISEAU)) {
                    gagnantRouge(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant rouge");
                } else {
                    gagnantEgalite(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant egalité");
                }
            } else if (ChoixRouge.equals(Attaque.PAPIER)) {
                if (ChoixBlanc.equals(Attaque.PAPIER)) {
                    gagnantEgalite(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant egalité");
                } else if (ChoixBlanc.equals(Attaque.CISEAU)) {
                    gagnantBlanc(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant blanc");
                } else {
                    gagnantRouge(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant rouge");
                }
            }else{
                if (ChoixBlanc.equals(Attaque.PAPIER)) {
                    gagnantRouge(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant rouge");
                } else if (ChoixBlanc.equals(Attaque.CISEAU)) {
                    gagnantEgalite(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant egalité");
                } else {
                    gagnantBlanc(combat);
                    System.out.println("Envoie du résultat du combat: Gagnant blanc");
                }
            }

            simpMessagingTemplate.convertAndSend("/kumite/CombatAndroid/3", combat);
            combatOad.save(combat);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println(e);
            }
            simpMessagingTemplate.convertAndSend("/kumite/CombatAndroid/4", combat);

            //METTRE A JOUR L'INFORMATION
            simpMessagingTemplate.convertAndSend("/kumite/MiseAJourCompte","");


            SalleCombatAndroid.booCombatEnCours=false;
            CombatOuNon();
        }

    }
    public void gagnantBlanc(Combat combat){
        combat.setIntGainPertePointBlanc(10);
        combat.setIntGainPerteCreditArbite(1);
    }public void gagnantRouge(Combat combat){
        combat.setIntGainPertePointRouge(10);
        combat.setIntGainPerteCreditArbite(1);
    }public void gagnantEgalite(Combat combat){
        int intNbPointBlanc = Math.round(5);
        int intNbPointRouge = Math.round(5);

        combat.setIntGainPertePointBlanc(intNbPointBlanc);
        combat.setIntGainPertePointRouge(intNbPointRouge);

        combat.setIntGainPerteCreditArbite(1);
    }
    public static <Compte> Predicate<Compte> distinctByKey(Function<? super Compte, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    public void CombatOuNon() {
        if (!SalleCombatAndroid.booCombatEnCours) {
            Random random = new Random();
            if ((SalleCombatAndroid.lstAttente.size() > 1) && (SalleCombatAndroid.lstArbitre.size() > 0)) {
                //if (Stream.concat(SalleCombatAndroid.lstArbitre.stream(), SalleCombatAndroid.lstAttente.stream()).distinct().count() > 2) {
                if (Stream.concat(SalleCombatAndroid.lstArbitre.stream(), SalleCombatAndroid.lstAttente.stream()).filter(distinctByKey(Compte::getCourriel)).count() > 2) {
                    //System.out.println(SalleCombatAndroid.lstArbitre);
                    //System.out.println(SalleCombatAndroid.lstAttente);
                    int nombre1 = 0;
                    int nombre2 = 0;
                    int arbitre = -1;
                    if (SalleCombatAndroid.lstArbitre.size() == 1) {
                        System.out.println("un");
                        arbitre = 0;
                        while (nombre1 == nombre2) {
                            nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            if (SalleCombatAndroid.lstAttente.get(nombre1).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel() ||
                                    SalleCombatAndroid.lstAttente.get(nombre2).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel()) {
                                nombre1 = 0;
                                nombre2 = 0;
                            }
                        }
                        //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(0)));
                        //System.out.println("1 nombre1:"+nombre1+" nombre2:"+nombre2+" arbitre:"+arbitre);

                        //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                    } else if (SalleCombatAndroid.lstAttente.size() == 2) {
                        System.out.println("2");
                        while (arbitre == -1) {
                            arbitre = (random.nextInt(SalleCombatAndroid.lstArbitre.size()));
                            if (SalleCombatAndroid.lstAttente.contains(SalleCombatAndroid.lstArbitre.get(arbitre))) {
                                arbitre = -1;
                            }
                        }
                        if ((random.nextInt(2) + 1) == 1) {
                            nombre2 = 1;
                            //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                            //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(0)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(1)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get((random.nextInt(SalleCombatAndroid.lstAttente.size())))));
                        } else {
                            nombre1 = 1;
                            //System.out.println("Rouge:"+compteOad.findAll().get(nombre1).getCourriel()+" Blanc:"+compteOad.findAll().get(nombre2).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                            //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(0)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get((random.nextInt(SalleCombatAndroid.lstAttente.size())))));
                        }
                        //System.out.println("2");

                    } else if (SalleCombatAndroid.lstAttente.size() == SalleCombatAndroid.lstArbitre.size() && SalleCombatAndroid.lstAttente.containsAll(SalleCombatAndroid.lstArbitre)) {
                        arbitre = random.nextInt(SalleCombatAndroid.lstArbitre.size());
                        System.out.println("3");
                        while (nombre1 == nombre2) {
                            nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            if (SalleCombatAndroid.lstAttente.get(nombre1).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel() ||
                                    SalleCombatAndroid.lstAttente.get(nombre2).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel()) {
                                nombre1 = 0;
                                nombre2 = 0;
                            }
                        }
                        //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                        //System.out.println("Rouge:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()+" Blanc:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                        System.out.println("3");
                    } else {
                        System.out.println("4");
                        //System.out.println(SalleCombatAndroid.lstArbitre.size());
                        arbitre = random.nextInt(SalleCombatAndroid.lstArbitre.size());
                        while (nombre1 == nombre2) {
                            nombre1 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            nombre2 = random.nextInt(SalleCombatAndroid.lstAttente.size());
                            if (SalleCombatAndroid.lstAttente.get(nombre1).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel() ||
                                    SalleCombatAndroid.lstAttente.get(nombre2).getCourriel() == SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel()) {
                                nombre1 = 0;
                                nombre2 = 0;
                            }
                        }

                        //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                        //System.out.println("Rouge:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)).getCourriel()+" Blanc:"+compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)).getCourriel()+" Arbitre"+compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)).getCourriel());
                        //System.out.println("4");
                    }

                    //combatAndroid(compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre1)),compteOad.findByCourriel(SalleCombatAndroid.lstAttente.get(nombre2)),compteOad.findByCourriel(SalleCombatAndroid.lstArbitre.get(arbitre)));
                    System.out.println("nombre1:" + nombre1 + " nombre2:" + nombre2 + " arbitre:" + arbitre);
                    System.out.println("Rouge:" + SalleCombatAndroid.lstAttente.get(nombre1).getCourriel() + " Blanc:" + SalleCombatAndroid.lstAttente.get(nombre2).getCourriel() + " Arbitre" + SalleCombatAndroid.lstArbitre.get(arbitre).getCourriel());
                    combatAndroid(SalleCombatAndroid.lstAttente.get(nombre1), SalleCombatAndroid.lstAttente.get(nombre2), SalleCombatAndroid.lstArbitre.get(arbitre));

                } else {
                    System.out.println("pas de combat car liste différentes:" + (Stream.concat(SalleCombatAndroid.lstArbitre.stream(), SalleCombatAndroid.lstAttente.stream()).collect(Collectors.toList()).stream().distinct()).toArray().length);
                }
            } else {
                System.out.println("pas de combat car attente:" + SalleCombatAndroid.lstAttente.size() + " et arbitre:" + SalleCombatAndroid.lstArbitre.size());
            }
        }
    }

}
