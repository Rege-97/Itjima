package com.itjima_server.mapper;

import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementPartyRole;
import com.itjima_server.domain.AgreementStatus;
import com.itjima_server.domain.Item;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementMapper {

    int insert(Agreement agreement);

    Agreement findById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") AgreementStatus status);

    AgreementDetailDTO findDetailById(@Param("id") Long id);

    List<Item> findByUserId(@Param("userId") Long userId, @Param("role") AgreementPartyRole role,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);
}

