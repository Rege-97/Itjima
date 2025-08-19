package com.itjima_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.security.core.userdetails.User;

@Mapper
public interface UserMapper {

    int insert(User user);
}
