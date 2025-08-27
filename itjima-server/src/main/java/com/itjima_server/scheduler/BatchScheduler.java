package com.itjima_server.scheduler;

import com.itjima_server.service.AgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final AgreementService agreementService;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueAgreements() {
        log.info("연체된 계약을 확인하는 스케줄 작업을 시작합니다...");
        try {
            agreementService.processOverdueAgreements();
            log.info("연체된 계약 확인 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("연체된 계약 확인 작업 중 오류가 발생했습니다.", e);
        }
    }
}
