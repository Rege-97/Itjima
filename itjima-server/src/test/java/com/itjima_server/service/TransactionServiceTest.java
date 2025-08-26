package com.itjima_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 단위 테스트")
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AgreementMapper agreementMapper;
    @Mock
    private AgreementPartyMapper agreementPartyMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private TransactionMapper transactionMapper;

    private Long creditorId;
    private Long debtorId;
    private Long agreementId;
    private Long itemId;
    private Long transactionId;

    private Agreement agreement;

    private AgreementParty creditorParty;
    private AgreementParty debtorParty;

    private Transaction transaction;

    @BeforeEach
    void init() {
        creditorId = 1L;
        debtorId = 2L;
        agreementId = 100L;
        itemId = 50L;
        transactionId = 1000L;

        agreement = Agreement.builder()
                .id(agreementId)
                .itemId(itemId)
                .status(AgreementStatus.ACCEPTED)
                .amount(new BigDecimal("10000.00"))
                .dueAt(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .build();

        creditorParty = AgreementParty.builder()
                .id(10L)
                .agreementId(agreementId)
                .userId(creditorId)
                .role(AgreementPartyRole.CREDITOR)
                .confirmAt(LocalDateTime.now())
                .build();

        debtorParty = AgreementParty.builder()
                .id(11L)
                .agreementId(agreementId)
                .userId(debtorId)
                .role(AgreementPartyRole.DEBTOR)
                .confirmAt(LocalDateTime.now())
                .build();

        transaction = Transaction.builder()
                .id(transactionId)
                .agreementId(agreementId)
                .amount(new BigDecimal("10000.00"))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("confirm()")
    class ConfirmTest {

        @Test
        @DisplayName("성공 - 상환 확정 + 완납")
        void confirm_success_and_complete() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(transactionMapper.updateStatusById(transactionId, TransactionStatus.CONFIRMED))
                    .thenReturn(1);
            // 방금 확정된 금액까지 합산되어 총액 == 대여금액
            when(transactionMapper.sumConfirmedAmountByAgreementId(agreementId))
                    .thenReturn(new BigDecimal("10000.00"));
            when(agreementMapper.updateStatusById(agreementId, AgreementStatus.COMPLETED))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(itemId, ItemStatus.AVAILABLE))
                    .thenReturn(1);

            // when
            TransactionResponseDTO res = transactionService.confirm(transactionId, creditorId);

            // then
            assertNotNull(res);
            assertEquals(TransactionStatus.CONFIRMED, res.getStatus());
            verify(transactionMapper, times(1))
                    .updateStatusById(transactionId, TransactionStatus.CONFIRMED);
            verify(transactionMapper, times(1))
                    .sumConfirmedAmountByAgreementId(agreementId);
            verify(agreementMapper, times(1))
                    .updateStatusById(agreementId, AgreementStatus.COMPLETED);
            verify(itemMapper, times(1))
                    .updateStatusById(itemId, ItemStatus.AVAILABLE);
        }

        @Test
        @DisplayName("성공 - 상환 확정 + 미완납")
        void confirm_success_without_complete() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(transactionMapper.updateStatusById(transactionId, TransactionStatus.CONFIRMED))
                    .thenReturn(1);
            // 아직 총 상환액 < 대여금액
            when(transactionMapper.sumConfirmedAmountByAgreementId(agreementId))
                    .thenReturn(new BigDecimal("5000.00"));

            // when
            TransactionResponseDTO res = transactionService.confirm(transactionId, creditorId);

            // then
            assertNotNull(res);
            assertEquals(TransactionStatus.CONFIRMED, res.getStatus());
            verify(agreementMapper, never())
                    .updateStatusById(anyLong(), any(AgreementStatus.class));
            verify(itemMapper, never())
                    .updateStatusById(anyLong(), any(ItemStatus.class));
        }

        @Test
        @DisplayName("실패 - 상환 요청 없음")
        void confirm_fail_not_found_transaction() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(null);

            // when & then
            assertThrows(NotFoundTransactionException.class,
                    () -> transactionService.confirm(transactionId, creditorId));
        }

        @Test
        @DisplayName("실패 - 이미 처리된 상태")
        void confirm_fail_invalid_state() {
            // given
            transaction.setStatus(TransactionStatus.REJECTED);
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> transactionService.confirm(transactionId, creditorId));
        }

        @Test
        @DisplayName("실패 - 권한 없음(채권자 아님)")
        void confirm_fail_not_author() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));

            // when & then (채무자 시도)
            assertThrows(NotAuthorException.class,
                    () -> transactionService.confirm(transactionId, debtorId));
        }

        @Test
        @DisplayName("실패 - 상태 업데이트 실패")
        void confirm_fail_update_failed() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(transactionMapper.updateStatusById(transactionId, TransactionStatus.CONFIRMED))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> transactionService.confirm(transactionId, creditorId));
        }

        @Test
        @DisplayName("실패 - 대여 없음")
        void confirm_fail_agreement_not_found() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(null);

            // when & then
            assertThrows(NotFoundAgreementException.class,
                    () -> transactionService.confirm(transactionId, creditorId));
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTest {

        @Test
        @DisplayName("성공 - 상환 거절")
        void reject_success() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(transactionMapper.updateStatusById(transactionId, TransactionStatus.REJECTED))
                    .thenReturn(1);

            // when
            TransactionResponseDTO res = transactionService.reject(transactionId, creditorId);

            // then
            assertNotNull(res);
            assertEquals(TransactionStatus.REJECTED, res.getStatus());
            verify(transactionMapper, times(1))
                    .updateStatusById(transactionId, TransactionStatus.REJECTED);
            verify(transactionMapper, never()).sumConfirmedAmountByAgreementId(anyLong());
            verify(agreementMapper, never()).updateStatusById(anyLong(),
                    any(AgreementStatus.class));
            verify(itemMapper, never()).updateStatusById(anyLong(), any(ItemStatus.class));
        }

        @Test
        @DisplayName("실패 - 이미 처리된 상태(대기 아님)")
        void reject_fail_invalid_state() {
            // given
            transaction.setStatus(TransactionStatus.CONFIRMED);
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> transactionService.reject(transactionId, creditorId));
        }

        @Test
        @DisplayName("실패 - 권한 없음(채권자 아님)")
        void reject_fail_not_author() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));

            // when & then (채무자 시도)
            assertThrows(NotAuthorException.class,
                    () -> transactionService.reject(transactionId, debtorId));
        }

        @Test
        @DisplayName("실패 - 상환 요청 없음")
        void reject_fail_not_found_transaction() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(null);

            // when & then
            assertThrows(NotFoundTransactionException.class,
                    () -> transactionService.reject(transactionId, creditorId));
        }

        @Test
        @DisplayName("실패 - 상태 업데이트 실패")
        void reject_fail_update_failed() {
            // given
            when(transactionMapper.findById(transactionId)).thenReturn(transaction);
            when(agreementMapper.findById(agreementId)).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreementId))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(transactionMapper.updateStatusById(transactionId, TransactionStatus.REJECTED))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> transactionService.reject(transactionId, creditorId));
        }
    }
}
