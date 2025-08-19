package com.itjima_server.mapper;

import com.itjima_server.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 이메일로 사용자 조회
    boolean findByEmail(@Param("email") String email);

    // 전화번호로 사용자 조회
    boolean findByPhone(@Param("phone") String phone);

    // 회원가입
    int insert(User user);
}
