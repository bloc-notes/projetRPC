package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.OTD.CompteOtd;
import doyonbenoit.projetRPC.entite.*;
import doyonbenoit.projetRPC.enumeration.EnumGroupe;
import doyonbenoit.projetRPC.enumeration.EnumRole;
import doyonbenoit.projetRPC.service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/Compte")
public class ControleurCompte {

    @Autowired
    CompteOad compteOad;

    @Autowired
    CompteService compteService;

    @Autowired
    ExamenOad examenOad;

    @Autowired
    CombatOad combatOad;

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

    @GetMapping(value = "/tout")
    public List<String> afficheToutCompte() {

        List<Compte> lstCompte = compteOad.findAll();

        List<String> lstCourriel = lstCompte.stream()
                .map(Compte::getCourriel)
                .collect(Collectors.toList());

        return lstCourriel;
    }

    @GetMapping(value = "/PointCredit/{courriel}")
    public HashMap<String,Object> afficheComptePointCredit(@PathVariable String courriel) {
        HashMap<String,Object> compteComplet = new HashMap<>();
        Compte compte = compteOad.findByCourriel(courriel);
        //Trouve tout ses examens échouer
        List<Examen> lstExamenAnterieurEchouer = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte, Boolean.FALSE);
        int intNbExamenEchouer = lstExamenAnterieurEchouer.size();

        //Trouver tout ses examens réussit
        List<Examen> lstExamenAnterieurReussit = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte,Boolean.TRUE);
        lstExamenAnterieurReussit.forEach(System.out::println);
        int intNbExamenReussit = lstExamenAnterieurReussit.size();

        //Solde des examens
        int intSoldeExamen =  -1 * ((intNbExamenEchouer * 5) + (intNbExamenReussit * 10));

        List<Combat> lstCombat;

        //à un examen réussit
        if (lstExamenAnterieurReussit.size() >= 1) {
            System.out.println("POSSEDE UN EXAMEN");
            Long dateDebut = lstExamenAnterieurReussit.get(0).getDate();
            Long dateActuel = Calendar.getInstance().getTime().getTime();

            //lstCombat = combatOad.findByDateLessThanEqualAndDateGreaterThanEqual(dateActuel,dateDebut);
            List<Combat> lstcmBlanc = combatOad.findByDateGreaterThanEqualAndCmBlanc(dateDebut,compte);
            List<Combat> lstcmRouge = combatOad.findByDateGreaterThanEqualAndCmRouge(dateDebut, compte);

            //lstCombat = combatOad.findByDateGreaterThanAndCmBlancOrCmRouge(dateDebut,compte,compte);
            lstCombat = Stream.concat(lstcmBlanc.stream(),lstcmRouge.stream()).distinct().collect(Collectors.toList());

        }
        else {
            lstCombat = combatOad.findByCmBlancOrCmRouge(compte,compte);
        }

        //Calcule le nombre de points
        Map<Compte,Integer> mapBlanc = lstCombat.stream()
                .filter(combat -> combat.getCmBlanc().getCourriel().equalsIgnoreCase(courriel))
                .collect(Collectors.groupingBy(Combat::getCmBlanc, Collectors.summingInt(Combat::getIntGainPertePointBlanc)));

        Map<Compte, Integer> mapRouge = lstCombat.stream()
                .filter(combat -> combat.getCmRouge().getCourriel().equalsIgnoreCase(courriel))
                .collect(Collectors.groupingBy(Combat::getCmRouge, Collectors.summingInt(Combat::getIntGainPertePointRouge)));

        //Total
        Map<Compte, Integer> mapTotal =  Stream.concat(mapBlanc.entrySet().stream(), mapRouge.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));

        int intNbPointCombat = mapTotal.entrySet().stream().mapToInt(value -> value.getValue()).sum();

        //Calcule solde arbitrage
        List<Combat> lstArbitage = combatOad.findByCmArbite(compte);
        int intNbCombatArbitrer = lstCombat.size();

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
        return compteComplet;
    }
}
