package com.mycompany.fitnesstracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception {
        http
                // 1. Новий синтаксис вимкнення CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Новий синтаксис для авторизації запитів (блок лямбда)
                .authorizeHttpRequests(auth -> auth
                        // Вкажіть тут ваші публічні ендпоінти
                        .requestMatchers("/api/auth/**").permitAll()
                        // Всі інші запити вимагають аутентифікації
                        .anyRequest().authenticated()
                )

                // 3. Новий синтаксис для сесій (блок лямбда)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Провайдер і фільтр додаються без .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }
}
