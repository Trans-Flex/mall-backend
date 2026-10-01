package com.example.mallbackend.controller;

import com.example.mallbackend.entity.Product;
import com.example.mallbackend.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products") public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/products/{id}") public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/products") public Product insertProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping("/products/{id}") public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean ok = productService.updateProduct(product);
        if(!ok) {
            return null;
        }
        return productService.getProductById(id);
    }

    @DeleteMapping("/products/{id}") public String deleteProduct(@PathVariable Long id) {
        boolean ok = productService.deleteProduct(id);
        if(!ok) {
            return "商品不存在";
        }
        return "删除成功";
    }
}
