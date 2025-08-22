package com.itjima_server.service;

import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementParty;
import com.itjima_server.domain.AgreementPartyRole;
import com.itjima_server.domain.AgreementStatus;
import com.itjima_server.domain.User;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementPartyInfoDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.user.response.UserSimpleInfoDTO;
import com.itjima_server.exception.agreement.NotInsertAgreementException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.UserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementMapper agreementMapper;
    private final AgreementPartyMapper agreementPartyMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO create(long userId, AgreementCreateRequestDTO req) {
        User creditorUser = userMapper.findById(userId);
        User debitorUser = userMapper.findById(req.getDebtorUserId());
        if (debitorUser == null) {
            throw new NotFoundUserException("상대방 사용자를 찾을 수 없습니다.");
        }

        // 대여 생성
        Agreement agreement = Agreement.builder()
                .itemId(req.getItemId())
                .status(AgreementStatus.PENDING)
                .amount(req.getAmount())
                .dueAt(req.getDueAt())
                .terms(req.getTerms())
                .createdAt(LocalDateTime.now())
                .build();

        checkInsertResult(agreementMapper.insert(agreement), "대여 계약 등록에 실패했습니다.");

        // 채권자 생성
        AgreementParty agreementPartyCreditor = AgreementParty.builder()
                .agreementId(agreement.getId())
                .userId(userId)
                .role(AgreementPartyRole.CREDITOR)
                .confirmAt(LocalDateTime.now())
                .build();

        checkInsertResult(agreementPartyMapper.insert(agreementPartyCreditor),
                "채권자 정보 등록에 실패했습니다.");

        // 채무자 생성
        AgreementParty agreementPartyDebtor = AgreementParty.builder()
                .agreementId(agreement.getId())
                .userId(req.getDebtorUserId())
                .role(AgreementPartyRole.DEBTOR)
                .build();

        checkInsertResult(agreementPartyMapper.insert(agreementPartyDebtor),
                "채무자 정보 등록에 실패했습니다.");

        AgreementPartyInfoDTO creditor = AgreementPartyInfoDTO.from(agreementPartyCreditor,
                UserSimpleInfoDTO.from(creditorUser));
        AgreementPartyInfoDTO debtor = AgreementPartyInfoDTO.from(agreementPartyDebtor,
                UserSimpleInfoDTO.from(debitorUser));

        return AgreementResponseDTO.from(agreement, creditor, debtor);
    }

    private void checkInsertResult(int result, String errorMessage) {
        if (result < 1) {
            throw new NotInsertAgreementException(errorMessage);
        }
    }
}
