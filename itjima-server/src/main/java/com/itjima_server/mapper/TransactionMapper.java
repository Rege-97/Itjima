package com.itjima_server.mapper;

import com.itjima_server.domain.transaction.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper {

    int insert(Transaction transaction);

}
