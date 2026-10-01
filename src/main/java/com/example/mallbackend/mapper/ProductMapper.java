package com.example.mallbackend.mapper;

import com.example.mallbackend.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {
    @Select("SELECT * FROM product") List<Product> getAllProducts();

    @Select("SELECT * FROM product WHERE id = #{id}") Product getProductById(Long id);

    @Insert("INSERT INTO product (name, price, stock, description) VALUES (#{name}, #{price}, #{stock}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProduct(Product product);

    @Update("UPDATE product SET name = #{name}, price = #{price}, stock = #{stock}, description = #{description} WHERE id = #{id}")
    int updateProduct(Product product);

    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Delete("DELETE FROM product WHERE id = #{id}") int deleteProduct(Long id);
}
