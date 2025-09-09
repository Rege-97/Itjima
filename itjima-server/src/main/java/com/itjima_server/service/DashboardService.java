package com.itjima_server.service;

import com.itjima_server.dto.dashboard.response.DashboardAgreementCountResponseDTO;
import com.itjima_server.dto.dashboard.response.DashboardComingReturnDTO;
import com.itjima_server.dto.dashboard.response.DashboardOverdueDTO;
import com.itjima_server.dto.dashboard.response.DashboardResponseDTO;
import com.itjima_server.mapper.AgreementMapper;
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

    /**
     * 대쉬보드 응답
     *
     * @param userId 로그인한 사용자 ID
     * @return 대쉬보드 응답 DTO
     */
    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboardInfo(Long userId) {
        List<DashboardAgreementCountResponseDTO> counts = agreementMapper.countAgreementsByUserId(
                userId);

        List<DashboardComingReturnDTO> comingReturns = agreementMapper.findComingAgreementsByUserId(
                userId);

        List<DashboardOverdueDTO> overdues = agreementMapper.findOverdueAgreementsByUserId(userId);

        return new DashboardResponseDTO(counts, comingReturns, overdues);
    }

}
