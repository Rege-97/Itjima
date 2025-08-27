package com.itjima_server.scheduler;

import com.itjima_server.service.AgreementService;
import com.itjima_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 배치 스케줄러
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final AgreementService agreementService;
    private final NotificationService notificationService;

    /**
     * 연체 자동 변경 및 알림 생성 배치
     */
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

    /**
     * 리마인드 알림 생성
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendReminderNotification() {
        log.info("리마인드 알림 생성 스케줄 작업을 시작합니다...");
        try {
            notificationService.createReminders();
            log.info("리마인드 알림 생성 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("리마인드 알림 생성 작업 중 오류가 발생했습니다.", e);
        }
    }
}
