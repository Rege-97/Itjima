package com.itjima_server.mapper;

import com.itjima_server.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.User;

@Mapper
public interface UserMapper {

    Users findByEmail(@Param("email") String email);

    int insert(User user);
}
