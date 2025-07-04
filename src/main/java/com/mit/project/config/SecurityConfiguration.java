package com.mit.project.config;

import com.mit.project.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends VaadinWebSecurity {

  @Autowired private CustomSuccessHandler customSuccessHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers("/public/**")
                .permitAll()
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")
                .requestMatchers("/user/**")
                .hasRole("USER"));

    http.formLogin(
        form ->
            form.loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler)
                .permitAll());

    super.configure(http);

    setLoginView(http, LoginView.class);
  }

  @Bean
  public UserDetailsManager userDetailsService() {
    UserDetails user =
        User.withUsername("devashish@gmail.com").password("{noop}user").roles("USER").build();

    UserDetails admin =
        User.withUsername("mit-facilities@gmail.com")
            .password("{noop}mit-facilities")
            .roles("ADMIN")
            .build();

    UserDetails parthUser =
        User.withUsername("parth.wagh@gmail.com").password("{noop}user").roles("USER").build();

    UserDetails ManjiriiUser = User.withUsername("manjirii.manrne@gmail.com").password("{noop}user").roles("USER").build();
    return new InMemoryUserDetailsManager(user, admin, parthUser,ManjiriiUser);
  }
}

