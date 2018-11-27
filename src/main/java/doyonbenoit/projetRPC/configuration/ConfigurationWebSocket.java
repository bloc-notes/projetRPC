package doyonbenoit.projetRPC.configuration;

import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.entite.SalleCombatAndroid;
import doyonbenoit.projetRPC.enumeration.EnumRole;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class ConfigurationWebSocket  implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sujet","/kumite");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/webSocket").setAllowedOrigins("*").withSockJS().setInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                System.out.println("----------------Avant-------------");
                System.out.println(request.getHeaders());

                if (request instanceof ServletServerHttpRequest) {

                }

                System.out.println("----------------Fin--------------");
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            }
        });
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(200000); // default : 64 * 1024
        registration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
        registration.setSendBufferSizeLimit(3* 512 * 1024); // default : 512 * 1024
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                System.out.println("-------------Canal--------------");
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                System.out.println("En-tete message: " + message.getHeaders());


                if (StompCommand.SEND == accessor.getCommand()) {
                    Authentication authentication = (Authentication) accessor.getUser();
                    System.out.println("?");
                    if (authentication != null && authentication.getPrincipal() instanceof Utilisateur) {
                        System.out.println("??");
                        Authentication authentication2 = (Authentication) accessor.getUser();
                        Utilisateur utilisateur = (Utilisateur) authentication2.getPrincipal();
                        Compte compte = utilisateur.getCompte();
                        System.out.println(compte.getRole().getRole().ordinal() + "_" + EnumRole.NOUVEAU.ordinal());
                        if (accessor.getDestination().equalsIgnoreCase("/app/messagePub") && (compte.getRole().getRole().ordinal() == EnumRole.NOUVEAU.ordinal())) {
                            System.out.println("ok?");
                            Message<doyonbenoit.projetRPC.entite.Message> stringMessage = MessageBuilder.createMessage(new doyonbenoit.projetRPC.entite.Message(compte.getCourriel(), "", "Pas autorisation", "Publique"), message.getHeaders());
                            message = stringMessage;
                        }


                    } else {
                        //peut pas envoyer de message car pas connecter
                        if (accessor.getDestination().equalsIgnoreCase("/app/messagePub")) {
                            Message<doyonbenoit.projetRPC.entite.Message> stringMessage = MessageBuilder.createMessage(new doyonbenoit.projetRPC.entite.Message("-", "", "Pas autorisation", "Publique"), message.getHeaders());
                            message = stringMessage;
                        }
                        else {
                            Message<doyonbenoit.projetRPC.entite.Message> stringMessage = MessageBuilder.createMessage(new doyonbenoit.projetRPC.entite.Message("-", "", "Pas autorisation", "Prive"), message.getHeaders());
                            message = stringMessage;
                        }


                    }
                }

                if (StompCommand.DISCONNECT == accessor.getCommand()) {
                    Authentication authentication = (Authentication) accessor.getUser();
                    System.out.println("user va disconnect");
                    if (authentication != null && authentication.getPrincipal() instanceof Utilisateur) {
                        System.out.println("user SE disconnect");
                        Authentication authentication2 = (Authentication) accessor.getUser();
                        Utilisateur utilisateur = (Utilisateur) authentication2.getPrincipal();
                        if (SalleCombatAndroid.lstArbitre.contains(utilisateur.getUsername())) {
                            SalleCombatAndroid.lstArbitre.remove(utilisateur.getUsername());
                        }
                        if (SalleCombatAndroid.lstAilleur.contains(utilisateur.getUsername())) {
                            SalleCombatAndroid.lstAilleur.remove(utilisateur.getUsername());
                        }
                        if (SalleCombatAndroid.lstSpectateur.contains(utilisateur.getUsername())) {
                            SalleCombatAndroid.lstSpectateur.remove(utilisateur.getUsername());
                        }
                        if (SalleCombatAndroid.lstAttente.contains(utilisateur.getUsername())) {
                            SalleCombatAndroid.lstAttente.remove(utilisateur.getUsername());
                        }
                        System.out.println(SalleCombatAndroid.lstArbitre.toString());
                        System.out.println(SalleCombatAndroid.lstAilleur.toString());
                        System.out.println(SalleCombatAndroid.lstSpectateur.toString());
                        System.out.println(SalleCombatAndroid.lstAttente.toString());

                    }
                }

                System.out.println("-------------Fin--------------");
                return message;
            }
        });
    }
}