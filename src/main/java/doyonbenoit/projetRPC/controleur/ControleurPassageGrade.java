package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CombatOad;
import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.OTD.ExamenOtd;
//import doyon.projetRPCA.entite.*;
import doyonbenoit.projetRPC.entite.*;
import doyonbenoit.projetRPC.enumeration.EnumGroupe;
import doyonbenoit.projetRPC.enumeration.EnumRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/PassageGrade")
public class ControleurPassageGrade {

    @Autowired
    CompteOad compteOAD;

    @Autowired
    CombatOad combatOad;

    @Autowired
    ExamenOad examenOad;

    @GetMapping(value = "/ExamenCeinturePoss")
    public Map<String,List<ReponseExamen>> personnePourExamenCeinture() {

        List<Compte> lstcmPossibleExam = compteOAD.findAll();

        List<ReponseExamen> lstCmValidePourExamen = new ArrayList<>();
        List<ReponseExamen> lstCmValidePourPromotion = new ArrayList<>();

        lstcmPossibleExam.forEach(compte -> {
            //Trouve tout ses examens échouer
            List<Examen> lstExamenAnterieurEchouer = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte, Boolean.FALSE);
            int intNbExamenEchouer = lstExamenAnterieurEchouer.size();

            //Trouver tout ses examens réussit
            List<Examen> lstExamenAnterieurReussit = examenOad.findByCmJugerAndBooReussitOrderByDateDesc(compte,Boolean.TRUE);
            int intNbExamenReussit = lstExamenAnterieurReussit.size();

            //Solde des examens
            int intSoldeExamen =  -1 * ((intNbExamenEchouer * 5) + (intNbExamenReussit * 10));

            List<Combat> lstCombat;

            //à un examen réussit
            if (lstExamenAnterieurReussit.size() >= 1) {
                Long dateDebut = lstExamenAnterieurReussit.get(0).getDate();
                Long dateActuel = Calendar.getInstance().getTime().getTime();
                //lstCombat = combatOad.findByDateLessThanEqualAndDateGreaterThanEqual(dateActuel,dateDebut);
                lstCombat = combatOad.findByDateLessThanEqualAndDateGreaterThanEqualAndAndCmBlancOrCmRouge(dateActuel,dateDebut,compte,compte);
            }
            else {
                lstCombat = combatOad.findByCmBlancOrAndCmRouge(compte,compte);
            }

            //Calcule le nombre de points
            Map<Compte,Integer> mapBlanc = lstCombat.stream()
                    .collect(Collectors.groupingBy(Combat::getCmBlanc, Collectors.summingInt(Combat::getIntGainPertePointBlanc)));

            Map<Compte, Integer> mapRouge = lstCombat.stream()
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
            if (!compte.getGroupe().getGroupe().equals(EnumGroupe.NOIRE) && (intSoldeTotal >= 10) && (intNbPointCombat >= 100 )) {
                //Est dans la honte?
                Examen dernierExamen = examenOad.findFirstByCmJugerOrderByDateDesc(compte);
                boolean booReussit = dernierExamen != null && !dernierExamen.getBooReussit();

                lstCmValidePourExamen.add(new ReponseExamen(compte, intSoldeTotal, booReussit));
            }

            if ((compte.getRole().getRole().ordinal() + 1 < EnumRole.ANCIEN.ordinal() + 1) && (intSoldeTotal >= 10) && intNbCombatArbitrer >= 30) {
                lstCmValidePourPromotion.add(new ReponseExamen(compte,intSoldeTotal));
            }

        });
        Map<String,List<ReponseExamen>> map = new HashMap<>();
        map.put("examen", lstCmValidePourExamen);
        map.put("promotion", lstCmValidePourPromotion);
        return map;
    }

    @PostMapping(value = "/examen")
    public ResponseEntity<Void> passerExamen(@RequestBody ExamenOtd examenOtd) {
        ResponseEntity<Void> valeurRetour = null;
        System.out.println(examenOtd);

        Compte compteJuger = compteOAD.findByCourriel(examenOtd.getJuger());
        Compte compteExaminateur = compteOAD.findByCourriel(examenOtd.getExaminateur());

        Examen examen = new Examen();
        examen.setDate(Calendar.getInstance().getTime().getTime());
        examen.setCmJuger(compteJuger);
        examen.setCmExaminateur(compteExaminateur);
        examen.setBooReussit(examenOtd.getReussit());

        if (!examenOad.save(examen).equals(null)) {
            valeurRetour = ResponseEntity.ok().build();

            if (examenOtd.getReussit()) {
                int intRangCeinture= compteJuger.getGroupe().getGroupe().ordinal();

                EnumGroupe gpSuivant = EnumGroupe.values()[intRangCeinture + 1];
                compteJuger.setGroupe(new Groupe(gpSuivant.ordinal(), gpSuivant));

                compteOAD.save(compteJuger);
            }
        }
        else {
            valeurRetour = ResponseEntity.badRequest().build();
        }

        return valeurRetour;
    }

    @PostMapping(value = "/promotionAncien")
    public ResponseEntity<Void> obtenirPromotionAncien(@RequestBody String strCourriel) {
        ResponseEntity<Void> valeurRetour = null;

        Compte compteFuturAncien = compteOAD.findByCourriel(strCourriel);

        compteFuturAncien.setRole(new Role(EnumRole.ANCIEN.ordinal() + 1,EnumRole.ANCIEN));

        if (!compteOAD.save(compteFuturAncien).equals(null)) {
            valeurRetour = ResponseEntity.ok().build();
        }
        else {
            valeurRetour = ResponseEntity.badRequest().build();
        }

        return valeurRetour;
    }

    @PostMapping(value = "/promotionNoir")
    public ResponseEntity<Void> promotionRoleCeintureNoir(@RequestBody String strCourriel) {
        ResponseEntity<Void> valeurRetour = null;
        Compte comptePromotion = compteOAD.findByCourriel(strCourriel);

        if (comptePromotion != null) {
            comptePromotion.setRole(new Role(EnumRole.SENSEI.ordinal() + 1,EnumRole.SENSEI));
            compteOAD.save(comptePromotion);
            valeurRetour = ResponseEntity.ok().build();
        }
        else {
            valeurRetour = ResponseEntity.badRequest().build();
        }

        return valeurRetour;
    }

    @PostMapping(value = "/destitutionNoir")
    public ResponseEntity<Void> destitutionRoleCeintureNoir(@RequestBody String strCourriel) {
        ResponseEntity<Void> valeurRetour = null;
        Compte compteDestitution = compteOAD.findByCourriel(strCourriel);

        if (compteDestitution != null) {
            compteDestitution.setRole(new Role(EnumRole.ANCIEN.ordinal() + 1,EnumRole.ANCIEN));
            compteOAD.save(compteDestitution);
            valeurRetour = ResponseEntity.ok().build();
        }
        else {
            valeurRetour = ResponseEntity.badRequest().build();
        }

        return valeurRetour;
    }
}
