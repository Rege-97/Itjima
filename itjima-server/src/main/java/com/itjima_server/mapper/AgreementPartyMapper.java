package com.itjima_server.mapper;

import com.itjima_server.domain.AgreementParty;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgreementPartyMapper {

    int insert(AgreementParty agreementParty);
}
