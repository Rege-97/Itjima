package com.itjima_server.mapper;

import com.itjima_server.domain.audit.AuditLog;
import com.itjima_server.dto.agreement.response.AgreementLogsResponseDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {

    int insert(AuditLog auditLog);

    List<AgreementLogsResponseDTO> findByAgreementId(@Param("agreementId") Long agreementId,
            @Param("lastId") Long lastId, @Param("sizePlusOne") int sizePlusOne);
}
