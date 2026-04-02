package com.kidfashion.ecommerce.kids_fashion_shop.config;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShopUserDetails implements UserDetails {

	private final AppUser appUser;

	public ShopUserDetails(AppUser appUser) {
		this.appUser = appUser;
	}

	public AppUser getAppUser() {
		return this.appUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		String r = "ROLE_" + this.appUser.getRole().name();
		list.add(new SimpleGrantedAuthority(r));
		return list;
	}

	@Override
	public String getPassword() {
		return this.appUser.getPassword();
	}

	@Override
	public String getUsername() {
		return this.appUser.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean isAdmin() {
		return this.appUser.getRole() == Role.ADMIN;
	}
}
