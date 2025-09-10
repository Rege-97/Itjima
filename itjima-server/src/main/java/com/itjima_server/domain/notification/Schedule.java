package com.itjima_server.domain.notification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Schedule {

    private long id;
    private long agreementId;
    private ScheduleType type;
    private LocalDate dueAt;
    private boolean notified;
}
