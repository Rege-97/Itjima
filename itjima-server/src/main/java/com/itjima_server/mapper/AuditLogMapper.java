package com.itjima_server.mapper;

import com.itjima_server.domain.audit.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {

    int insert(AuditLog auditLog);
}
