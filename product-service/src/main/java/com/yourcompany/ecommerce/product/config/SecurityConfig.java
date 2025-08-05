package com.yourcompany.ecommerce.product.config;

import com.yourcompany.ecommerce.product.config.filter.AuthorizationHeaderFilter; // Import lớp filter
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public AuthorizationHeaderFilter authorizationHeaderFilter() {
        return new AuthorizationHeaderFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().anyRequest().authenticated();

        // Thêm bộ lọc của chúng ta vào trước bộ lọc mặc định
        http.addFilterBefore(authorizationHeaderFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}