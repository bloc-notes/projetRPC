package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
//import doyon.projetRPCA.entite.*;
import doyonbenoit.projetRPC.securite.Utilisateur;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Examen;
import doyonbenoit.projetRPC.entite.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ControleurMVC {

    @Autowired
    private CompteOad compteOAD;

    @Autowired
    ExamenOad examenOad;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Map<String, Object> model) {
        return "identification";
    }

    @GetMapping(value = "/inscription")
    public String inscription(Map<String, Object> model) {
        return "publique/creationCompte";
    }

    @GetMapping(value="/")
    public String dojo(Map<String, Object> model){
        Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String strAlias = "Visiteur";
        String strRole = "Non-connecté";
        String strCeinture = "---";
        String strImage = "/images/profile-vide.png";

        if (objPrincipal instanceof Utilisateur) {
            Compte compte = ((Utilisateur) objPrincipal).getCompte();
            Compte compteMAJ = compteOAD.findByCourriel(compte.getCourriel());
            strAlias = compteMAJ.getAlias();
            strRole = compteMAJ.getRole().name();
            strCeinture = compteMAJ.getGroupe().name();
            strImage = compteMAJ.getAvatar().getImgAvatar();
        }

        model.put("Alias",strAlias);
        model.put("Role", strRole);
        model.put("Ceinture", strCeinture);
        model.put("ImageProfile", strImage);

        return "publique/dojo";
    }

    @GetMapping(value = "/NotreEcole")
    public String lstMembre(Map<String, Object> model)
    {
        List<Compte> lstProf = compteOAD.findByRole(Role.SENSEI);
        List<Compte> lstNouveau = compteOAD.findByRole(Role.NOUVEAU);
        List<Compte> lstAncient = compteOAD.findByRole(Role.ANCIEN);

        List<Compte> venerable = compteOAD.findByRole(Role.VENERABLE);

        //Détermine les combatants de la honte et les exclus ...
        List<Compte> lstHonte = new ArrayList<>();
        List<Compte> lstHonteAncient =  lstAncient.stream().filter(combatant -> examenOad.findByCmJugerOrderByDateDesc(combatant).getBooReussit() == true).collect(Collectors.toList());
        List<Compte> lstHonteNouveau =  lstNouveau.stream().filter(combatant -> {
            Optional<Examen> opExam = Optional.ofNullable(examenOad.findByCmJugerOrderByDateDesc(combatant));
            if (opExam.isPresent()) {
                return !opExam.get().getBooReussit();
            }
            else {
                return false;
            }
        }).collect(Collectors.toList());

        lstNouveau.removeAll(lstHonteNouveau);
        lstAncient.removeAll(lstHonteAncient);

        lstHonte.addAll(lstHonteNouveau);
        lstHonte.addAll(lstHonteAncient);

        Map<String, List<Compte>> ecole = new HashMap<>();

        ecole.put("Nos nouveaux", lstNouveau);
        ecole.put("Nos anciens", lstAncient);
        ecole.put("Nos hontes", lstHonte);
        ecole.put("Nos professeurs",lstProf);
        ecole.put("Notre vénérable", venerable);

        model.put("ecole",ecole);

        return "publique/membre";
    }

    @GetMapping(value = "/PassageGrade")
    public String examen(Map<String, Object> model) {
        Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Compte compteProfile = null;
        Compte compteProfileMAJ = null;

        if (objPrincipal instanceof Utilisateur) {
            compteProfile = ((Utilisateur) objPrincipal).getCompte();
            compteProfileMAJ = compteOAD.findByCourriel(compteProfile.getCourriel());

        }

        model.put("profile", compteProfileMAJ);

        return "publique/passageGrade";
    }
}


