package com.example.mallbackend.mapper;

import com.example.mallbackend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user") List<User> getAllUsers();

    @Insert("INSERT INTO user (username, password, phone) VALUES (#{username}, #{password}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Select("SELECT * FROM user WHERE id = #{id}") User getUserById(Long id);

    @Update("UPDATE user SET username = #{user.username}, password = #{user.password}, phone = #{user.phone} WHERE id = #{id}")
    int updateUser(@Param("id") Long id, @Param("user") User user);

    @Delete("DELETE FROM user WHERE id = #{id}") int deleteUser(Long id);
}

