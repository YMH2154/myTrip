package com.soloProject.myTrip.config;

import com.soloProject.myTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

        private final MemberService memberService;
        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                                .requestMatchers("/", "/member/**", "/item/**", "/images/**", "/itemImages/**",
                                                "/email/**", "/api/prices/status/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/reservation/**").authenticated()
                                .anyRequest().authenticated()).formLogin(login -> login
                                                .loginPage("/member/login")
                                                .defaultSuccessUrl("/member/login/success")
                                                .failureUrl("/member/login/error")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
                                                .logoutSuccessUrl("/"))
                                .oauth2Login(oauthLogin -> oauthLogin
                                                .defaultSuccessUrl("/")
                                                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                                                .userService(customOAuth2UserService)))
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**")
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

                http.exceptionHandling(exception -> exception
                                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));

                return http.build();
        }

        @Bean
        public static PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Autowired
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
        }
}
