package com.itjima_server.aop;

import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.audit.AuditLog;
import com.itjima_server.domain.transaction.Transaction;
import com.itjima_server.mapper.AuditLogMapper;
import com.itjima_server.security.CustomUserDetails;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;

    @AfterReturning(pointcut = "@annotation(audit)")
    public void LogActivity(JoinPoint joinPoint, Audit audit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return; // 사용자 정보가 없으면 로그를 남기지 않음
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Object[] args = joinPoint.getArgs();
        Long agreementId = findAgreementId(args);
        String detail = createDetail(args);

        if (agreementId == null) {
            return;
        }

        AuditLog log = AuditLog.builder()
                .agreementId(agreementId)
                .userId(userId)
                .action(audit.action())
                .detail(detail)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogMapper.insert(log);
    }

    private Long findAgreementId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Agreement) {
                return ((Agreement) arg).getId();
            }
            if (arg instanceof Transaction) {
                return ((Transaction) arg).getAgreementId();
            }
        }
        return null;
    }

    private String createDetail(Object[] args) {
        return null;
    }

}
