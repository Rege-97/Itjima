package com.itjima_server.mapper;

import com.itjima_server.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

    RefreshToken findByToken(@Param("token") String token);

    RefreshToken findByUserId(@Param("userId") int userId);

    void insert(RefreshToken refreshToken);

    void update(RefreshToken refreshToken);

    void deleteByUserId(@Param("userId") int userId);
}
