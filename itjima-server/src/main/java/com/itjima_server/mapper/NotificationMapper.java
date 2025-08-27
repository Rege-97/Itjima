package com.itjima_server.mapper;

import com.itjima_server.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> findNotReadByUserId(@Param("userId") Long userId,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);

    Notification findById(@Param("id") Long id);

    int updateReadAtById(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);
}
