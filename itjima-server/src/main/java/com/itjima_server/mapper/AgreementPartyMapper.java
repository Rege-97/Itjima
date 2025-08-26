package com.itjima_server.mapper;

import com.itjima_server.domain.agreement.AgreementParty;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementPartyMapper {

    int insert(AgreementParty agreementParty);

    List<AgreementParty> findByAgreementId(@Param("agreementId") Long agreementId);

    int updateConfirmedAtById(@Param("id") Long id, @Param("confirmedAt") LocalDateTime confirmedAt);
}
