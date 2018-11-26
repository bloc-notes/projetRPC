package doyonbenoit.projetRPC.configuration;

import doyonbenoit.projetRPC.securite.Utilisateur;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpAsyncRequestControl;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        registry.addEndpoint("/webSocket").setAllowedOrigins("http://localhost:8087").withSockJS().setInterceptors(new HandshakeInterceptor() {
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
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

                /*
                if (StompCommand.SUBSCRIBE == accessor.getCommand() && (accessor.getDestination().equals("/sujet/reponsePrive"))) {
                    System.out.println("[Inscription Privée]---");

                    Authentication authentication = (Authentication) accessor.getUser();
                    Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
                    System.out.println(utilisateur.getUsername());
                }*/

                if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
                    System.out.println("[Inscription]-------");

                    Authentication authentication = (Authentication) accessor.getUser();

                    //System.out.println(authentication);

                    if (authentication != null && authentication.getPrincipal() instanceof Utilisateur) {
                        //System.out.println("AUTHENTIFIER");

                        Authentication authentication2 = (Authentication) accessor.getUser();
                        Utilisateur utilisateur = (Utilisateur) authentication2.getPrincipal();
                        Boolean booType = accessor.getDestination().equals("/sujet/reponsePrive");
                        System.out.println(utilisateur.getUsername() +  " s'est abonné aux messages " + (booType ? "privés": "publics") );

                    }
                    else  {
                        System.out.println("PAS AUTHENTIFIER");
                    }
                }


                //System.out.println("En-tete message: " + message.getHeaders());

                System.out.println("-------------Fin--------------");
                return message;
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                return message;
            }
        });
    }
}
