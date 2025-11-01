package com.ecoswap.ecoswap.product.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ecoswap.ecoswap.exchange.models.entities.Exchange;
import com.ecoswap.ecoswap.exchange.repositories.ExchangeRepository;
import com.ecoswap.ecoswap.product.exceptions.FileFormatException;
import com.ecoswap.ecoswap.product.exceptions.ProductCreationException;
import com.ecoswap.ecoswap.product.models.dto.ProductResponseDTO;
import com.ecoswap.ecoswap.user.models.entities.User;
import com.ecoswap.ecoswap.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecoswap.ecoswap.product.exceptions.ProductNotFoundException;
import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.product.models.entities.Product;
import com.ecoswap.ecoswap.product.repositories.ProductRepository;
import com.ecoswap.ecoswap.product.services.ProductService;
import com.ecoswap.ecoswap.user.models.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServicesImpl implements ProductService{

    @Autowired
    public ProductRepository productRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${image.storage.path}")
    private String storageFolderPath;

    @Override
    public ProductResponseDTO findAll(Integer page, Integer size) {
        List<Product> productList;

        if (page == null || size == null || size <= 0) {
            productList = productRepository.findByProductStatus("activo");
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage = productRepository.findByProductStatus("activo", pageable);
            productList = productPage.getContent();

            List<ProductDTO> products = productList.stream()
                    .map(this::mapToProductDTO)
                    .collect(Collectors.toList());

            return new ProductResponseDTO(products, productPage.getTotalPages());
        }

        List<ProductDTO> products = productList.stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        return new ProductResponseDTO(products, 1);


    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile image) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User usuarioAutenticado = (User) auth.getPrincipal();


            if (image == null || image.isEmpty()) {
                throw new IllegalArgumentException("Debe proporcionar una imagen para crear el producto");
            }

            String imageName = image.getOriginalFilename();
            // Obtener la extensión del archivo
            String fileExtension = imageName.substring(imageName.lastIndexOf(".") + 1).toLowerCase();

            if (!Arrays.asList("jpg", "jpeg", "png").contains(fileExtension)) {
                throw new FileFormatException("Extensión de imagen no permitida. Por favor suba una imagen con extensión jpg, png o jpeg");
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + imageName;
            // Ruta donde se guardarán las imágenes en el servidor
            String serverImagePath = "https://ecoswap-backend.onrender.com/images/" + uniqueFileName;

            // Guardar la imagen en el servidor 
            Files.write(Paths.get(storageFolderPath, uniqueFileName), image.getBytes());

            //Pasar el DTO a entidad para guardarlo
            Product product = new Product();
            product.setId(productDTO.getId());
            product.setTitle(productDTO.getTitle());
            product.setDescription(productDTO.getDescription());
            product.setCategory(productDTO.getCategory());
            product.setImageProduct(serverImagePath);
            product.setConditionProduct(productDTO.getConditionProduct());
            product.setUser(usuarioAutenticado);
            product.setProductStatus("activo");
            product.setReleaseDate(LocalDate.now());

            Product savedProduct = productRepository.save(product);
            ProductDTO productDTOResponse = new ProductDTO();
            productDTOResponse.setId(savedProduct.getId());
            productDTOResponse.setDescription(savedProduct.getDescription());
            productDTOResponse.setCategory(savedProduct.getCategory());
            productDTOResponse.setConditionProduct(savedProduct.getConditionProduct());
            productDTOResponse.setTitle(savedProduct.getTitle());
            productDTOResponse.setImageProduct(savedProduct.getImageProduct());
//            productDTOResponse.setUser(savedProduct.getUser().getEmail());
            productDTOResponse.setReleaseDate(savedProduct.getReleaseDate());


            return productDTOResponse;

        } catch (IOException e) { 
            throw new ProductCreationException("Error al crear el producto: " + e.getMessage());
        }
    }

    @Override
    public ProductDTO updateProductById(Long id, ProductDTO productDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAutenticado = (User) auth.getPrincipal();

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("El producto no existe"));
        if(product.getUser().getId() != usuarioAutenticado.getId()){
            throw new RuntimeException("No tienes permiso para esta acción");
        }

        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setCategory(productDTO.getCategory());
        product.setImageProduct(productDTO.getImageProduct());
        product.setConditionProduct(productDTO.getConditionProduct());

        Product savedProduct = productRepository.save(product);

        ProductDTO productDTOResponse = new ProductDTO();
        productDTOResponse.setId(savedProduct.getId());
        productDTOResponse.setDescription(savedProduct.getDescription());
        productDTOResponse.setCategory(savedProduct.getCategory());
        productDTOResponse.setConditionProduct(savedProduct.getConditionProduct());
        productDTOResponse.setTitle(savedProduct.getTitle());
        productDTOResponse.setImageProduct(savedProduct.getImageProduct());
        productDTOResponse.setReleaseDate(savedProduct.getReleaseDate());

        return productDTOResponse;
    }

    @Override
    public void deleteProduct(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAutenticado = (User) auth.getPrincipal();

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("El producto no existe"));
        if(product.getUser().getId() != usuarioAutenticado.getId()){
            throw new RuntimeException("No tienes permiso para esta acción");
        }

        product.setProductStatus("inactivo");

        productRepository.save(product);

    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("No existe el producto con id " + id));

        UserDTO userDTO = new UserDTO(product.getId(), product.getUser().getName(), product.getUser().getEmail(), product.getUser().getEmail(), product.getUser().getCellphoneNumber());
    
        return new ProductDTO(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getCategory(),
                product.getConditionProduct(),
                product.getImageProduct(),
                LocalDate.now(),
                userDTO
        );
    }

    @Override
    public ProductResponseDTO getProductsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategory(category, pageable);

        List<ProductDTO> productDTOs = products.stream()
                .filter(product -> "activo".equals(product.getProductStatus()))
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setTitle(product.getTitle());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCategory(product.getCategory());
                    productDTO.setConditionProduct(product.getConditionProduct());
                    productDTO.setImageProduct(product.getImageProduct());
                    productDTO.setReleaseDate(product.getReleaseDate());
                    return productDTO;
                })
                .collect(Collectors.toList());

        return new ProductResponseDTO(productDTOs, products.getTotalPages());

    }

    @Override
    public List<ProductDTO> getProductsByUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAutenticado = (User) auth.getPrincipal();

        List<Product> products = productRepository.findByUser(usuarioAutenticado);

        List<ProductDTO> productDTOs = products.stream()
                .filter(product -> "activo".equals(product.getProductStatus()))
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setTitle(product.getTitle());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCategory(product.getCategory());
                    productDTO.setConditionProduct(product.getConditionProduct());
                    productDTO.setImageProduct(product.getImageProduct());
                    productDTO.setReleaseDate(product.getReleaseDate());
                    return productDTO;
                })
                .collect(Collectors.toList());

        return productDTOs;

    }

    @Override
    public List<ProductDTO> getRecentlyProducts() {
        List<Product> recentlyProducts = productRepository.findTop5ByProductStatusOrderByReleaseDateDesc("activo");

        return recentlyProducts.stream()
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setTitle(product.getTitle());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCategory(product.getCategory());
                    productDTO.setConditionProduct(product.getConditionProduct());
                    productDTO.setImageProduct(product.getImageProduct());
                    productDTO.setReleaseDate(product.getReleaseDate());
                    return productDTO;
                })
                .toList();

    }

    @Override
    public Long countProduct() {
        return productRepository.count();
    }

    @Override
    public List<ProductDTO> getActiveProductsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Product> activeProducts = productRepository.findByUserAndProductStatus(user, "activo");
        
        return activeProducts.stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedRate = 2000)
    public void markProductsAsInactiveFromCompletedExchanges() {
         List<Exchange> completedExchanges = exchangeRepository.findByStatus("completado");

         for (Exchange exchange : completedExchanges) {
             Product productTo = exchange.getProductTo();
             Product productFrom = exchange.getProductFrom();

             if (productTo != null) {
                 productTo.setProductStatus("inactivo");
                 productRepository.save(productTo);
             }

             if (productFrom != null) {
                 productFrom.setProductStatus("inactivo");
                 productRepository.save(productFrom);
             }
         }

    }

    private ProductDTO mapToProductDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getCategory(),
            product.getConditionProduct(),
            product.getImageProduct(),
            product.getReleaseDate(),
            new UserDTO(
                product.getUser().getId(),
                product.getUser().getName(),
                product.getUser().getEmail(),
                product.getUser().getAddress(),
                product.getUser().getCellphoneNumber()
            )
        );
    }

    @Override
    public String getActiveProductsSummary() {
        List<Product> activeProducts = productRepository.findByProductStatus("activo");
        StringBuilder summary = new StringBuilder("Resumen de Productos Activos:\n");
        for (Product product : activeProducts) {
            summary.append("- Título: ").append(product.getTitle())
                   .append(", Categoría: ").append(product.getCategory())
                   .append(", Condición: ").append(product.getConditionProduct())
                   .append(", Fecha: ").append(product.getReleaseDate())
                   .append("\n");
        }
        return summary.toString();
    }
    

}
