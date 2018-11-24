package doyonbenoit.projetRPC.evenement;

import doyonbenoit.projetRPC.controleur.ControleurKumite;
import doyonbenoit.projetRPC.entite.SalleCombat;
import doyonbenoit.projetRPC.enumeration.ActionDeplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class EcouteurEvenementStomp implements ApplicationListener<SessionConnectedEvent> {

    private static Logger logger = LoggerFactory.getLogger(EcouteurEvenementStomp.class);

    @Autowired
    private ControleurKumite controleurKumite;

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        //System.out.println("Connexion?");

        if (event.getUser() != null) {
            String strUtilisateurId = event.getUser().getName();
            StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
            boolean isConnect = sha.getCommand() == StompCommand.CONNECT;
            boolean isDisconnect = sha.getCommand() == StompCommand.DISCONNECT;

            /*
            logger.debug("Connect: " + isConnect + ",disconnect:" + isDisconnect + ", event [sessionId: " + sha.getSessionId() + ";" + strUtilisateurId + " ,command="
                    + sha.getCommand());*/

            //System.out.println("Utilisateur: " + strUtilisateurId + " [Entre]");
        }
    }

    @EventListener
    public void onSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if(sha.getUser() != null) {
            //logger.info("[Disonnected] " + sha.getUser().getName());

            ActionDeplacement acDep = SalleCombat.estDansSalle(sha.getUser().getName());
            if (acDep != null) {
                //System.out.println(acDep.toString());
                //System.out.println("Quitte la salle ...");
                controleurKumite.quitterSalle(acDep,sha.getUser().getName());
            }
        }
    }

    @EventListener
    public void onSocketConnected(SessionConnectedEvent event) {

        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if(sha.getUser() != null) {
            //logger.info("[Connected] " + sha.getUser().getName());

            String strTest = "{url=[http://127.0.0.1:8087/kumite], accept-version=[1.1,1.0], heart-beat=[10000,10000]}";


            //System.out.println("[----------------------");
            String strEnTete = pageDeEvenement(event);
            //System.out.println("----------------------]");

            if (strTest.equals(strEnTete)) {
                controleurKumite.enterSalle(sha.getUser().getName().toString());
            }
        }
    }


    //https://github.com/SimlerGray/java-websocket-stomp/blob/master/src/main/java/com/example/config/STOMPConnectEventListener.java
    protected String pageDeEvenement(AbstractSubProtocolEvent event) {
        MessageHeaderAccessor accessor = NativeMessageHeaderAccessor.getAccessor(event.getMessage(), SimpMessageHeaderAccessor.class);
        accessor.getMessageHeaders();
        Object header = accessor.getHeader("simpConnectMessage");
        GenericMessage<?> generic = (GenericMessage<?>) accessor.getHeader("simpConnectMessage");
        //System.out.println(generic.getHeaders().get("nativeHeaders").toString());
        Object nativeHeaders = generic.getHeaders().get("nativeHeaders");
        return nativeHeaders.toString();
    }
}
