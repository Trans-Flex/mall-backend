package com.example.mallbackend.service;

import com.example.mallbackend.entity.Product;
import com.example.mallbackend.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> getAllProducts() { return productMapper.getAllProducts(); }

    public Product getProductById(Long id) { return productMapper.getProductById(id); }

    public Product createProduct(Product product) {
        int rows = productMapper.insertProduct(product);
        if(rows == 0) {
            return null;
        }
        return product;
    }

    public boolean updateProduct(Product product) {
        int rows = productMapper.updateProduct(product);
        return rows != 0;
    }

    public boolean decreaseStock(Long id, int quantity) {
        int rows = productMapper.decreaseStock(id, quantity);
        return rows != 0;
    }

    public boolean deleteProduct(Long id) {
        int rows = productMapper.deleteProduct(id);
        return rows != 0;
    }
}
