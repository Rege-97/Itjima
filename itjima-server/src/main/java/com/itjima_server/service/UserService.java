package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import com.itjima_server.mapper.AgreementMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final AgreementMapper agreementMapper;

    /**
     * 대여 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param id     로그인한 사용자 ID
     * @param lastId 마지막으로 조회한 대여 ID
     * @param size   요청한 페이지 크기
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
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
