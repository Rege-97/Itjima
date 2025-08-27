package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.notification.Notification;
import com.itjima_server.domain.notification.NotificationType;
import com.itjima_server.domain.notification.Schedule;
import com.itjima_server.domain.notification.ScheduleType;
import com.itjima_server.dto.notification.response.NotificationResponseDTO;
import com.itjima_server.exception.agreement.NotInsertAgreementException;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.NotFoundException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.NotificationMapper;
import com.itjima_server.mapper.ScheduleMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ScheduleMapper scheduleMapper;
    private final AgreementPartyMapper agreementPartyMapper;

    /**
     * 스케줄 기반 리마인드/연체 알림 생성
     */
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

                checkInsertResult(notificationMapper.insert(notification), "알림 생성에 실패했습니다.");
            }

            checkUpdateResult(scheduleMapper.updateNotifiedById(schedule.getId()),
                    "스케쥴 상태 변경에 실패했습니다.");

        }
    }

    /**
     * 읽지 않은 알림 목록 조회 (페이징)
     *
     * @param userId 로그인한 사용자 id
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @return 알림 리스트 응답 DTO
     */
    @Transactional(readOnly = true)
    public PagedResultDTO<?> getNotReadList(long userId, long lastId, int size) {
        int sizePlusOne = size + 1;
        List<Notification> notificationList = notificationMapper.findNotReadByUserId(userId, lastId,
                sizePlusOne);

        if (notificationList == null || notificationList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }
        boolean hasNext = false;
        if (notificationList.size() == sizePlusOne) {
            hasNext = true;
            notificationList.remove(size);
        }

        List<NotificationResponseDTO> notifications = new ArrayList<>();

        for (Notification notification : notificationList) {
            notifications.add(NotificationResponseDTO.from(notification));
        }

        lastId = notificationList.get(notificationList.size() - 1).getId();

        return PagedResultDTO.from(notifications, hasNext, lastId);
    }

    /**
     * 알림 읽음 처리
     *
     * @param userId 로그인한 사용자 ID
     * @param id     읽음 처리할 알림 ID
     * @return  읽음 처리된 알림 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public NotificationResponseDTO updateReadAt(Long userId, Long id) {
        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            throw new NotFoundException("해당 알림을 찾을 수 없습니다.");
        }
        if (notification.getUserId() != userId) {
            throw new NotAuthorException("로그인한 사용자의 알림이 아닙니다.");
        }
        if (notification.getReadAt() != null) {
            throw new InvalidStateException("이미 읽은 알림입니다.");
        }

        notification.setReadAt(LocalDateTime.now());

        checkUpdateResult(notificationMapper.updateReadAtById(id, notification.getReadAt()),
                "알림 읽음 처리를 실패했습니다.");

        return NotificationResponseDTO.from(notification);
    }

    /**
     * INSERT 실행 결과 검증 유틸리티
     *
     * @param result       실행된 row 수
     * @param errorMessage 실패 시 예외 메시지
     * @throws NotInsertAgreementException insert 실패 시
     */
    private void checkInsertResult(int result, String errorMessage) {
        if (result < 1) {
            throw new NotInsertAgreementException(errorMessage);
        }
    }

    /**
     * UPDATE 실행 결과 검증 유틸리티
     *
     * @param result       실행된 row 수
     * @param errorMessage 실패 시 예외 메시지
     * @throws UpdateFailedException update 실패 시
     */
    private void checkUpdateResult(int result, String errorMessage) {
        if (result < 1) {
            throw new UpdateFailedException(errorMessage);
        }
    }
}
