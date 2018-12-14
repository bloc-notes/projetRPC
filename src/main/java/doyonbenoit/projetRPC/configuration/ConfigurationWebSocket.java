package doyonbenoit.projetRPC.configuration;

import doyonbenoit.projetRPC.entite.SalleCombatAndroid;
import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
        registry.addEndpoint("/webSocket").setAllowedOrigins("*").withSockJS();
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