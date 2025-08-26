package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AgreementMapper agreementMapper;

    @Transactional(readOnly = true)
    public PagedResultDTO<?> getRecentPartnerList(Long id, Long lastId, int size) {
        int sizePlusOne = size + 1;
        List<RecentPartnerResponseDTO> userList = agreementMapper.findRecentPartnersByUserId(id,
                lastId, sizePlusOne);

        if (userList == null || userList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }
        boolean hasNext = false;
        if (userList.size() == sizePlusOne) {
            hasNext = true;
            userList.remove(size);
        }

        lastId = userList.get(userList.size() - 1).getLastAgreementId();

        return PagedResultDTO.from(userList, hasNext, lastId);
    }
}
