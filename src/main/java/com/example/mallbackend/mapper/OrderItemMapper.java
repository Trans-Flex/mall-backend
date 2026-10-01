package com.example.mallbackend.mapper;

import com.example.mallbackend.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    @Select("SELECT * FROM order_item") List<OrderItem> getAllOrderItems();

    @Select("SELECT * FROM order_item WHERE id = #{id}") OrderItem getOrderItemById(Long id);

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}") List<OrderItem> getOrderItemsByOrderId(Long orderId);

    @Insert("INSERT INTO order_item (order_id, product_id, product_name, quantity, price) VALUES (#{orderId}, #{productId}, #{productName}, #{quantity}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);
}
