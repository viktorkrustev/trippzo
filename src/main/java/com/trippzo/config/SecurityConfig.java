package com.trippzo.config;

import com.trippzo.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests.requestMatchers("/static/**", "/css/**", "/js/**", "/img/**").permitAll()
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers("/", "/index", "/login", "/register", "/trips/search", "/locale", "/trips/{id}")
                            .permitAll().requestMatchers("/api/**", "/ws-chat/**").permitAll()
                            .requestMatchers("/chat/unread/count", "/notifications/unread/count").authenticated()
                            .anyRequest().authenticated();
                }).formLogin(formLogin -> {
                    formLogin.loginPage("/login").usernameParameter("email").passwordParameter("password")
                            .defaultSuccessUrl("/", true).failureUrl("/login?error=true").permitAll();
                }).oauth2Login(oauth2 -> oauth2.loginPage("/login").defaultSuccessUrl("/oauth2/success", true))
                .logout(logout -> {
                    logout.logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true)
                            .clearAuthentication(true).deleteCookies("JSESSIONID").permitAll();
                }).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
