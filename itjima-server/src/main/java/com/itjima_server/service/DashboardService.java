package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.dashboard.response.DashboardAgreementCountResponseDTO;
import com.itjima_server.dto.dashboard.response.DashboardComingReturnDTO;
import com.itjima_server.dto.dashboard.response.DashboardOverdueDTO;
import com.itjima_server.dto.dashboard.response.DashboardPendingResponseDTO;
import com.itjima_server.dto.dashboard.response.DashboardResponseDTO;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대쉬보드 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-09-10
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AgreementMapper agreementMapper;
    private final UserMapper userMapper;

    /**
     * 대쉬보드 응답
     *
     * @param userId 로그인한 사용자 ID
     * @return 대쉬보드 응답 DTO
     */
    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboardInfo(Long userId) {
        String name = userMapper.findNameById(userId);

        List<DashboardAgreementCountResponseDTO> counts = agreementMapper.countAgreementsByUserId(
                userId);

        List<DashboardComingReturnDTO> comingReturns = agreementMapper.findComingAgreementsByUserId(
                userId);

        List<DashboardOverdueDTO> overDues = agreementMapper.findOverdueAgreementsByUserId(userId);

        return new DashboardResponseDTO(name, counts, comingReturns, overDues);
    }

    @Transactional(readOnly = true)
    public PagedResultDTO<?> getPending(Long userId, Long cursorKey, int size) {
        int sizePlusOne = size + 1;
        List<DashboardPendingResponseDTO> pendingList = agreementMapper.findPendingAgreementsByUserId(
                userId, cursorKey, sizePlusOne);
        if (pendingList == null || pendingList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }
        boolean hasNext = false;
        if (pendingList.size() == sizePlusOne) {
            hasNext = true;
            pendingList.remove(size);
        }

        cursorKey = pendingList.get(pendingList.size() - 1).getCursorKey();
        return PagedResultDTO.from(pendingList, hasNext, cursorKey);
    }

}
