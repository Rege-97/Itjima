package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementParty;
import com.itjima_server.domain.AgreementPartyRole;
import com.itjima_server.domain.AgreementStatus;
import com.itjima_server.domain.Item;
import com.itjima_server.domain.ItemStatus;
import com.itjima_server.domain.User;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementPartyInfoDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
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
import com.itjima_server.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementMapper agreementMapper;
    private final AgreementPartyMapper agreementPartyMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

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

        checkUpdateResult(
                itemMapper.updateStatusById(item.getId(), ItemStatus.PENDING_APPROVAL),
                "물품 상태 변경에 실패했습니다.");

        AgreementPartyInfoDTO creditor = AgreementPartyInfoDTO.from(agreementPartyCreditor,
                UserSimpleInfoDTO.from(creditorUser));
        AgreementPartyInfoDTO debtor = AgreementPartyInfoDTO.from(agreementPartyDebtor,
                UserSimpleInfoDTO.from(debtorUser));

        return AgreementResponseDTO.from(agreement, creditor, debtor);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO accept(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.DEBTOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 승인 처리
        agreement.setStatus(AgreementStatus.ACCEPTED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        agreementPartyDebtor.setConfirmAt(LocalDateTime.now());
        checkUpdateResult(
                agreementPartyMapper.updateConfirmedAtById(agreementPartyDebtor.getId(),
                        agreementPartyDebtor.getConfirmAt()), "대여 승인 등록에 실패했습니다.");

        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.ON_LOAN),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO reject(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.DEBTOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 거절 처리
        agreement.setStatus(AgreementStatus.REJECTED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");

        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO cancel(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.CREDITOR, List.of(AgreementStatus.PENDING));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 취소 처리
        agreement.setStatus(AgreementStatus.CANCELED);
        checkUpdateResult(
                agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgreementResponseDTO complete(Long userId, Long agreementId) {
        // 대여 검증
        Agreement agreement = findByAgreementId(agreementId);

        List<AgreementParty> agreementPartyList = verifyCanRespond(userId, agreement,
                AgreementPartyRole.CREDITOR,
                List.of(AgreementStatus.ACCEPTED, AgreementStatus.OVERDUE));

        AgreementParty agreementPartyCreditor = agreementPartyList.get(0);
        AgreementParty agreementPartyDebtor = agreementPartyList.get(1);

        // 완료 처리
        agreement.setStatus(AgreementStatus.COMPLETED);
        checkUpdateResult(agreementMapper.updateStatusById(agreementId, agreement.getStatus()),
                "대여 상태 변경에 실패했습니다.");
        checkUpdateResult(itemMapper.updateStatusById(agreement.getItemId(), ItemStatus.AVAILABLE),
                "물품 상태 변경에 실패했습니다.");

        return toAgreementResponseDTO(agreementPartyCreditor, agreementPartyDebtor, agreement);
    }

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

    @Transactional(readOnly = true)
    public PagedResultDTO<?> getList(Long userId, Long agreementId, Long lastId,
            int size, AgreementPartyRole role) {

        int sizePlusOne = size + 1;
        List<AgreementDetailDTO> AgreementList = agreementMapper.findByUserId(userId, role,
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

    private void checkInsertResult(int result, String errorMessage) {
        if (result < 1) {
            throw new NotInsertAgreementException(errorMessage);
        }
    }

    private void checkUpdateResult(int result, String errorMessage) {
        if (result < 1) {
            throw new UpdateFailedException(errorMessage);
        }
    }

    private Agreement findByAgreementId(Long agreementId) {
        Agreement agreement = agreementMapper.findById(agreementId);
        if (agreement == null) {
            throw new NotFoundAgreementException("해당 대여 요청을 찾을 수 없습니다.");
        }
        return agreement;
    }

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
            throw new InvalidStateException("계약 참여자 정보 구성이 올바르지 않습니다.");
        }

        AgreementParty requiredParty =
                (agreementPartyRole == AgreementPartyRole.CREDITOR) ? creditor : debtor;

        if (requiredParty.getUserId() != userId) {
            throw new NotAuthorException("해당 요청을 처리할 권한이 없습니다.");
        }

        return List.of(creditor, debtor);
    }

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
