package com.example.sneakerstorebackend.config;

import com.example.sneakerstorebackend.security.jwt.AuthEntryPointJwt;
import com.example.sneakerstorebackend.security.jwt.JwtFilter;
import com.example.sneakerstorebackend.security.oauth.CustomOAuth2UserService;
import com.example.sneakerstorebackend.security.oauth.Failure;
import com.example.sneakerstorebackend.security.oauth.Success;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {
    private final JwtFilter jwtFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final Success successHandler;

    private final String[] ALLOWED_LIST_URLS = {
            "/api/auth/**",
            "/api/oauth/**",
            "/oauth2/**",
            "/login/**",
            // SwaggerUI
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/ws/**",
            //Paypal
            "/api/checkout/*/success",
            "/api/checkout/*/cancel",
            //GHN
            "/api/shipping/**",
    };

    private final String[] ALLOWED_GET_LIST_URLS = {
            "/api/products/**",
            "/api/categories/**",
            "/api/brands/**",
            "/api/reviews/**",
    };

    @Value("${app.allow.origin}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers(ALLOWED_LIST_URLS).permitAll().and()
                .authorizeRequests().antMatchers(HttpMethod.GET, ALLOWED_GET_LIST_URLS).permitAll().and()
                .authorizeRequests().antMatchers("/api/admin/manage/**")
                .hasAuthority(ConstantsConfig.ROLE_ADMIN).and()
                .authorizeRequests().antMatchers("/api/manage/**")
                .hasAnyAuthority(ConstantsConfig.ROLE_STAFF, ConstantsConfig.ROLE_ADMIN).and()
                .authorizeRequests().antMatchers("/api/**")
                .hasAnyAuthority(ConstantsConfig.ROLE_USER,ConstantsConfig.ROLE_STAFF, ConstantsConfig.ROLE_ADMIN)
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(oAuth2UserService)
                .and()
                .successHandler(successHandler)
                .failureHandler(authenticationFailureHandler());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public AuthenticationManager authenticationManager (
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "origin", "x-request-with", "accept"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new Failure();
    }

    @Bean
    FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        final FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
