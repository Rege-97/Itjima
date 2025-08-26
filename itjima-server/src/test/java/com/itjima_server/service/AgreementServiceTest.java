package com.itjima_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.agreement.Agreement;
import com.itjima_server.domain.agreement.AgreementParty;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.Item;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.exception.agreement.NotFoundAgreementException;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.NotInsertException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.item.NotFoundItemException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.AgreementPartyMapper;
import com.itjima_server.mapper.ItemMapper;
import com.itjima_server.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
public class AgreementServiceTest {

    @InjectMocks
    private AgreementService agreementService;

    @Mock
    private AgreementMapper agreementMapper;
    @Mock
    private AgreementPartyMapper agreementPartyMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ItemMapper itemMapper;

    private User creditor; // 채권자 (ID: 1)
    private User debtor;   // 채무자 (ID: 2)
    private Item item;

    @BeforeEach
    void setUp() {
        creditor = User.builder().id(1L).name("Creditor").build();
        debtor = User.builder().id(2L).name("Debtor").build();
        item = Item.builder().id(10L).userId(creditor.getId()).title("Test Item")
                .status(ItemStatus.AVAILABLE).build();
    }

    @Nested
    @DisplayName("대여 계약 생성 로직")
    class CreateAgreementTest {

        private AgreementCreateRequestDTO agreementCreateRequestDTO;

        @BeforeEach
        void setUp() {
            agreementCreateRequestDTO = new AgreementCreateRequestDTO();
            agreementCreateRequestDTO.setItemId(item.getId());
            agreementCreateRequestDTO.setDebtorUserId(debtor.getId());
            agreementCreateRequestDTO.setDueAt(LocalDateTime.now().plusDays(7));
        }

        @Test
        @DisplayName("성공")
        void create_success() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);
            when(agreementMapper.insert(any(Agreement.class))).thenReturn(1);
            when(agreementPartyMapper.insert(any(AgreementParty.class))).thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class))).thenReturn(1);

            // when
            AgreementResponseDTO res = agreementService.create(creditor.getId(),
                    agreementCreateRequestDTO);

            // then
            assertNotNull(res);
            assertEquals(AgreementStatus.PENDING, res.getStatus());
            assertEquals(creditor.getName(), res.getCreditor().getUser().getName());
            assertEquals(debtor.getName(), res.getDebtor().getUser().getName());
            verify(userMapper, times(2)).findById(anyLong());
            verify(itemMapper, times(1)).findById(anyLong());
            verify(agreementMapper, times(1)).insert(any(Agreement.class));
            verify(itemMapper, times(1)).updateStatusById(anyLong(), any(ItemStatus.class));
        }

        @Test
        @DisplayName("실패 - 자신에게 요청")
        void create_fail_to_self() {
            // given
            agreementCreateRequestDTO.setDebtorUserId(creditor.getId());

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - 상대방을 찾을 수 없음")
        void create_fail_when_debtor_not_found() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(null);

            // when & then
            assertThrows(NotFoundUserException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - 대여물품을 찾을 수 없음")
        void create_fail_when_item_not_found() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(null);

            // when & then
            assertThrows(NotFoundItemException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - 자신이 등록한 물품이 아님")
        void create_fail_when_not_author_item() {
            // given
            item.setUserId(20L);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);

            // then & when
            assertThrows(NotAuthorException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - 대여 물품이 대여 가능 상태가 아님")
        void create_fail_when_not_available_item() {
            // given
            item.setStatus(ItemStatus.ON_LOAN);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);

            // then & when
            assertThrows(InvalidStateException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 등록 실패)")
        void create_fail_when_agreement_insert_return_zero() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);
            when(agreementMapper.insert(any(Agreement.class))).thenReturn(0);

            // when & then
            assertThrows(NotInsertException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 구성원 등록 실패)")
        void create_fail_when_agreement_party_insert_return_zero() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);
            when(agreementMapper.insert(any(Agreement.class))).thenReturn(1);
            when(agreementPartyMapper.insert(any(AgreementParty.class))).thenReturn(0);

            // when & then
            assertThrows(NotInsertException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 물품 상태 변경 실패)")
        void create_fail_when_item_status_update_return_zero() {
            // given
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);
            when(itemMapper.findById(item.getId())).thenReturn(item);
            when(agreementMapper.insert(any(Agreement.class))).thenReturn(1);
            when(agreementPartyMapper.insert(any(AgreementParty.class))).thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class))).thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.create(creditor.getId(), agreementCreateRequestDTO));
        }
    }

    @Nested
    @DisplayName("대여 승인 처리 로직")
    class AgreementAcceptTest {

        private Agreement agreement;
        AgreementParty creditorParty;
        AgreementParty debtorParty;

        @BeforeEach
        void setUp() {
            agreement = Agreement.builder()
                    .id(1L)
                    .itemId(item.getId())
                    .status(AgreementStatus.PENDING)
                    .dueAt(LocalDateTime.now().plusDays(7))
                    .terms("test")
                    .createdAt(LocalDateTime.now())
                    .build();
            creditorParty = AgreementParty.builder()
                    .id(1L)
                    .agreementId(agreement.getId())
                    .userId(creditor.getId())
                    .role(AgreementPartyRole.CREDITOR)
                    .build();
            debtorParty = AgreementParty.builder()
                    .id(2L)
                    .agreementId(agreement.getId())
                    .userId(debtor.getId())
                    .role(AgreementPartyRole.DEBTOR)
                    .build();
        }

        @Test
        @DisplayName("성공")
        void accept_success() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId())).thenReturn(
                    List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(),
                    any(AgreementStatus.class))).thenReturn(1);
            when(agreementPartyMapper.updateConfirmedAtById(anyLong(),
                    any(LocalDateTime.class))).thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class))).thenReturn(1);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);

            // when
            AgreementResponseDTO res = agreementService.accept(debtor.getId(),
                    agreement.getId());

            // then
            assertNotNull(res);
            assertEquals(agreement.getId(), res.getId());
            assertEquals(AgreementStatus.ACCEPTED, res.getStatus());
            verify(agreementMapper, times(1)).findById(agreement.getId());
            verify(agreementPartyMapper, times(1)).findByAgreementId(agreement.getId());
            verify(agreementMapper, times(1)).updateStatusById(anyLong(),
                    any(AgreementStatus.class));
            verify(agreementPartyMapper, times(1)).updateConfirmedAtById(anyLong(),
                    any(LocalDateTime.class));
            verify(itemMapper, times(1)).updateStatusById(anyLong(), any(ItemStatus.class));
            verify(userMapper, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("실패 - 대여 없음")
        void accept_fail_when_agreement_not_found() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(null);

            // when & then
            assertThrows(com.itjima_server.exception.agreement.NotFoundAgreementException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 요청자가 채무자가 아님")
        void accept_fail_when_not_debtor() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            AgreementParty fakeDebtor = AgreementParty.builder()
                    .id(2002L).agreementId(agreement.getId())
                    .userId(999L).role(AgreementPartyRole.DEBTOR).build();

            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, fakeDebtor));

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 현재 상태가 PENDING 아님")
        void accept_fail_when_status_invalid() {
            // given
            agreement.setStatus(AgreementStatus.REJECTED);
            when(agreementMapper.findById(eq(agreement.getId()))).thenReturn(agreement);

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(상태 업데이트 실패)")
        void accept_fail_when_update_status_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));

            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(확인일 업데이트 실패)")
        void accept_fail_when_update_confirm_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(agreementPartyMapper.updateConfirmedAtById(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 물품 상태 업데이트 실패)")
        void accept_fail_when_item_update_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(agreementPartyMapper.updateConfirmedAtById(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.accept(debtor.getId(), agreement.getId()));
        }
    }

    @Nested
    @DisplayName("대여 거절 처리 로직")
    class AgreementRejectTest {

        private Agreement agreement;
        private AgreementParty creditorParty;
        private AgreementParty debtorParty;

        @BeforeEach
        void setUp() {
            agreement = Agreement.builder()
                    .id(1L)
                    .itemId(item.getId())
                    .status(AgreementStatus.PENDING)
                    .build();

            creditorParty = AgreementParty.builder()
                    .id(1L).agreementId(agreement.getId())
                    .userId(creditor.getId()).role(AgreementPartyRole.CREDITOR).build();
            debtorParty = AgreementParty.builder()
                    .id(2L).agreementId(agreement.getId())
                    .userId(debtor.getId()).role(AgreementPartyRole.DEBTOR).build();
        }

        @Test
        @DisplayName("성공")
        void reject_success() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(1);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);

            // when
            AgreementResponseDTO res = agreementService.reject(debtor.getId(), agreement.getId());

            // then
            assertNotNull(res);
            assertEquals(AgreementStatus.REJECTED, res.getStatus());
            verify(agreementMapper, times(1)).findById(agreement.getId());
            verify(agreementPartyMapper, times(1)).findByAgreementId(agreement.getId());
            verify(agreementMapper, times(1)).updateStatusById(anyLong(),
                    any(AgreementStatus.class));
            verify(itemMapper, times(1)).updateStatusById(anyLong(), any(ItemStatus.class));
            verify(userMapper, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("실패 - 대여 없음")
        void reject_fail_when_agreement_not_found() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(null);

            // when & then
            assertThrows(com.itjima_server.exception.agreement.NotFoundAgreementException.class,
                    () -> agreementService.reject(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 요청자가 채무자가 아님")
        void reject_fail_not_debtor() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            AgreementParty fakeDebtor = AgreementParty.builder()
                    .id(3003L).agreementId(agreement.getId())
                    .userId(999L).role(AgreementPartyRole.DEBTOR).build();
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, fakeDebtor));

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> agreementService.reject(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 상태 업데이트 실패")
        void reject_fail_when_update_status_zero() {
            //given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.reject(debtor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 물품 상태 업데이트 실패")
        void reject_fail_when_item_update_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(0);

            // when
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.reject(debtor.getId(), agreement.getId()));
        }
    }

    @Nested
    @DisplayName("대여 취소 처리 로직")
    class AgreementCancelTest {

        private Agreement agreement;
        private AgreementParty creditorParty;
        private AgreementParty debtorParty;

        @BeforeEach
        void setUp() {
            agreement = Agreement.builder()
                    .id(1L)
                    .itemId(item.getId())
                    .status(AgreementStatus.PENDING)
                    .build();

            creditorParty = AgreementParty.builder()
                    .id(1L).agreementId(agreement.getId())
                    .userId(creditor.getId()).role(AgreementPartyRole.CREDITOR).build();
            debtorParty = AgreementParty.builder()
                    .id(2L).agreementId(agreement.getId())
                    .userId(debtor.getId()).role(AgreementPartyRole.DEBTOR).build();
        }

        @Test
        @DisplayName("성공")
        void cancel_success() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(1);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);

            // when
            AgreementResponseDTO res = agreementService.cancel(creditor.getId(), agreement.getId());

            // then
            assertNotNull(res);
            assertEquals(AgreementStatus.CANCELED, res.getStatus());
            verify(agreementMapper, times(1)).findById(agreement.getId());
            verify(agreementPartyMapper, times(1)).findByAgreementId(agreement.getId());
            verify(agreementMapper, times(1)).updateStatusById(anyLong(),
                    any(AgreementStatus.class));
            verify(itemMapper, times(1)).updateStatusById(anyLong(), any(ItemStatus.class));
            verify(userMapper, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("실패 - 대여 없음")
        void cancel_fail_when_agreement_not_found() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(null);

            // when & then
            assertThrows(com.itjima_server.exception.agreement.NotFoundAgreementException.class,
                    () -> agreementService.cancel(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 현재 상태가 PENDING 아님")
        void cancel_fail_status_invalid() {
            // given
            agreement.setStatus(AgreementStatus.ACCEPTED);
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> agreementService.cancel(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 권한 없음(채권자만 취소 가능)")
        void cancel_fail_not_creditor() {
            // given
            AgreementParty fakeCreditor = AgreementParty.builder()
                    .id(4003L)
                    .agreementId(agreement.getId())
                    .userId(999L)
                    .role(AgreementPartyRole.CREDITOR)
                    .build();
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(fakeCreditor, debtorParty));

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> agreementService.cancel(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(상태 업데이트 실패)")
        void cancel_fail_update_status_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.cancel(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 물품 상태 업데이트 실패)")
        void cancel_fail_item_update_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.cancel(creditor.getId(), agreement.getId()));
        }
    }

    @Nested
    @DisplayName("대여 완료 처리 로직")
    class AgreementCompleteTest {

        private Agreement agreement;
        private AgreementParty creditorParty;
        private AgreementParty debtorParty;

        @BeforeEach
        void setUp() {
            agreement = Agreement.builder()
                    .id(1L)
                    .itemId(item.getId())
                    .status(AgreementStatus.ACCEPTED)
                    .build();

            creditorParty = AgreementParty.builder()
                    .id(1L).agreementId(agreement.getId())
                    .userId(creditor.getId()).role(AgreementPartyRole.CREDITOR).build();
            debtorParty = AgreementParty.builder()
                    .id(2L).agreementId(agreement.getId())
                    .userId(debtor.getId()).role(AgreementPartyRole.DEBTOR).build();
        }

        @Test
        @DisplayName("성공")
        void complete_success() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(1);
            when(userMapper.findById(creditor.getId())).thenReturn(creditor);
            when(userMapper.findById(debtor.getId())).thenReturn(debtor);

            // when
            AgreementResponseDTO res = agreementService.complete(creditor.getId(),
                    agreement.getId());

            // then
            assertNotNull(res);
            assertEquals(AgreementStatus.COMPLETED, res.getStatus());
            verify(agreementMapper, times(1)).findById(agreement.getId());
            verify(agreementPartyMapper, times(1)).findByAgreementId(agreement.getId());
            verify(agreementMapper, times(1)).updateStatusById(anyLong(),
                    any(AgreementStatus.class));
            verify(itemMapper, times(1)).updateStatusById(anyLong(), any(ItemStatus.class));
            verify(userMapper, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("실패 - 대여 없음")
        void complete_fail_when_agreement_not_found() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(null);

            // when & then
            assertThrows(com.itjima_server.exception.agreement.NotFoundAgreementException.class,
                    () -> agreementService.complete(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 현재 상태가 ACCEPTED/OVERDUE 아님")
        void complete_fail_status_invalid() {
            // given
            agreement.setStatus(AgreementStatus.REJECTED);
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);

            // when & then
            assertThrows(InvalidStateException.class,
                    () -> agreementService.complete(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - 권한 없음(채권자만 완료 가능)")
        void complete_fail_not_creditor() {
            // given
            AgreementParty fakeCreditor = AgreementParty.builder()
                    .id(5003L).agreementId(agreement.getId())
                    .userId(999L).role(AgreementPartyRole.CREDITOR).build();
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(fakeCreditor, debtorParty));

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> agreementService.complete(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(상태 업데이트 실패)")
        void complete_fail_update_status_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.complete(creditor.getId(), agreement.getId()));
        }

        @Test
        @DisplayName("실패 - DB 오류(대여 물품 상태 업데이트 실패)")
        void complete_fail_item_update_zero() {
            // given
            when(agreementMapper.findById(agreement.getId())).thenReturn(agreement);
            when(agreementPartyMapper.findByAgreementId(agreement.getId()))
                    .thenReturn(List.of(creditorParty, debtorParty));
            when(agreementMapper.updateStatusById(anyLong(), any(AgreementStatus.class)))
                    .thenReturn(1);
            when(itemMapper.updateStatusById(anyLong(), any(ItemStatus.class)))
                    .thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> agreementService.complete(creditor.getId(), agreement.getId()));
        }
    }

    @Nested
    @DisplayName("대여 계약 조회 로직")
    class GetAgreementTest {

        private AgreementDetailDTO agreementDetailDTO;
        private Long agreementId;

        @BeforeEach
        void setUp() {
            agreementId = 1L;
            agreementDetailDTO = new AgreementDetailDTO();
            agreementDetailDTO.setAgreementId(agreementId);
            agreementDetailDTO.setCreditorId(creditor.getId());
            agreementDetailDTO.setDebtorId(debtor.getId());
        }

        @Test
        @DisplayName("상세 조회 성공")
        void get_detail_success() {
            // given
            when(agreementMapper.findDetailById(agreementId)).thenReturn(agreementDetailDTO);

            // when
            AgreementDetailResponseDTO res = agreementService.get(creditor.getId(), agreementId);

            // then
            assertNotNull(res);
            assertEquals(agreementId, res.getAgreementId());
            verify(agreementMapper, times(1)).findDetailById(agreementId);
        }

        @Test
        @DisplayName("상세 조회 실패 - 계약 없음")
        void get_detail_fail_not_found() {
            // given
            when(agreementMapper.findDetailById(agreementId)).thenReturn(null);

            // when & then
            assertThrows(NotFoundAgreementException.class,
                    () -> agreementService.get(creditor.getId(), agreementId));
        }

        @Test
        @DisplayName("상세 조회 실패 - 권한 없음")
        void get_detail_fail_not_author() {
            // given
            long otherUserId = 99L;
            when(agreementMapper.findDetailById(agreementId)).thenReturn(agreementDetailDTO);

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> agreementService.get(otherUserId, agreementId));
        }

        @Test
        @DisplayName("목록 조회 성공")
        void get_list_success() {
            // given
            int size = 5;
            when(agreementMapper.findByUserId(creditor.getId(), "CREDITOR", null, size + 1))
                    .thenReturn(List.of(new AgreementDetailDTO()));

            // when
            PagedResultDTO<?> res = agreementService.getList(creditor.getId(), null, size,
                    AgreementPartyRole.CREDITOR);

            // then
            assertNotNull(res);
            assertEquals(1, res.getItems().size());
            assertFalse(res.isHasNext());
        }

        @Test
        @DisplayName("목록 조회 성공 - 다음 페이지 있음")
        void get_list_success_has_next() {
            // given
            int size = 2;
            AgreementDetailDTO agreementDetailDTO1 = new AgreementDetailDTO();
            agreementDetailDTO1.setAgreementId(agreementId);
            agreementDetailDTO1.setCreditorId(creditor.getId());
            agreementDetailDTO1.setDebtorId(debtor.getId());

            AgreementDetailDTO agreementDetailDTO2 = new AgreementDetailDTO();
            agreementDetailDTO2.setAgreementId(agreementId);
            agreementDetailDTO2.setCreditorId(creditor.getId());
            agreementDetailDTO2.setDebtorId(debtor.getId());

            AgreementDetailDTO agreementDetailDTO3 = new AgreementDetailDTO();
            agreementDetailDTO3.setAgreementId(agreementId);
            agreementDetailDTO3.setCreditorId(creditor.getId());
            agreementDetailDTO3.setDebtorId(debtor.getId());

            List<AgreementDetailDTO> dummyList = Arrays.asList(
                    agreementDetailDTO1, agreementDetailDTO2, agreementDetailDTO3
            );
            when(agreementMapper.findByUserId(creditor.getId(), "CREDITOR", null, size + 1))
                    .thenReturn(new ArrayList<>(dummyList));

            // when
            PagedResultDTO<?> result = agreementService.getList(creditor.getId(), null, size,
                    AgreementPartyRole.CREDITOR);

            // then
            assertNotNull(result);
            assertEquals(size, result.getItems().size());
            assertTrue(result.isHasNext());
        }
    }
}


