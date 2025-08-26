package com.itjima_server.mapper;

import com.itjima_server.domain.transaction.Transaction;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {

    int insert(Transaction transaction);

    BigDecimal sumConfirmedAmountByAgreementId(@Param("agreementId") long agreementId);

    List<Transaction> findByAgreementId(@Param("agreementId") long agreementId,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);
}
