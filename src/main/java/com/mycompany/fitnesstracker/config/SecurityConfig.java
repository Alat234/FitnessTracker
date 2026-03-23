package com.mycompany.fitnesstracker.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
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
                .cors(org.springframework.security.config.Customizer.withDefaults())
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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                .logoutUrl("/api/auth/logout") // URL для запиту з фронтенду
                .addLogoutHandler((request, response, authentication) -> {
                    // Тут можна додати логіку, якщо ти захочеш заносити токен у чорний список
                    // Але поки що достатньо очистити контекст
                    SecurityContextHolder.clearContext();
                })
                .logoutSuccessHandler((request, response, authentication) -> {
                    Cookie cookie = new Cookie("jwt", null);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(0); // Кажемо браузеру видалити негайно
                    // Якщо використовуєш HTTPS, додай: cookie.setSecure(true);
                    response.addCookie(cookie);

                    response.setStatus(HttpServletResponse.SC_OK);
                })
        );




        return http.build();

    }
}
