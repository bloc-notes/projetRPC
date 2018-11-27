package doyonbenoit.projetRPC.controleur;

import doyonbenoit.projetRPC.OAD.CompteOad;
import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.Message;
import doyonbenoit.projetRPC.entite.Reponse;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ControleurMessage {

    @Autowired
    CompteOad compteOad;

    @MessageMapping("/messagePub")
    @SendTo("/sujet/reponsePublique")
    public HashMap<String, Object> messagePub(Message message) throws Exception {
        //Compte compte = compteOad.findByCourriel(message.getDe());
        HashMap<String, Object> mapReponse = new HashMap<>();
        mapReponse.put("id", message.getDe());
        mapReponse.put("date", dateCreation());
        mapReponse.put("contenu", message.getContenu());

        return mapReponse;
    }

    @MessageMapping("/messagePri")
    @SendTo("/sujet/reponsePrive")
    public HashMap<String, Object> messagePri(Message message) throws Exception {
        //Compte compte = compteOad.findByCourriel(message.getDe());
        HashMap<String, Object> mapReponse = new HashMap<>();
        mapReponse.put("id", message.getDe());
        mapReponse.put("date", dateCreation());
        mapReponse.put("contenu", message.getContenu());

        return mapReponse;
    }


    @GetMapping(value = "/file")
    public String messagerie(Map<String, Object> model) {
        Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String strRole = "";
        String strImg = "";
        String strCourriel = "";
        if (objPrincipal instanceof Utilisateur) {
            Compte compte = ((Utilisateur) objPrincipal).getCompte();
            strRole = compte.getRole().getRole().name();
            strImg = compte.getAvatar().getImgAvatar();
            strCourriel = compte.getCourriel();
        }

        model.put("Role", strRole);
        model.put("Image", strImg);
        model.put("Courriel", strCourriel);

        return "publique/fileMessage";
    }

    private String dateCreation(){
        DateFormat df = new SimpleDateFormat("EEEE dd MMMM HH:mm:ss");
        Date date = Calendar.getInstance().getTime();
        return df.format(date);
    }

}
