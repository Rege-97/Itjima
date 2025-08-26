package com.itjima_server.mapper;

import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementMapper {

    int insert(Agreement agreement);

    Agreement findById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") AgreementStatus status);

    AgreementDetailDTO findDetailById(@Param("id") Long id);

    List<AgreementDetailDTO> findByUserId(@Param("userId") Long userId, @Param("role") String role,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);

    List<RecentPartnerResponseDTO> findRecentPartnersByUserId(@Param("userId") Long userId, @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);
}

