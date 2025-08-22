package com.itjima_server.mapper;

import com.itjima_server.domain.AgreementParty;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementPartyMapper {

    int insert(AgreementParty agreementParty);

    AgreementParty findByAgreementIdAndRole(@Param("agreementId") int agreementId,
            @Param("Role") String role);

    int updateConfirmedAtById(@Param("id") int id, @Param("confirmedAt") LocalDateTime confirmedAt);
}
