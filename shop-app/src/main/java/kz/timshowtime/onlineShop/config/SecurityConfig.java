package kz.timshowtime.onlineShop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/items/add-page", "/items/upload").hasRole("ADMIN")
                        .pathMatchers("/items/**", "/css/**", "/login").permitAll()
                        .anyExchange().authenticated()
                )
                .anonymous(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/items"))
                )
                .logout(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
