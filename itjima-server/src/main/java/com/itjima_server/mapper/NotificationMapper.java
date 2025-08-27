package com.itjima_server.mapper;

import com.itjima_server.domain.notification.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);
}
