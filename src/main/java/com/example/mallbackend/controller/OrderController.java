package com.example.mallbackend.controller;

import com.example.mallbackend.dto.CreateOrderRequest;
import com.example.mallbackend.entity.Order;
import com.example.mallbackend.entity.OrderItem;
import com.example.mallbackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders") public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/{id}") public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/orders/{orderId}/items") public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    @PostMapping("/orders") public Order insertOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.createOrder(createOrderRequest);
    }
}
