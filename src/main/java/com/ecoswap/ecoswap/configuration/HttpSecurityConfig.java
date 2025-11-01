package com.ecoswap.ecoswap.configuration;

import com.ecoswap.ecoswap.configuration.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class HttpSecurityConfig {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementCustomizer -> sessionManagementCustomizer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> {

                    // Swagger UI v3 (OpenAPI)
                    authorize.requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/swagger-resources/**",
                            "/swagger-resources",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml",
                            "/webjars/**",
                            "/actuator/health",
                            "/configuration/**").permitAll();

                    // Permitir solicitudes OPTIONS para CORS
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    authorize.requestMatchers(HttpMethod.GET, "/images/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();

                    authorize.requestMatchers(HttpMethod.POST, "/auth/register").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/product/category/{category}").permitAll();

                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/product/create").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/product/user").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/product/active/user/{userId}").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/product/{id}").permitAll();
                    authorize.requestMatchers(HttpMethod.PUT, "/product/{id}").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/product/{id}").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "api/v1/product/recent").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "api/v1/products/counts").permitAll();

                    authorize.requestMatchers(HttpMethod.GET, "api/v1/product/**").permitAll();

                    authorize.requestMatchers(HttpMethod.GET, "/images/**").permitAll();

                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/create-exchange").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/create-exchange-existing-product").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/select-exchange").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/exchanges").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/exchanges/counts").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/exchanges").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/completed/user/{userId}").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/{exchangeId}/confirm").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/{exchangeId}/cancel").permitAll();


                    authorize.requestMatchers(HttpMethod.GET,"/api/v1/user").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/user/me").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/users/count").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll();
                    authorize.requestMatchers(HttpMethod.DELETE, "/api/v1/user/{id}").permitAll();
                    authorize.requestMatchers(HttpMethod.PUT, "/api/v1/user/{id}").permitAll();

                    authorize.requestMatchers(HttpMethod.POST, "/api/notifications/send").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/notifications/unread/{userId}").permitAll();
                    authorize.requestMatchers(HttpMethod.GET,"/ws/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST,"/ws/**").permitAll();

                    authorize.requestMatchers(HttpMethod.GET,"/ws").permitAll();
                    authorize.requestMatchers(HttpMethod.POST,"/ws").permitAll();

                    authorize.requestMatchers(HttpMethod.GET,"/ws/**", "/topic/**", "/app/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST,"/ws/**", "/topic/**", "/app/**").permitAll();

                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/chat/message/exchange/{receiverId}").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/chat/message/create").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/chat/message").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/message/exchange/{exchangeId}").permitAll();


                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/prediction").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/prediction-manual").permitAll();






                    // Rutas del AI Assistant
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/ai/assistant").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/chat/assistant").permitAll();
                    authorize.requestMatchers(HttpMethod.GET, "/api/v1/ai/suggestions/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/ai/quick-help").permitAll();

                    // Rutas de asistente de intercambio
                    authorize.requestMatchers(HttpMethod.GET, "/chat").permitAll();
                    // authorize.requestMatchers(HttpMethod.GET, "/chat/search-products").permitAll();

                    // Rutas de sesiones de chat
                    // authorize.requestMatchers(HttpMethod.POST, "/chat/session/new").permitAll();
                    // authorize.requestMatchers(HttpMethod.POST, "/chat/session/*/message").permitAll();
                    // authorize.requestMatchers(HttpMethod.GET, "/chat/session/*/history").permitAll();
                    // authorize.requestMatchers(HttpMethod.POST, "/chat/session/*/close").permitAll();

                    // authorize.requestMatchers(HttpMethod.POST, "/api/v1/create").hasAuthority(Permission.SAVE_ONE_PRODUCT.name());

                    authorize.anyRequest().denyAll();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
