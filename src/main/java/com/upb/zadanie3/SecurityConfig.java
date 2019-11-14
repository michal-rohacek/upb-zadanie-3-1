package com.upb.zadanie3;

import com.upb.zadanie3.database.user.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    };

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new CryptologicPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return new DelayedAuthenticationFailureHandler();
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(getPasswordEncoder());

        return authProvider;
    }

    private AuthenticationEventPublisher publisher() {
        return new CustomAuthenticationEventPublisher();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(getAuthenticationProvider());
        auth.authenticationEventPublisher(publisher());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/create-user").permitAll()
                .antMatchers( "/public/**").permitAll()
                .antMatchers( "/sign-error").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/sign").permitAll()
                .loginProcessingUrl("/sign-user")
                .defaultSuccessUrl("/index",true)
                .failureHandler(getAuthenticationFailureHandler())

                //these options are required to access H2 Console
                .and().headers().frameOptions().disable()
                .and().csrf().disable()

                .logout();

    }
}