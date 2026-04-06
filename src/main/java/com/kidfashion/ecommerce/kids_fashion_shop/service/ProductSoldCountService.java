package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.OrderLineRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.List;

@Service
public class ProductSoldCountService {

    private final ProductRepository productRepository;
    private final OrderLineRepository orderLineRepository;

    public ProductSoldCountService(ProductRepository productRepository, OrderLineRepository orderLineRepository) {
        this.productRepository = productRepository;
        this.orderLineRepository = orderLineRepository;
    }

    /**
     * Đồng bộ hóa lại toàn bộ sold_count từ bảng order_lines.
     * Chạy phương thức này một lần để khởi tạo dữ liệu cũ.
     */
    @PostConstruct
    @Transactional
    public void syncAllSoldCounts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            long totalSold = orderLineRepository.sumQuantityByProductId(product.getId());
            product.setSoldCount(totalSold);
        }
        productRepository.saveAll(products);
    }

    /**
     * Tăng sold_count của sản phẩm khi có đơn hàng mới thành công.
     */
    @Transactional
    public void incrementSoldCount(Long productId, int quantity) {
        productRepository.findById(productId).ifPresent(product -> {
            long current = product.getSoldCount() == null ? 0L : product.getSoldCount();
            product.setSoldCount(current + quantity);
            productRepository.save(product);
        });
    }
}
