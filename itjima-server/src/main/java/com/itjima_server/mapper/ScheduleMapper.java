package com.itjima_server.mapper;

import com.itjima_server.domain.notification.Schedule;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScheduleMapper {

    int insert(Schedule schedule);

    int updateNotifiedById(@Param("id") long id);

    List<Schedule> findPendingSchedules();
}
