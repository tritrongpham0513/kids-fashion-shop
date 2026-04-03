package com.kidfashion.ecommerce.kids_fashion_shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.kidfashion.ecommerce.kids_fashion_shop.service.CartPersistenceService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationSuccessHandler loginSuccessHandler(CartPersistenceService cartPersistenceService) {
		return (HttpServletRequest request, HttpServletResponse response,
				org.springframework.security.core.Authentication authentication) -> {
			if (authentication != null && authentication.getPrincipal() instanceof ShopUserDetails details) {
				try {
					cartPersistenceService.mergeSessionCartIntoUserCart(request.getSession(true),
							details.getAppUser().getId());
				} catch (Exception ex) {
					log.warn("Gộp giỏ phiên vào DB sau đăng nhập thất bại: {}", ex.toString());
				}
			}
			redirectByRole(request, response, authentication);
		};
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler loginSuccessHandler,
		DaoAuthenticationProvider authenticationProviderBean,
		OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
		ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) throws Exception {
		http.authenticationProvider(authenticationProviderBean);
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/images/**", "/error", "/oauth2/**", "/login/oauth2/**")
				.permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/checkout/**", "/account/**")
				.authenticated().anyRequest().permitAll());
		// Bật CSRF bằng cookie để vẫn có _csrf trong view nhưng không phụ thuộc session
		http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
		http.formLogin(form -> form.loginPage("/login").permitAll().successHandler(loginSuccessHandler));
		if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
			http.oauth2Login(oauth -> oauth.loginPage("/login").successHandler(oAuth2LoginSuccessHandler));
		}
		http.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());
		return http.build();
	}

	private void redirectByRole(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.Authentication authentication) throws java.io.IOException {
		boolean isAdmin = false;
		for (org.springframework.security.core.GrantedAuthority ga : authentication.getAuthorities()) {
			if ("ROLE_ADMIN".equals(ga.getAuthority())) {
				isAdmin = true;
				break;
			}
		}
		String ctx = request.getContextPath();
		if (isAdmin) {
			response.sendRedirect(ctx + "/admin");
		} else {
			response.sendRedirect(ctx + "/");
		}
	}
}
