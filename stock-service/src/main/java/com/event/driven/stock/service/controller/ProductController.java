package com.event.driven.stock.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.stock.service.dto.request.CreateProductRequest;
import com.event.driven.stock.service.dto.request.UpdateProductRequest;
import com.event.driven.stock.service.dto.response.ProductResponse;
import com.event.driven.stock.service.service.ProductService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
                    @RequestBody CreateProductRequest createProductRequest) {
        ProductResponse productResponse = productService.createProduct(
                                                        createProductRequest);
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
                        @PathVariable Long productId,
                        @RequestBody UpdateProductRequest updateProductRequest) {
        ProductResponse productResponse = productService
                        .updateProduct(productId, updateProductRequest);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String sku) {
        ProductResponse productResponse = productService.findProductBySku(sku);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> productResponses = productService.findAllProducts();
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }
}
