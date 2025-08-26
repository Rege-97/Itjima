package com.itjima_server.aop;

import com.itjima_server.domain.audit.AuditLog;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.mapper.AuditLogMapper;
import com.itjima_server.security.CustomUserDetails;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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

    /**
     * Audit 어노테이션이 붙은 메소드가 성공적으로 반환된 후 활동 로그를 기록
     *
     * @param audit     메소드에 적용된 Audit 어노테이션
     * @param result    메소드의 반환값
     */
    @AfterReturning(pointcut = "@annotation(audit)", returning = "result")
    public void LogActivity(Audit audit, Object result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return; // 사용자 정보가 없으면 로그를 남기지 않음
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Long agreementId = findAgreementIdFromResult(result);

        if (agreementId == null) {
            return;
        }

        AuditLog log = AuditLog.builder()
                .agreementId(agreementId)
                .userId(userId)
                .action(audit.action())
                .detail(audit.action().getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        auditLogMapper.insert(log);
    }

    private Long findAgreementIdFromResult(Object result) {
        if (result instanceof AgreementResponseDTO) {
            return ((AgreementResponseDTO) result).getId();
        }
        if (result instanceof TransactionResponseDTO) {
            return ((TransactionResponseDTO) result).getAgreementId();
        }
        return null;
    }

}
