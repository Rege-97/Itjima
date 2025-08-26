package com.itjima_server.service;

import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.agreement.AgreementParty;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.transaction.Transaction;
import com.itjima_server.domain.transaction.TransactionStatus;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.exception.agreement.NotFoundAgreementException;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.transaction.NotFoundTransactionException;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.ItemMapper;
import com.itjima_server.mapper.TransactionMapper;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AgreementMapper agreementMapper;
    private final AgreementPartyMapper agreementPartyMapper;
    private final ItemMapper itemMapper;
    private final TransactionMapper transactionMapper;

    @Transactional(rollbackFor = Exception.class)
    public TransactionResponseDTO confirm(Long id, Long userId) {
        // 상환 요청 검증
        Transaction transaction = findByTransactionId(id);
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidStateException("해당 상환 요청은 이미 처리되었습니다.");
        }

        // 대여 검증
        Agreement agreement = findByAgreementId(transaction.getAgreementId());
        verifyCanRespond(userId, agreement, AgreementPartyRole.CREDITOR,
                List.of(AgreementStatus.ACCEPTED, AgreementStatus.OVERDUE));

        // 승인 처리
        transaction.setStatus(TransactionStatus.CONFIRMED);
        checkUpdateResult(transactionMapper.updateStatusById(id, TransactionStatus.CONFIRMED),
                "상환 요청 승인에 실패했습니다.");

        BigDecimal totalPaidAmount = transactionMapper.sumConfirmedAmountByAgreementId(
                agreement.getId());
        if (agreement.getAmount().compareTo(totalPaidAmount) <= 0) {
            checkUpdateResult(
                    agreementMapper.updateStatusById(agreement.getId(), AgreementStatus.COMPLETED),
                    "대여 상태 변경에 실패했습니다.");
            checkUpdateResult(
                    itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                    "물품 상태 변경에 실패했습니다.");
        }

        return TransactionResponseDTO.from(transaction);
    }
// ==========================
    // 내부 유틸리티
    // ==========================

    /**
     * UPDATE 실행 결과 검증 유틸리티
     *
     * @param result       실행된 row 수
     * @param errorMessage 실패 시 예외 메시지
     * @throws UpdateFailedException update 실패 시
     */
    private void checkUpdateResult(int result, String errorMessage) {
        if (result < 1) {
            throw new UpdateFailedException(errorMessage);
        }
    }

    /**
     * ID로 상환요청 조회 (없으면 예외 발생)
     *
     * @param transactionId 대여 ID
     * @return 조회된 Transaction
     * @throws NotFoundTransactionException 상환요청이 존재하지 않을 경우
     */
    private Transaction findByTransactionId(Long transactionId) {
        Transaction transaction = transactionMapper.findById(transactionId);
        if (transaction == null) {
            throw new NotFoundTransactionException("해당 상환 요청을 찾을 수 없습니다.");
        }
        return transaction;
    }

    /**
     * ID로 대여 조회 (없으면 예외 발생)
     *
     * @param agreementId 대여 ID
     * @return 조회된 Agreement
     * @throws NotFoundAgreementException 대여가 존재하지 않을 경우
     */
    private Agreement findByAgreementId(Long agreementId) {
        Agreement agreement = agreementMapper.findById(agreementId);
        if (agreement == null) {
            throw new NotFoundAgreementException("해당 대여 요청을 찾을 수 없습니다.");
        }
        return agreement;
    }

    /**
     * 대여 상태와 파티 정보 검증 유틸리티
     *
     * @param agreement         대여 엔티티
     * @param agreementStatuses 허용되는 상태 목록
     * @return 대여 참여자 목록 (채권자, 채무자)
     * @throws InvalidStateException      상태가 허용되지 않을 경우
     * @throws NotFoundAgreementException 파티 정보가 없거나 올바르지 않을 경우
     */
    private List<AgreementParty> verifyAgreementStatus(Agreement agreement,
            List<AgreementStatus> agreementStatuses) {
        if (!agreementStatuses.contains(agreement.getStatus())) {
            throw new InvalidStateException("이미 처리된 대여 요청입니다.");
        }

        List<AgreementParty> parties = agreementPartyMapper.findByAgreementId(agreement.getId());
        if (parties == null || parties.size() != 2) {
            throw new NotFoundAgreementException("해당 대여의 사용자들을 찾을 수 없습니다.");
        }

        return parties;
    }

    /**
     * 특정 역할의 사용자가 응답할 수 있는지 검증 유틸리티
     *
     * @param userId             사용자 ID
     * @param agreement          대여 엔티티
     * @param agreementPartyRole 요구되는 역할 (CREDITOR/DEBTOR)
     * @param agreementStatuses  허용되는 상태 목록
     * @return 대여 참여자 목록 (채권자, 채무자)
     * @throws NotAuthorException 권한 없는 사용자인 경우
     */
    private List<AgreementParty> verifyCanRespond(Long userId,
            Agreement agreement, AgreementPartyRole agreementPartyRole,
            List<AgreementStatus> agreementStatuses) {
        List<AgreementParty> parties = verifyAgreementStatus(agreement,
                agreementStatuses);

        AgreementParty creditor = null;
        AgreementParty debtor = null;
        for (AgreementParty party : parties) {
            if (party.getRole() == AgreementPartyRole.CREDITOR) {
                creditor = party;
            } else {
                debtor = party;
            }
        }

        if (creditor == null || debtor == null) {
            throw new InvalidStateException("대여 참여자 정보 구성이 올바르지 않습니다.");
        }

        if (agreementPartyRole != null) {
            AgreementParty requiredParty =
                    (agreementPartyRole == AgreementPartyRole.CREDITOR) ? creditor : debtor;

            if (requiredParty.getUserId() != userId) {
                throw new NotAuthorException("해당 요청을 처리할 권한이 없습니다.");
            }
        } else {
            if (creditor.getUserId() != userId && debtor.getUserId() != userId) {
                throw new NotAuthorException("해당 요청을 처리할 권한이 없습니다.");
            }
        }

        return List.of(creditor, debtor);
    }

}
