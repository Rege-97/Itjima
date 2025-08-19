package com.itjima_server.mapper;

import com.itjima_server.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 이메일 중복 검증
    boolean existsByEmail(@Param("email") String email);

    // 전화번호 중복 검증
    boolean existsByPhone(@Param("phone") String phone);

    // 회원가입
    int insert(User user);

    User findById(@Param("id") Long id);

    User findByEmail(@Param("email") String email);
}
