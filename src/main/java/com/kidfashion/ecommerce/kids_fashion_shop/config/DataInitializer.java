package com.kidfashion.ecommerce.kids_fashion_shop.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Role;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.AppUserRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.CategoryRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.DiscountCodeRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner seedData(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
			AdminBootstrapProperties adminProps, CategoryRepository categoryRepository,
			ProductRepository productRepository, DiscountCodeRepository discountCodeRepository) {
		return args -> {
			Optional<AppUser> adminExists = userRepository.findByEmailIgnoreCase(adminProps.getEmail());
			if (adminExists.isEmpty()) {
				AppUser admin = new AppUser();
				admin.setEmail(adminProps.getEmail().trim().toLowerCase());
				admin.setPassword(passwordEncoder.encode(adminProps.getPassword()));
				admin.setFullName(adminProps.getFullName());
				admin.setPhone("");
				admin.setAddress("");
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
			}

			long catCount = categoryRepository.count();
			if (catCount == 0) {
				Category ao = new Category();
				ao.setName("Áo trẻ em");
				ao.setDescription("Áo thun, áo sơ mi, áo khoác");
				ao = categoryRepository.save(ao);

				Category quan = new Category();
				quan.setName("Quần trẻ em");
				quan.setDescription("Quần jean, quần thể thao, quần short");
				quan = categoryRepository.save(quan);

				Category vay = new Category();
				vay.setName("Váy & đầm");
				vay.setDescription("Váy công chúa, đầm dự tiệc");
				vay = categoryRepository.save(vay);

				Category pk = new Category();
				pk.setName("Phụ kiện");
				pk.setDescription("Mũ, tất, balô nhỏ");
				categoryRepository.save(pk);

				Product p1 = new Product();
				p1.setName("Áo thun cotton Basic");
				p1.setDescription("100% cotton, nhiều size 1–8 tuổi, nhiều màu pastel.");
				p1.setPrice(new BigDecimal("159000"));
				p1.setImageUrl("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80");
				p1.setStockQuantity(Integer.valueOf(40));
				p1.setNewArrival(Boolean.TRUE);
				p1.setBestSeller(Boolean.TRUE);
				p1.setCreatedAt(LocalDateTime.now());
				p1.setCategory(ao);
				productRepository.save(p1);

				Product p2 = new Product();
				p2.setName("Áo hoodie ấm mùa đông");
				p2.setDescription("Chất liệu len mềm, nhiều màu pastel.");
				p2.setPrice(new BigDecimal("289000"));
				p2.setImageUrl("https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=800&q=80");
				p2.setStockQuantity(Integer.valueOf(25));
				p2.setNewArrival(Boolean.TRUE);
				p2.setBestSeller(Boolean.FALSE);
				p2.setCreatedAt(LocalDateTime.now().minusDays(1));
				p2.setCategory(ao);
				productRepository.save(p2);

				Product p3 = new Product();
				p3.setName("Quần jean mềm dài");
				p3.setDescription("Ôm vừa, có dây điều chỉnh.");
				p3.setPrice(new BigDecimal("239000"));
				p3.setImageUrl("https://images.unsplash.com/photo-1541093306590-bad0a7a62f4e?w=800&q=80");
				p3.setStockQuantity(Integer.valueOf(30));
				p3.setNewArrival(Boolean.FALSE);
				p3.setBestSeller(Boolean.TRUE);
				p3.setCreatedAt(LocalDateTime.now().minusDays(5));
				p3.setCategory(quan);
				productRepository.save(p3);

				Product p4 = new Product();
				p4.setName("Váy tulle công chúa");
				p4.setDescription("Lộng lẫy cho buổi tiệc, lót mềm bên trong.");
				p4.setPrice(new BigDecimal("399000"));
				p4.setImageUrl("https://images.unsplash.com/photo-1596870230757-0b5d27f5f0b2?w=800&q=80");
				p4.setStockQuantity(Integer.valueOf(15));
				p4.setNewArrival(Boolean.TRUE);
				p4.setBestSeller(Boolean.TRUE);
				p4.setCreatedAt(LocalDateTime.now().minusDays(2));
				p4.setCategory(vay);
				productRepository.save(p4);

				Product p5 = new Product();
				p5.setName("Set áo quần thể thao");
				p5.setDescription("Thoáng mát, thấm hút mồ hôi tốt.");
				p5.setPrice(new BigDecimal("199000"));
				p5.setImageUrl("https://images.unsplash.com/photo-1503919005314-2a1ccc787d8c?w=800&q=80");
				p5.setStockQuantity(Integer.valueOf(50));
				p5.setNewArrival(Boolean.FALSE);
				p5.setBestSeller(Boolean.FALSE);
				p5.setCreatedAt(LocalDateTime.now().minusDays(10));
				p5.setCategory(ao);
				productRepository.save(p5);
			}

			long disCount = discountCodeRepository.count();
			if (disCount == 0) {
				DiscountCode d1 = new DiscountCode();
				d1.setCode("KID10");
				d1.setPercentOff(new BigDecimal("10"));
				d1.setFixedAmountOff(null);
				d1.setExpiryDate(LocalDate.now().plusMonths(3));
				d1.setActive(Boolean.TRUE);
				d1.setMaxUses(Integer.valueOf(100));
				d1.setUsedCount(Integer.valueOf(0));
				discountCodeRepository.save(d1);

				DiscountCode d2 = new DiscountCode();
				d2.setCode("FREESHIP20K");
				d2.setPercentOff(null);
				d2.setFixedAmountOff(new BigDecimal("20000"));
				d2.setExpiryDate(LocalDate.now().plusMonths(6));
				d2.setActive(Boolean.TRUE);
				d2.setMaxUses(null);
				d2.setUsedCount(Integer.valueOf(0));
				discountCodeRepository.save(d2);
			}
		};
	}
}
