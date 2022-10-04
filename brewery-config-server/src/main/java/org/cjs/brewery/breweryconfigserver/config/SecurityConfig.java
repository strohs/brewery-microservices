//package org.cjs.brewery.breweryconfigserver.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//
///**
// *
// */
//@Configuration
//public class SecurityConfig {
//
//    /**
//     * This config could be used to open the /encrypt  and  /decrypt endpoints so that
//     * Basic Auth is not needed
//     */
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/encrypt", "/decrypt").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();
//        return http.build();
//    }
//}
