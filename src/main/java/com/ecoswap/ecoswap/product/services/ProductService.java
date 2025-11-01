package com.ecoswap.ecoswap.product.services;

import java.util.List;

import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.product.models.dto.ProductResponseDTO;
import com.ecoswap.ecoswap.user.models.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    ProductResponseDTO findAll(Integer page, Integer size);
    ProductDTO createProduct(ProductDTO productDTO, MultipartFile image);
    ProductDTO updateProductById(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductDTO getProductById(Long id);

    ProductResponseDTO getProductsByCategory(String category, int page, int size);
    List<ProductDTO> getProductsByUser();
    List<ProductDTO> getActiveProductsByUserId(Long userId);
    List<ProductDTO> getRecentlyProducts();
    Long countProduct();

    void markProductsAsInactiveFromCompletedExchanges();
    String getActiveProductsSummary();

}
