package com.itjima_server.mapper;

import com.itjima_server.domain.user.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

    RefreshToken findByToken(@Param("token") String token);

    boolean existsByUserId(@Param("userId") long userId);

    void insert(RefreshToken refreshToken);

    void update(RefreshToken refreshToken);

    void deleteByUserId(@Param("userId") long userId);
}
