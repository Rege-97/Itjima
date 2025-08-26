package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.agreement.AgreementParty;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.Item;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.item.ItemType;
import com.itjima_server.domain.transaction.Transaction;
import com.itjima_server.domain.transaction.TransactionStatus;
import com.itjima_server.domain.transaction.TransactionType;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementPartyInfoDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.dto.user.response.UserSimpleInfoDTO;
import com.itjima_server.exception.agreement.NotFoundAgreementException;
import com.itjima_server.exception.agreement.NotInsertAgreementException;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.item.NotFoundItemException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.ItemMapper;
import com.itjima_server.mapper.TransactionMapper;
import com.itjima_server.mapper.UserMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대여 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-26
 */
@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementMapper agreementMapper;
    private final AgreementPartyMapper agreementPartyMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final TransactionMapper transactionMapper;

    /**
     * 대여 생성 처리
     *
     * @param userId 로그인한 사용자 ID (채권자)
     * @param req    대여 생성 요청 DTO
     * @return 생성된 대여 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO create(Long userId, AgreementCreateRequestDTO req) {
        // 사용자 검증
        if (userId == req.getDebtorUserId()) {
            throw new InvalidStateException("자기 자신에게 대여 요청을 보낼 수 없습니다.");
        }

        User creditorUser = userMapper.findById(userId);
        User debtorUser = userMapper.findById(req.getDebtorUserId());
        if (debtorUser == null) {
            throw new NotFoundUserException("상대방 사용자를 찾을 수 없습니다.");
        }

        // 물품 검증
        Item item = itemMapper.findById(req.getItemId());
        if (item == null) {
            throw new NotFoundItemException("해당 물품을 찾을 수 없습니다.");
        }

        if (item.getUserId() != userId) {
            throw new NotAuthorException("자신이 등록한 물품만 대여할 수 있습니다.");
        }

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new InvalidStateException("해당 물품은 대여가 불가능 합니다.");
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

        checkInsertResult(agreementMapper.insert(agreement), "대여 등록에 실패했습니다.");

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

        checkUpdateResult(
                itemMapper.updateStatusById(item.getId(), ItemStatus.PENDING_APPROVAL),
                "물품 상태 변경에 실패했습니다.");

        AgreementPartyInfoDTO creditor = AgreementPartyInfoDTO.from(agreementPartyCreditor,
                UserSimpleInfoDTO.from(creditorUser));
        AgreementPartyInfoDTO debtor = AgreementPartyInfoDTO.from(agreementPartyDebtor,
                UserSimpleInfoDTO.from(debtorUser));

        return AgreementResponseDTO.from(agreement, creditor, debtor);
    }

    /**
     * 대여 승인 처리 (채무자만 가능)
     *
     * @param userId      로그인한 사용자 ID (채무자)
     * @param agreementId 승인할 대여 ID
     * @return 승인 처리된 대여 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO accept(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.DEBTOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 상태 업데이트: ACCEPTED
        agreement.setStatus(AgreementStatus.ACCEPTED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        // 채무자 확인일 업데이트
        agreementPartyDebtor.setConfirmAt(LocalDateTime.now());
        checkUpdateResult(
                agreementPartyMapper.updateConfirmedAtById(agreementPartyDebtor.getId(),
                        agreementPartyDebtor.getConfirmAt()), "대여 승인 등록에 실패했습니다.");

        // 물품 상태 업데이트: ON_LOAN
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.ON_LOAN),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    /**
     * 대여 거절 처리 (채무자만 가능)
     *
     * @param userId      로그인한 사용자 ID (채무자)
     * @param agreementId 거절할 대여 ID
     * @return 거절 처리된 대여 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO reject(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.DEBTOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 상태 업데이트: REJECTED
        agreement.setStatus(AgreementStatus.REJECTED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        // 물품 상태 업데이트: AVAILABLE
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    /**
     * 대여 취소 처리 (채권자만 가능, PENDING 상태에서 가능)
     *
     * @param userId      로그인한 사용자 ID (채권자)
     * @param agreementId 취소할 대여 ID
     * @return 취소 처리된 대여 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO cancel(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.CREDITOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 상태 업데이트: CANCELED
        agreement.setStatus(AgreementStatus.CANCELED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        // 물품 상태 업데이트: AVAILABLE
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    /**
     * 대여 완료 처리 (채권자만 가능, ACCEPTED/OVERDUE → COMPLETED)
     *
     * @param userId      로그인한 사용자 ID (채권자)
     * @param agreementId 완료 처리할 대여 ID
     * @return 완료 처리된 대여 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO complete(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.CREDITOR,
                List.of(AgreementStatus.ACCEPTED, AgreementStatus.OVERDUE));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 상태 업데이트: COMPLETED
        agreement.setStatus(AgreementStatus.COMPLETED);
        checkUpdateResult(agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        // 물품 상태 업데이트: AVAILABLE
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    /**
     * 대여 단건 상세 조회
     *
     * @param userId      로그인한 사용자 ID
     * @param agreementId 조회할 대여 ID
     * @return 대여 상세 응답 DTO
     */
    @Transactional(readOnly = true)
    public AgreementDetailResponseDTO get(Long userId, Long agreementId) {

        AgreementDetailDTO agreementDetailDTO = agreementMapper.findDetailById(agreementId);

        if (agreementDetailDTO == null) {
            throw new NotFoundAgreementException("해당 대여 요청을 찾을 수 없습니다.");
        }

        if (agreementDetailDTO.getCreditorId() != userId
                && agreementDetailDTO.getDebtorId() != userId) {
            throw new NotAuthorException("해당 요청을 처리할 권한이 없습니다.");
        }

        return AgreementDetailResponseDTO.from(agreementDetailDTO);
    }

    /**
     * 대여 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param userId 로그인한 사용자 ID
     * @param lastId 마지막으로 조회한 대여 ID
     * @param size   요청한 페이지 크기
     * @param role   조회 관점(CREDITOR/DEBTOR)
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Transactional(readOnly = true)
    public PagedResultDTO<?> getList(Long userId, Long lastId, int size, AgreementPartyRole role) {
        int sizePlusOne = size + 1;
        List<AgreementDetailDTO> AgreementList = agreementMapper.findByUserId(userId, role.name(),
                lastId, sizePlusOne);

        if (AgreementList == null || AgreementList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }

        boolean hasNext = false;
        if (AgreementList.size() == sizePlusOne) {
            hasNext = true;
            AgreementList.remove(size);
        }

        List<AgreementDetailResponseDTO> agreements = new ArrayList<>();

        for (AgreementDetailDTO agreementDetailDTO : AgreementList) {
            agreements.add(AgreementDetailResponseDTO.from(agreementDetailDTO));
        }

        lastId = AgreementList.get(AgreementList.size() - 1).getAgreementId();

        return PagedResultDTO.from(agreements, hasNext, lastId);
    }

    /**
     * 금전 상환 요청 (채무자만 가능)
     *
     * @param userId      로그인한 사용자
     * @param agreementId 대여 ID
     * @param amount      상환 금액
     * @return 요청 완료된 상환 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponseDTO createTransaction(Long userId, Long agreementId,
            BigDecimal amount) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        // 대여 물품 검증
        Item item = itemMapper.findById(agreement.getItemId());
        if (item != null && item.getType() != ItemType.MONEY) {
            throw new InvalidStateException("상환 요청은 금전 대여에만 이용할 수 있습니다.");
        }

        verifyCanRespond(userId, agreement, AgreementPartyRole.DEBTOR,
                List.of(AgreementStatus.ACCEPTED, AgreementStatus.OVERDUE));

        // 남은 상환 금액 검증
        BigDecimal totalPaidAmount = transactionMapper.sumConfirmedAmountByAgreementId(agreementId);
        BigDecimal remainingAmount = agreement.getAmount().subtract(totalPaidAmount);
        if (amount.compareTo(remainingAmount) > 0) {
            throw new InvalidStateException("요청 금액이 남은 잔액(" + remainingAmount + "원)을 초과할 수 없습니다.");
        }

        Transaction transaction = Transaction.builder()
                .agreementId(agreementId)
                .type(TransactionType.REPAYMENT)
                .amount(amount)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        checkUpdateResult(transactionMapper.insert(transaction), "상환 요청에 실패했습니다.");

        return TransactionResponseDTO.from(transaction);
    }

    @Transactional(readOnly = true)
    public PagedResultDTO<?> getTransactionList(Long userId, Long agreementId, Long lastId,
            int size) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        // 대여 물품 검증
        Item item = itemMapper.findById(agreement.getItemId());
        if (item != null && item.getType() != ItemType.MONEY) {
            throw new InvalidStateException("상환 요청은 금전 대여에만 이용할 수 있습니다.");
        }

        verifyCanRespond(userId, agreement, null,
                List.of(AgreementStatus.ACCEPTED, AgreementStatus.OVERDUE,
                        AgreementStatus.COMPLETED));

        int sizePlusOne = size + 1;

        List<Transaction> transactionList = transactionMapper.findByAgreementId(agreementId, lastId,
                sizePlusOne);

        if (transactionList == null || transactionList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }

        boolean hasNext = false;
        if (transactionList.size() == sizePlusOne) {
            hasNext = true;
            transactionList.remove(size);
        }

        List<TransactionResponseDTO> transactions = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            transactions.add(TransactionResponseDTO.from(transaction));
        }

        lastId = transactionList.get(transactionList.size() - 1).getId();

        return PagedResultDTO.from(transactions, hasNext, lastId);
    }

    // ==========================
    // 내부 유틸리티
    // ==========================

    /**
     * INSERT 실행 결과 검증 유틸리티
     *
     * @param result       실행된 row 수
     * @param errorMessage 실패 시 예외 메시지
     * @throws NotInsertAgreementException insert 실패 시
     */
    private void checkInsertResult(int result, String errorMessage) {
        if (result < 1) {
            throw new NotInsertAgreementException(errorMessage);
        }
    }

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

    /**
     * Agreement, 파티, 사용자 정보를 종합해 응답 DTO로 변환
     *
     * @param agreementPartyCreditor 채권자 파티
     * @param agreementPartyDebtor   채무자 파티
     * @param agreement              대여 엔티티
     * @return 대여 응답 DTO
     */
    private AgreementResponseDTO toAgreementResponseDTO(AgreementParty agreementPartyCreditor,
            AgreementParty agreementPartyDebtor, Agreement agreement) {
        User creditorUser = userMapper.findById(agreementPartyCreditor.getUserId());
        User debtorUser = userMapper.findById(agreementPartyDebtor.getUserId());

        AgreementPartyInfoDTO creditor = AgreementPartyInfoDTO.from(agreementPartyCreditor,
                UserSimpleInfoDTO.from(creditorUser));
        AgreementPartyInfoDTO debtor = AgreementPartyInfoDTO.from(agreementPartyDebtor,
                UserSimpleInfoDTO.from(debtorUser));

        return AgreementResponseDTO.from(agreement, creditor, debtor);
    }

}
