package doyonbenoit.projetRPC.configuration;

import doyonbenoit.projetRPC.entite.Compte;
import doyonbenoit.projetRPC.securite.Utilisateur;
import doyonbenoit.projetRPC.securite.UtilisateurDetailService;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ConfiguraionSecuriteWeb extends WebSecurityConfigurerAdapter {

    @Autowired
    private UtilisateurDetailService monUtilisateurDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(monUtilisateurDetailsService);
    }


    //Définir les méthodes qui permettront l’identification monUserDetailsService et le cryptage des mots de passe.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(monUtilisateurDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    //Définir la méthode de cryptage des mots de passe
    //https://en.wikipedia.org/wiki/Bcrypt
    //La valeur par defaut est 10
    //https://docs.spring.io/spring-security/site/docs/4.2.7.RELEASE/apidocs/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html
    //https://www.browserling.com/tools/bcrypt

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //permettre toutes les requêtes
        http.authorizeRequests()
                .antMatchers("/Compte/tout", "/Combat/Historique/**").permitAll()
                .antMatchers("/kumite/**","/Compte/**", "/PassageGrade/Mobile/**", "/Combat/**").authenticated()
                .antMatchers("/PassageGrade/**").hasAnyAuthority("SENSEI","VENERABLE")
                .and()
                .csrf()
                .ignoringAntMatchers("/Compte/**", "/PassageGrade/**", "/login", "/connexion", "/logout")
                //Activer le formulaire pour login
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/connexion")
                .permitAll()
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        UserAgentStringParser agent = UADetectorServiceFactory.getResourceModuleParser();
                        String infoAgent = request.getHeader("User-Agent");

                        ReadableUserAgent ruAgent = agent.parse(infoAgent);

                        Compte compte = ((Utilisateur) authentication.getPrincipal()).getCompte();
                        Utilisateur utilisateur = ((Utilisateur) authentication.getPrincipal());

                        //System.out.println(request.getHeader("User-Agent"));
                        //System.out.println(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE);
                        System.out.println("CONNEXION ---");
                        System.out.println("[" + utilisateur.getUsername() + "] => " +  ruAgent.getName().split(" ")[0] + " : " + ruAgent.getDeviceCategory().getName());
                        System.out.println("-------------");

                        //response.sendRedirect("");
                        if (ruAgent.getType().getName().equalsIgnoreCase("Browser")) {
                            response.sendRedirect("");
                        }
                        else {
                            //android reste a déterminer (peut etre rien)
                        }
                    }
                })
                .failureUrl("/login?error=true")
                .and()
                .logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                //.logoutSuccessUrl("/login")
                .logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        UserAgentStringParser agent = UADetectorServiceFactory.getResourceModuleParser();
                        String infoAgent = request.getHeader("User-Agent");
                        ReadableUserAgent ruAgent = agent.parse(infoAgent);

                        Compte compte = ((Utilisateur) authentication.getPrincipal()).getCompte();
                        Utilisateur utilisateur = ((Utilisateur) authentication.getPrincipal());

                        System.out.println("Déconnexion : " + utilisateur.getUsername());

                        if (ruAgent.getType().getName().equalsIgnoreCase("Browser")) {
                            response.sendRedirect("/login?logout");
                        }
                    }
                })
                //configuration spécifique pour la console H2
                .and()
                .csrf()
                .ignoringAntMatchers("/consoleBD/**")
                .and()
                .headers()
                .frameOptions()
                .sameOrigin();
    }


}
