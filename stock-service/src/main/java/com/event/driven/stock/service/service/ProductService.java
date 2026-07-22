package com.event.driven.stock.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.ProductCreatedEvent;
import com.event.driven.common.service.events.ProductUpdatedEvent;
import com.event.driven.stock.service.dto.request.CreateProductRequest;
import com.event.driven.stock.service.dto.request.UpdateProductRequest;
import com.event.driven.stock.service.dto.response.ProductResponse;
import com.event.driven.stock.service.entity.Product;
import com.event.driven.stock.service.enums.EventType;
import com.event.driven.stock.service.exception.ResourceNotFoundException;
import com.event.driven.stock.service.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final OutboxEventService outboxEventService;

    @Autowired
    public ProductService(ProductRepository productRepository,
                            OutboxEventService outboxEventService) {
        this.productRepository = productRepository;
        this.outboxEventService = outboxEventService;
    }
    
    public ProductResponse createProduct(CreateProductRequest createProductRequest) {
        log.info("Creating product with SKU={}", createProductRequest.getSku());
        
        Product product = Product.builder()
                            .sku(createProductRequest.getSku())
                            .name(createProductRequest.getName())
                            .price(createProductRequest.getPrice())
                            .build();

        Product createdProduct = productRepository.save(product);
        ProductCreatedEvent createdEvent = ProductCreatedEvent.builder()
                            .productId(product.getId())
                            .sku(product.getSku())
                            .name(product.getName())
                            .price(product.getPrice())
                            .build();
        outboxEventService.saveEvent(EventType.PRODUCT_CREATED, AggregateType.PRODUCT, 
                        createdProduct.getId().toString(), createdEvent);
        log.info("Product created successfully, ProductId={}, SKU={}", 
                        createdProduct.getId(), createdProduct.getSku());      
        return mapToResponse(createdProduct);
    }

    public ProductResponse updateProduct(Long productId, UpdateProductRequest updateProductRequest) {
        log.info("Updating product, productId={}", productId);

        Product product = findByProductId(productId);
        Product update = Product.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(updateProductRequest.getName())
                .price(updateProductRequest.getPrice())
                .active(updateProductRequest.isActive())
                .build();
        Product updatedProduct = productRepository.save(update);

        ProductUpdatedEvent updatedEvent = ProductUpdatedEvent.builder()
                .productId(updatedProduct.getId())
                .sku(updatedProduct.getSku())
                .name(updatedProduct.getName())
                .price(updatedProduct.getPrice())
                .active(updatedProduct.isActive())
                .build();

        outboxEventService.saveEvent(EventType.PRODUCT_UPDATED, AggregateType.PRODUCT, 
                            updatedProduct.getId().toString(), updatedEvent);
        log.info("Product updated successfully, id={}", updatedProduct.getId());
        return mapToResponse(updatedProduct);
    }

    public Product findByProductId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found"));
    }

    public Product findBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found"));
    }

    public ProductResponse findProductBySku(String sku) {
        Product product = findBySku(sku);
        return mapToResponse(product);
    }

    public List<ProductResponse> findAllProducts() {
        return productRepository.findAll()
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                        .id(product.getId())
                        .sku(product.getSku())
                        .name(product.getName())
                        .price(product.getPrice())
                        .active(product.isActive())
                        .build();
    }
}
