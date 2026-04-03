package com.kidfashion.ecommerce.kids_fashion_shop.config;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.service.AppUserService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartPersistenceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

	private final AppUserService appUserService;
	private final CartPersistenceService cartPersistenceService;

	public OAuth2LoginSuccessHandler(AppUserService appUserService, CartPersistenceService cartPersistenceService) {
		this.appUserService = appUserService;
		this.cartPersistenceService = cartPersistenceService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof OAuth2User oauth2User)) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		Map<String, Object> attrs = oauth2User.getAttributes();
		String email = asText(attrs.get("email"));
		String name = asText(attrs.get("name"));
		if (email == null || email.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/login?error");
			return;
		}

		AppUser appUser = this.appUserService.findOrCreateGoogleCustomer(email, name);
		ShopUserDetails details = new ShopUserDetails(appUser);
		UsernamePasswordAuthenticationToken localAuth = new UsernamePasswordAuthenticationToken(details, null,
				details.getAuthorities());
		localAuth.setDetails(authentication.getDetails());
		SecurityContextHolder.getContext().setAuthentication(localAuth);

		try {
			this.cartPersistenceService.mergeSessionCartIntoUserCart(request.getSession(true), appUser.getId());
		} catch (Exception ex) {
			log.warn("Gộp giỏ phiên vào DB sau đăng nhập Google thất bại: {}", ex.toString());
		}
		response.sendRedirect(request.getContextPath() + "/");
	}

	private String asText(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}
}
