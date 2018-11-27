package doyonbenoit.projetRPC.entite;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.enumeration.EnumGroupe;
import doyonbenoit.projetRPC.enumeration.EnumRole;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SalleCombatAndroid {

    public static ArrayList<String> lstAilleur =  new ArrayList<>();
    public static ArrayList<String> lstSpectateur =  new ArrayList<>();
    public static ArrayList<String> lstAttente =  new ArrayList<>();
    public static ArrayList<String> lstArbitre =  new ArrayList<>();

    /**
     * Retourne un map avec les clés : [Compte], [NbPoint], [Credit]
     *
     */
    public HashMap<String, Object> calculePoint(String courriel, CombatOad combatOad, CompteOad compteOad, ExamenOad examenOad) {
        HashMap<String,Object> compteComplet = new HashMap<>();
        Compte compte = compteOad.findByCourriel(courriel);
        //Trouve tout ses examens échouer
        List<Examen> lstExamenAnterieurEchouer = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte, Boolean.FALSE);
        int intNbExamenEchouer = lstExamenAnterieurEchouer.size();

        //Trouver tout ses examens réussit
        List<Examen> lstExamenAnterieurReussit = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte,Boolean.TRUE);
        //lstExamenAnterieurReussit.forEach(System.out::println);
        int intNbExamenReussit = lstExamenAnterieurReussit.size();

        //Solde des examens
        int intSoldeExamen =  -1 * ((intNbExamenEchouer * 5) + (intNbExamenReussit * 10));

        //List<Combat> lstCombat;
        List<Combat> lstcmBlanc;
        List<Combat> lstcmRouge;

        //à un examen réussit
        if (lstExamenAnterieurReussit.size() >= 1) {
            System.out.println("POSSEDE UN EXAMEN\n--------------");
            Long dateDebut = lstExamenAnterieurReussit.get(0).getDate();
            //Long dateActuel = Calendar.getInstance().getTime().getTime();

            lstcmBlanc = combatOad.findByDateGreaterThanEqualAndCmBlanc(dateDebut,compte);
            lstcmRouge = combatOad.findByDateGreaterThanEqualAndCmRouge(dateDebut, compte);

            //lstCombat = Stream.concat(lstcmBlanc.stream(),lstcmRouge.stream()).distinct().collect(Collectors.toList());
        }
        else {
            //lstCombat = combatOad.findByCmBlancOrCmRouge(compte,compte);
            lstcmBlanc = combatOad.findByCmBlanc(compte);
            lstcmRouge = combatOad.findByCmRouge(compte);
        }

        //converti les points
        //Pour les blanc
        List<Combat> lstCombatBlancFiltrer = lstcmBlanc.stream()
                .filter(combat -> combat.getIntGainPertePointBlanc() > 0)
                .collect(Collectors.toList());

        lstCombatBlancFiltrer.forEach(combat -> {
            EnumGroupe blanc = combat.getCmBlanc().getGroupe().getGroupe();
            EnumGroupe rouge = combat.getCmRouge().getGroupe().getGroupe();

            int intPoint = blanc.nbPointSelonCeinture(rouge);

            combat.setIntGainPertePointBlanc(combat.getIntGainPertePointBlanc() > 5 ? intPoint : Math.round(intPoint / 2));
        });

        //Pour les rouges
        List<Combat> lstCombatRougeFiltrer = lstcmRouge.stream()
                .filter(combat -> combat.getIntGainPertePointRouge() > 0)
                .collect(Collectors.toList());

        lstCombatRougeFiltrer.forEach(combat -> {
            EnumGroupe blanc = combat.getCmBlanc().getGroupe().getGroupe();
            EnumGroupe rouge = combat.getCmRouge().getGroupe().getGroupe();

            int intPoint = rouge.nbPointSelonCeinture(blanc);

            combat.setIntGainPertePointRouge(combat.getIntGainPertePointRouge() > 5 ? intPoint : Math.round(intPoint / 2));
        });


        //Récupère seulement les points
        Map<Compte,Integer> mapBlanc = lstCombatBlancFiltrer.stream()
                .collect(Collectors.groupingBy(Combat::getCmBlanc, Collectors.summingInt(Combat::getIntGainPertePointBlanc)));

        Map<Compte, Integer> mapRouge = lstCombatRougeFiltrer.stream()
                .collect(Collectors.groupingBy(Combat::getCmRouge, Collectors.summingInt(Combat::getIntGainPertePointRouge)));

        //Total
        Map<Compte, Integer> mapTotal =  Stream.concat(mapBlanc.entrySet().stream(), mapRouge.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));

        int intNbPointCombat = mapTotal.entrySet().stream().mapToInt(value -> value.getValue()).sum();

        //Calcule solde arbitrage
        List<Combat> lstArbitage = combatOad.findByCmArbite(compte);
        int intNbCombatArbitrer = (int) lstArbitage.stream().distinct().count();

        int intSoldeArbitrage = lstArbitage.stream()
                .mapToInt(com -> com.getIntGainPerteCreditArbite())
                .sum();

        //Remplit les conditions de 100 points et 10 credits pour passer son examen de ceinture ...
        //Ajouter -10 si ancient ....
        int intSoldeTotal = 0;
        intSoldeTotal -= compte.getRole().getRole().ordinal() >= EnumRole.ANCIEN.ordinal() ? 10 : 0;
        intSoldeTotal += intSoldeArbitrage + intSoldeExamen;


        //Objet a envoyer
        compteComplet.put("Compte", compte);
        compteComplet.put("NbPoint", intNbPointCombat);
        compteComplet.put("Credit", intSoldeTotal);
        compteComplet.put("NbCombatArbiter", intNbCombatArbitrer);

        return compteComplet;
    }
}
