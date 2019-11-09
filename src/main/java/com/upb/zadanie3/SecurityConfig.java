package com.upb.zadanie3;

import com.upb.zadanie3.user.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    };

    //TODO
    //tuto treba asi integrovat ten password hashing a tie veci okolo, do nejakej classy co implementuje PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new NajlepsiNaCelejFEIkePasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new DelayedAuthenticationFailureHandler();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/create-user").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/sign").permitAll()
                .loginProcessingUrl("/sign-user")
                .defaultSuccessUrl("/index",true)
                .failureHandler(authenticationFailureHandler())
//                .failureUrl("/sign.html?error=true")

                //these options are required to access H2 Console
                .and().headers().frameOptions().disable()
                .and().csrf().disable()
                .logout();

    }
}