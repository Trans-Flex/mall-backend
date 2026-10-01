package com.example.mallbackend.mapper;

import com.example.mallbackend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {
    @Select("SELECT * FROM orders") List<Order> getAllOrders();

    @Select("SELECT * FROM orders WHERE id = #{id}") Order getOrderById(Long id);

    @Select("SELECT * FROM orders WHERE user_id = #{userId}") List<Order> getOrdersByUserId(Long userId);

    @Insert("INSERT INTO orders (user_id, total_price, status) VALUES (#{userId}, #{totalPrice}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Order order);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}") int updateStatus(Order order);
}
