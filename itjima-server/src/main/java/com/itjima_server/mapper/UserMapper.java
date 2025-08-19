package com.itjima_server.mapper;

import com.itjima_server.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    boolean findByEmail(@Param("email") String email);

    boolean findByPhone(@Param("phone") String phone);

    int insert(User user);
}
