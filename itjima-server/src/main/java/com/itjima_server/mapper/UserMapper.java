package com.itjima_server.mapper;

import com.itjima_server.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByEmail(@Param("email") String email);

    int insert(User user);
}
