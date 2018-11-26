package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.OAD.ExamenOad;
import doyonbenoit.projetRPC.entite.Role;
import doyonbenoit.projetRPC.securite.Utilisateur;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Examen;
import doyonbenoit.projetRPC.enumeration.EnumRole;
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
            strRole = compteMAJ.getRole().getRole().name();
            strCeinture = compteMAJ.getGroupe().getGroupe().name();
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
        List<Compte> lstProf = compteOAD.findByRole(new Role(EnumRole.SENSEI.ordinal() + 1,EnumRole.SENSEI));
        List<Compte> lstNouveau = compteOAD.findByRole(new Role(EnumRole.NOUVEAU.ordinal() + 1,EnumRole.NOUVEAU));
        List<Compte> lstAncient = compteOAD.findByRole(new Role(EnumRole.ANCIEN.ordinal()+ 1,EnumRole.ANCIEN));

        List<Compte> venerable = compteOAD.findByRole(new Role(EnumRole.VENERABLE.ordinal() + 1,EnumRole.VENERABLE));

        //Détermine les combatants de la honte et les exclus ...
        List<Compte> lstHonte = new ArrayList<>();
        List<Compte> lstHonteAncient =  lstAncient.stream().filter(combatant -> !examenOad.findFirstByCmJugerOrderByDateDesc(combatant).getBooReussit()).collect(Collectors.toList());
        List<Compte> lstHonteNouveau =  lstNouveau.stream().filter(combatant -> {
            Optional<Examen> opExam = Optional.ofNullable(examenOad.findFirstByCmJugerOrderByDateDesc(combatant));

            return opExam.isPresent() && !opExam.get().getBooReussit();
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


