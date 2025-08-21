package com.itjima_server.mapper;

import com.itjima_server.domain.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemMapper {

    int insert(Item item);

    Item findById(@Param("id") Long id);

    int updateById(Item item);

    int updateFileById(Item item);

    Item findByUserId(@Param("userId") Long userId, @Param("lastId") Long lastId,
            @Param("size") int size);
}
