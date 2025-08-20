package com.itjima_server.mapper;

import com.itjima_server.domain.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper {

    int insert(Item item);
}
