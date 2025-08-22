package com.itjima_server.mapper;

import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementMapper {

    int insert(Agreement agreement);

    Agreement findById(@Param("id") int id);

    int updateStatusById(@Param("status") AgreementStatus status);
}
