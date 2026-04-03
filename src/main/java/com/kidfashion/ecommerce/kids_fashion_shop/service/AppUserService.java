package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Role;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppUserService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<AppUser> findById(Long id) {
		return this.appUserRepository.findById(id);
	}

	public Optional<AppUser> findByEmail(String email) {
		if (email == null) {
			return Optional.empty();
		}
		return this.appUserRepository.findByEmailIgnoreCase(email.trim());
	}

	public List<AppUser> findCustomersOnly() {
		return this.appUserRepository.findByRoleOrderByFullNameAsc(Role.CUSTOMER);
	}

	@Transactional
	public AppUser registerCustomer(String email, String rawPassword, String fullName, String phone, String address) {
		AppUser user = new AppUser();
		user.setEmail(email.trim().toLowerCase());
		user.setPassword(this.passwordEncoder.encode(rawPassword));
		user.setFullName(fullName);
		user.setPhone(phone);
		user.setAddress(address);
		user.setRole(Role.CUSTOMER);
		return this.appUserRepository.save(user);
	}

	@Transactional
	public AppUser findOrCreateGoogleCustomer(String email, String fullName) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email Google không hợp lệ.");
		}
		String normalizedEmail = email.trim().toLowerCase();
		Optional<AppUser> existing = this.appUserRepository.findByEmailIgnoreCase(normalizedEmail);
		if (existing.isPresent()) {
			AppUser user = existing.get();
			if ((user.getFullName() == null || user.getFullName().isBlank()) && fullName != null && !fullName.isBlank()) {
				user.setFullName(fullName.trim());
				return this.appUserRepository.save(user);
			}
			return user;
		}

		AppUser user = new AppUser();
		user.setEmail(normalizedEmail);
		user.setFullName((fullName == null || fullName.isBlank()) ? normalizedEmail : fullName.trim());
		// Vẫn lưu password hash để tương thích cột NOT NULL và các flow hiện có.
		user.setPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));
		user.setRole(Role.CUSTOMER);
		return this.appUserRepository.save(user);
	}

	@Transactional
	public AppUser save(AppUser user) {
		return this.appUserRepository.save(user);
	}

	/**
	 * Cập nhật họ tên, điện thoại, địa chỉ (email không đổi tại đây).
	 */
	@Transactional
	public AppUser updateProfile(Long userId, String fullName, String phone, String address) {
		Optional<AppUser> opt = this.appUserRepository.findById(userId);
		if (opt.isEmpty()) {
			throw new IllegalStateException("Không tìm thấy người dùng.");
		}
		AppUser u = opt.get();
		if (fullName == null || fullName.isBlank()) {
			throw new IllegalArgumentException("Họ tên không được để trống.");
		}
		u.setFullName(fullName.trim());
		if (phone == null || phone.isBlank()) {
			u.setPhone(null);
		} else {
			String p = phone.trim();
			if (p.length() > 40) {
				throw new IllegalArgumentException("Số điện thoại quá dài.");
			}
			u.setPhone(p);
		}
		if (address == null || address.isBlank()) {
			u.setAddress(null);
		} else {
			String a = address.trim();
			if (a.length() > 400) {
				throw new IllegalArgumentException("Địa chỉ quá dài.");
			}
			u.setAddress(a);
		}
		return this.appUserRepository.save(u);
	}

	/**
	 * Đổi mật khẩu (xác nhận mật khẩu hiện tại).
	 */
	@Transactional
	public AppUser changePassword(Long userId, String currentRaw, String newRaw) {
		if (newRaw == null || newRaw.length() < 6) {
			throw new IllegalArgumentException("Mật khẩu mới tối thiểu 6 ký tự.");
		}
		Optional<AppUser> opt = this.appUserRepository.findById(userId);
		if (opt.isEmpty()) {
			throw new IllegalStateException("Không tìm thấy người dùng.");
		}
		AppUser u = opt.get();
		if (currentRaw == null || !this.passwordEncoder.matches(currentRaw, u.getPassword())) {
			throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
		}
		u.setPassword(this.passwordEncoder.encode(newRaw));
		return this.appUserRepository.save(u);
	}

	@Transactional
	public void deleteCustomerById(Long id) {
		Optional<AppUser> u = this.appUserRepository.findById(id);
		if (u.isEmpty()) {
			return;
		}
		if (u.get().getRole() == Role.ADMIN) {
			return;
		}
		this.appUserRepository.deleteById(id);
	}
}
