package org.cjs.brewery.eureka.msscbreweryeureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * This config disables CSRF, which is required for Eureka Server, and enables HTTP Basic authentication
 */
@Configuration
public class SecurityConfig {

    // the original config that overrides WebSecurityConfigurerAdapter. No longer recommended
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeRequests()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();
//    }

    /**
     * The new, recommended approach, to configuring Http Security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests((authz) -> authz
                        .antMatchers("/actuator/health").permitAll()
                        .antMatchers("/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());
        return http.build();
    }
}
