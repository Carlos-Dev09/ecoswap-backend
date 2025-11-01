package com.ecoswap.ecoswap.product.repositories;

import java.util.List;

import com.ecoswap.ecoswap.user.models.dto.UserDTO;
import com.ecoswap.ecoswap.user.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.product.models.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    List<Product> findTop5ByProductStatusOrderByReleaseDateDesc(String productStatus);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByProductStatus(String productStatus, Pageable pageable);
    List<Product> findByUser(User user);
    List<Product> findByProductStatus(String productStatus);
    List<Product> findByUserAndProductStatus(User user, String productStatus);

}
