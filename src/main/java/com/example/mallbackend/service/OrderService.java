package com.example.mallbackend.service;

import com.example.mallbackend.dto.CreateOrderRequest;
import com.example.mallbackend.dto.OrderItemRequest;
import com.example.mallbackend.entity.*;
import com.example.mallbackend.exception.*;
import com.example.mallbackend.mapper.OrderItemMapper;
import com.example.mallbackend.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final UserService userService;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductService productService, UserService userService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
        this.userService = userService;
    }

    public List<Order> getAllOrders() {
        return orderMapper.getAllOrders();
    }

    public Order getOrderById(Long id) {
        return orderMapper.getOrderById(id);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.getOrdersByUserId(userId);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        Order order = orderMapper.getOrderById(orderId);
        if(order == null) {
            throw new OrderNotFoundException("订单 " + orderId + " 不存在");
        }
        return orderItemMapper.getOrderItemsByOrderId(orderId);
    }

    @Transactional
    public Order createOrder(CreateOrderRequest createOrderRequest) {
        User user = userService.getUserById(createOrderRequest.getUserId());
        if(user == null) {
            throw new UserNotFoundException("用户 " + createOrderRequest.getUserId() + " 不存在");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest item : createOrderRequest.getItems()) {
            Long productId = item.getProductId();
            Product product = productService.getProductById(productId);
            if(product == null) {
                throw new ProductNotFoundException("商品 " + productId + " 不存在");
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItems.add(orderItem);
        }
        Order order = new Order();
        order.setUserId(createOrderRequest.getUserId());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        int row = orderMapper.insertOrder(order);
        if (row == 0) {
            throw new BusinessException("订单插入失败");
        }

        Long orderId = order.getId();
        for(OrderItem item : orderItems) {
            item.setOrderId(orderId);
            row = orderItemMapper.insertOrderItem(item);
            if (row == 0) {
                throw new BusinessException("订单项插入失败，商品 " + item.getProductId());
            }

            boolean ok = productService.decreaseStock(item.getProductId(), item.getQuantity());
            if (!ok) {
                throw new InsufficientStockException("商品 " + item.getProductId() + " 库存不足");
            }
        }

        return orderMapper.getOrderById(orderId);
    }
}
