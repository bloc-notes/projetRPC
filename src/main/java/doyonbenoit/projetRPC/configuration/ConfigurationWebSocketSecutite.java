package doyonbenoit.projetRPC.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class ConfigurationWebSocketSecutite extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpMessageDestMatchers("/app/messagepub").hasAnyAuthority("ANCIEN","VENERABLE","SENSEI")
                .simpSubscribeDestMatchers("/sujet/reponsepriv").authenticated();
    }
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
