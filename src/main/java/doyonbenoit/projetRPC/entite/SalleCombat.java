package doyonbenoit.projetRPC.entite;

import doyonbenoit.projetRPC.enumeration.ActionDeplacement;
import doyonbenoit.projetRPC.enumeration.Attaque;

import java.util.ArrayList;
import java.util.List;

public final class SalleCombat {
    private static List<Compte> lstSpectateur = new ArrayList<>();
    private static List<Compte> lstAttenteCombat = new ArrayList<>();
    private static List<Compte> lstAilleur = new ArrayList<>();
    private static List<Compte> lstAttenteArbitre = new ArrayList<>();
    private static Compte Rouge;
    private static Compte Blanc;
    private static Compte Arbite;

    private static Combat combatEnCour;

    private static Attaque attaqueBlanc;
    private static Attaque attaqueRouge;

    public SalleCombat() {
    }

    public static List<Compte> getLstSpectateur() {
        return lstSpectateur;
    }

    public static List<Compte> getLstAttenteCombat() {
        return lstAttenteCombat;
    }

    public Compte getRouge() {
        return Rouge;
    }

    public Compte getBlanc() {
        return Blanc;
    }

    public static Compte getCompteBlanc() {
        return Blanc;
    }

    public static Compte getCompteRouge() {
        return Rouge;
    }

    public List<Compte> getLesSpectateurs() {
        return lstSpectateur;
    }

    public List<Compte> getLesAttentesCombat() {
        return lstAttenteCombat;
    }

    public static List<Compte> getLstAilleur() {
        return lstAilleur;
    }

    public static void setLstAilleur(List<Compte> lstAilleur) {
        SalleCombat.lstAilleur = lstAilleur;
    }

    public static List<Compte> getLstAttenteArbitre() {
        return lstAttenteArbitre;
    }

    public static void setLstAttenteArbitre(List<Compte> lstAttenteArbitre) {
        SalleCombat.lstAttenteArbitre = lstAttenteArbitre;
    }

    public Compte getArbite() {
        return Arbite;
    }

    public static Compte getCompteArbite() {
        return Arbite;
    }

    public static Combat getCombatEnCour() {
        return combatEnCour;
    }

    public static void setCombatEnCour(Combat combatEnCour) {
        SalleCombat.combatEnCour = combatEnCour;
    }

    public static Attaque getAttaqueBlanc() {
        return attaqueBlanc;
    }

    public static void setAttaqueBlanc(Attaque attaqueBlanc) {
        SalleCombat.attaqueBlanc = attaqueBlanc;
    }

    public static Attaque getAttaqueRouge() {
        return attaqueRouge;
    }

    public static void setAttaqueRouge(Attaque attaqueRouge) {
        SalleCombat.attaqueRouge = attaqueRouge;
    }

    public static ActionDeplacement estDansSalle(String strCourriel) {

        ActionDeplacement acDep = null;
        //Vérifie s'il est dans les spectateurs
        if (lstSpectateur.stream().
                filter(compte -> compte.getCourriel().equalsIgnoreCase(strCourriel))
                .findFirst().isPresent()) {
            //Est dans spectateur
            acDep = ActionDeplacement.SPECTATEUR;
        }
        else if (lstAttenteCombat.stream()
                .filter(compte -> compte.getCourriel().equalsIgnoreCase(strCourriel))
                .findFirst().isPresent()){
            //est dans attente combat
            acDep = ActionDeplacement.ATTENTECOMBAT;
        }
        else if(Rouge != null && Rouge.getCourriel().equalsIgnoreCase(strCourriel)) {
            //est combatant rouge
            acDep = ActionDeplacement.COMBATANTROUGE;
        }
        else if(Blanc != null && Blanc.getCourriel().equalsIgnoreCase(strCourriel)) {
            //est combatant blanc
            acDep = ActionDeplacement.COMBATANTBLANC;
        }
        else if(Arbite != null && Arbite.getCourriel().equalsIgnoreCase(strCourriel)) {
            //est l'arbite
            acDep = ActionDeplacement.ARBITE;
        }
        else {
            //N'est pas dans la salle de combat....
        }

        return acDep;
    }

    public static void retireDeLaSalle(ActionDeplacement acDep, Compte compte) {
        switch (acDep) {
            case SPECTATEUR:
                lstSpectateur.remove(compte);
                break;
            case ATTENTECOMBAT:
                lstAttenteCombat.remove(compte);
                break;
            case COMBATANTROUGE:
                Rouge = null;
                break;
            case COMBATANTBLANC:
                Blanc = null;
                break;
            case ARBITE:
                Arbite = null;
                break;
                default:
                    System.out.println("Action supression non prit en charge");
                    break;
        }
    }

    // peut etre doublon comme méthode ...
    public static void ajoutALaSalle(ActionDeplacement acDep, Compte compte) {
        switch (acDep) {
            case SPECTATEUR:
                lstSpectateur.add(0,compte);
                break;
            case ATTENTECOMBAT:
                lstAttenteCombat.add(0,compte);
                break;
            case COMBATANTROUGE:
                Rouge = compte;
                break;
            case COMBATANTBLANC:
                Blanc = compte;
                break;
            case ARBITE:
                Arbite = compte;
                break;
            default:
                System.out.println("Action ajout non prit en charge");
                break;
        }
    }
}
