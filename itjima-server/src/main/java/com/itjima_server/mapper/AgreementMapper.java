package com.itjima_server.mapper;

import com.itjima_server.domain.Agreement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgreementMapper {

    int insert(Agreement agreement);
}
