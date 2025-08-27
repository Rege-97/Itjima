package com.itjima_server.service;

import com.itjima_server.domain.notification.Notification;
import com.itjima_server.domain.notification.NotificationType;
import com.itjima_server.domain.notification.Schedule;
import com.itjima_server.domain.notification.ScheduleType;
import com.itjima_server.exception.common.NotFoundException;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.NotificationMapper;
import com.itjima_server.mapper.ScheduleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ScheduleMapper scheduleMapper;
    private final AgreementPartyMapper agreementPartyMapper;

    @Transactional(rollbackFor = Exception.class)
    public void createReminders() {
        List<Schedule> pendingSchedules = scheduleMapper.findPendingSchedules();

        for (Schedule schedule : pendingSchedules) {
            List<Long> userIds = agreementPartyMapper.findUserIdsByAgreementId(
                    schedule.getAgreementId());
            String message = null;
            NotificationType notificationType = null;

            if (ScheduleType.OVERDUE == schedule.getType()) {
                message = "[ " + schedule.getType().getDescription() + " ] 연체 중입니다!";
                notificationType = NotificationType.OVERDUE;
            } else {
                message = "[ " + schedule.getType().getDescription() + " ] 반납일을 잊지 마세요!";
                notificationType = NotificationType.REMINDER;
            }
            for (Long userId : userIds) {
                Notification notification = Notification.builder()
                        .agreementId(schedule.getAgreementId())
                        .userId(userId)
                        .type(notificationType)
                        .message(message)
                        .build();
                notificationMapper.insert(notification);
            }
            scheduleMapper.updateNotifiedById(schedule.getId());
        }
    }
}
