package com.example.mallbackend.service;

import com.example.mallbackend.entity.Product;
import com.example.mallbackend.exception.BusinessException;
import com.example.mallbackend.mapper.ProductMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ProductService {
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private ConcurrentHashMap<String, Lock> locks;

    private static final String PRODUCT_KEY_PREFIX = "mall:product:";

    public ProductService(ProductMapper productMapper, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        locks = new ConcurrentHashMap<>();
    }

    public List<Product> getAllProducts() { return productMapper.getAllProducts(); }

    public Product getProductById(Long id) {
        String redisKey = PRODUCT_KEY_PREFIX + id;
        String productStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (productStr != null) {
            try {
                if("null".equals(productStr)) {
                    return null;
                }
                return objectMapper.readValue(productStr, Product.class);
            } catch (JacksonException e) {
                throw new BusinessException("商品缓存解析失败，id=" + id, e);
            }
        }

        Lock lock = locks.computeIfAbsent(redisKey, k -> new ReentrantLock());
        lock.lock();
        try{
            productStr = stringRedisTemplate.opsForValue().get(redisKey);
            if(productStr != null) {
                try {
                    if("null".equals(productStr)) {
                        return null;
                    }
                    return objectMapper.readValue(productStr, Product.class);
                } catch (JacksonException e) {
                    throw new BusinessException("商品缓存解析失败，id=" + id, e);
                }
            }

            Product product = productMapper.getProductById(id);
            if (product != null) {
                try {
                    String json = objectMapper.writeValueAsString(product);
                    stringRedisTemplate.opsForValue().set(redisKey, json, Duration.ofMinutes(10 + ThreadLocalRandom.current().nextInt(6)));
                } catch (JacksonException e) {
                    throw new BusinessException("商品缓存写入失败，id=" + id, e);
                }
            }
            else {
                stringRedisTemplate.opsForValue().set(redisKey, "null", Duration.ofMinutes(2 + ThreadLocalRandom.current().nextInt(3)));
            }
            return product;
        } finally {
            lock.unlock();
        }
    }

    public Product createProduct(Product product) {
        int rows = productMapper.insertProduct(product);
        if(rows == 0) {
            return null;
        }
        return product;
    }

    public boolean updateProduct(Product product) {
        int rows = productMapper.updateProduct(product);
        if (rows <= 0) {
            return false;
        }
        stringRedisTemplate.delete(PRODUCT_KEY_PREFIX + product.getId());
        return true;
    }

    public boolean decreaseStock(Long id, int quantity) {
        int rows = productMapper.decreaseStock(id, quantity);
        if (rows <= 0) {
            return false;
        }
        stringRedisTemplate.delete(PRODUCT_KEY_PREFIX + id);
        return true;
    }

    public boolean deleteProduct(Long id) {
        int rows = productMapper.deleteProduct(id);
        if (rows <= 0) {
            return false;
        }
        stringRedisTemplate.delete(PRODUCT_KEY_PREFIX + id);
        return true;
    }
}
