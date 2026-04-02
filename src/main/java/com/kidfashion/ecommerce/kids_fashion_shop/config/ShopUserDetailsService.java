package com.kidfashion.ecommerce.kids_fashion_shop.config;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.AppUserRepository;

@Service
public class ShopUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public ShopUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username == null) {
			throw new UsernameNotFoundException("Email trống.");
		}
		Optional<AppUser> u = this.appUserRepository.findByEmailIgnoreCase(username.trim());
		if (u.isEmpty()) {
			throw new UsernameNotFoundException("Không tìm thấy tài khoản.");
		}
		AppUser user = u.get();
		return new ShopUserDetails(user);
	}
}
