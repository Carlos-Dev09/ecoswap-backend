package com.ecoswap.ecoswap.product.controllers;

import java.util.List;

import com.ecoswap.ecoswap.product.models.dto.ProductResponseDTO;
import com.ecoswap.ecoswap.product.models.entities.Product;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.product.services.ProductService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/")
public class ProductController {

    @Autowired
    public ProductService productService;

    @GetMapping("product")
    public ResponseEntity<ProductResponseDTO> findAllProducts(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/product/category/{category}")
    public ResponseEntity<ProductResponseDTO> getProductByCategory(@PathVariable String category,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "9") int size){
        return ResponseEntity.ok(productService.getProductsByCategory(category, page, size));
    }

    @GetMapping("/product/user")
    public ResponseEntity<List<ProductDTO>> getProductByUser(){
        return ResponseEntity.ok(productService.getProductsByUser());
    }

    @GetMapping("/product/active/user/{userId}")
    public ResponseEntity<List<ProductDTO>> getActiveProductsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(productService.getActiveProductsByUserId(userId));
    }

    @GetMapping("/product/recent")
    public ResponseEntity<List<ProductDTO>> getRecentlyProducts(){
        return ResponseEntity.ok(productService.getRecentlyProducts());
    }

    @PostMapping("/product/create")
    public ResponseEntity<ProductDTO> createProduct(@Valid ProductDTO productDTO, @RequestParam(value = "file", required = false) MultipartFile image){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDTO, image));
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductDTO productDTO){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productService.updateProductById(id,productDTO));
    }

    @DeleteMapping("/product/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    @GetMapping("/products/counts")
    public ResponseEntity<Long> countProduct(){
        return ResponseEntity.ok(productService.countProduct());
    }

}
