package com.itjima_server.mapper;

import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import com.itjima_server.dto.agreement.response.AgreementRenderingDetailResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementSummaryResponseDTO;
import com.itjima_server.dto.item.response.ItemAgreementHistoryResponseDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgreementMapper {

    int insert(Agreement agreement);

    Agreement findById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") AgreementStatus status);

    int updateCompleted(@Param("id") Long id);

    AgreementDetailDTO findDetailById(@Param("id") Long id);

    List<AgreementDetailDTO> findByUserId(@Param("userId") Long userId, @Param("role") String role,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);

    List<RecentPartnerResponseDTO> findRecentPartnersByUserId(@Param("userId") Long userId,
            @Param("lastId") Long lastId,
            @Param("sizePlusOne") int sizePlusOne);

    List<Long> findOverdueAgreementIds();

    int updateDueAtAndStatusById(@Param("id") Long id, @Param("status") AgreementStatus status,
            @Param("dueAt") LocalDateTime dueAt);

    int updateTermsById(@Param("id") Long id, @Param("terms") String terms);

    List<ItemAgreementHistoryResponseDTO> findHistoryByItemId(@Param("itemId") Long itemId,
            @Param("lastId") Long lastId, @Param("sizePlusOne") int sizePlusOne);

    List<AgreementSummaryResponseDTO> findAgreementSummariesByUserId(@Param("userId") Long userId,
            @Param("keyword") String keyword, @Param("role") AgreementPartyRole role,
            @Param("lastId") Long lastId, @Param("sizePlusOne") int sizePlusOne);

    AgreementRenderingDetailResponseDTO findAgreementDetailById(@Param("id") Long id);

    boolean existsByIdAndUserId(@Param("id") Long id,@Param("userId") Long userId);
}

