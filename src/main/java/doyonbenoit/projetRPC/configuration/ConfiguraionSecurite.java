package doyonbenoit.projetRPC.configuration;

import doyonbenoit.projetRPC.securite.UtilisateurDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ConfiguraionSecurite extends WebSecurityConfigurerAdapter {

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
                .antMatchers("/kumite/**").authenticated()
                .antMatchers("/PassageGrade/**").hasAnyAuthority("SENSEI","VENERABLE")
                .and()
                .csrf()
                .ignoringAntMatchers("/Compte/**", "/PassageGrade/**")
                //Activer le formulaire pour login
                .and()
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .and()
                .logout()
                //.logoutUrl("/logout")
                //.logoutSuccessUrl("/")
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
