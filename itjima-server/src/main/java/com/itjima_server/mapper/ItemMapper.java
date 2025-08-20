package com.itjima_server.mapper;

import com.itjima_server.domain.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemMapper {

    int insert(Item item);

    boolean existsById(@Param("id") Long id);

    int update(Item item);
}
