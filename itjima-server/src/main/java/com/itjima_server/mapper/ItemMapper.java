package com.itjima_server.mapper;

import com.itjima_server.domain.Item;
import com.itjima_server.domain.ItemStatus;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemMapper {

    int insert(Item item);

    Item findById(@Param("id") Long id);

    int updateById(Item item);

    int updateFileById(Item item);

    List<Item> findByUserId(@Param("userId") Long userId, @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);

    int updateStatusById(@Param("id") Long id, @Param("status") ItemStatus status);
}
