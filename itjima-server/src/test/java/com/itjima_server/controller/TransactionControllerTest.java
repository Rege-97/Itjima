package com.itjima_server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.item.ItemType;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.service.AgreementService;
import com.itjima_server.service.ItemService;
import com.itjima_server.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private AgreementService agreementService;

    private Long creditorId;
    private Long debtorId;
    private String creditorAccessToken;
    private String debtorAccessToken;
    private Long agreementId;
    private Long itemId;

    @BeforeEach
    void setupUsersItemAgreementAccepted() throws Exception {
        // 1) 사용자 2명 생성
        UserRegisterRequestDTO reg1 = new UserRegisterRequestDTO();
        reg1.setName("Creditor");
        reg1.setEmail("creditor_tx@example.com");
        reg1.setPassword("password123!");
        reg1.setPhone("01022223333");
        UserResponseDTO creditor = userService.register(reg1);

        UserRegisterRequestDTO reg2 = new UserRegisterRequestDTO();
        reg2.setName("Debtor");
        reg2.setEmail("debtor_tx@example.com");
        reg2.setPassword("password123!");
        reg2.setPhone("01022224444");
        UserResponseDTO debtor = userService.register(reg2);

        creditorId = creditor.getId();
        debtorId = debtor.getId();

        // 2) 로그인
        UserLoginResponseDTO creditorLogin =
                userService.login(
                        new UserLoginRequestDTO("creditor_tx@example.com", "password123!"));
        UserLoginResponseDTO debtorLogin =
                userService.login(new UserLoginRequestDTO("debtor_tx@example.com", "password123!"));
        creditorAccessToken = creditorLogin.getAccessToken();
        debtorAccessToken = debtorLogin.getAccessToken();

        // 3) 채권자 물품(MONEY) 생성
        ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
        itemReq.setType(ItemType.MONEY);
        itemReq.setTitle("금전 대여");
        itemReq.setDescription("테스트 금전");
        ItemResponseDTO itemRes = itemService.create(itemReq, creditorId);
        itemId = itemRes.getId();

        // 4) 대여 생성(서비스로 생성) → PENDING
        AgreementCreateRequestDTO agreeReq = new AgreementCreateRequestDTO();
        agreeReq.setItemId(itemId);
        agreeReq.setAmount(new BigDecimal("10000.00"));
        agreeReq.setDueAt(LocalDateTime.now().plusDays(3));
        agreeReq.setTerms("테스트 대여");
        agreeReq.setDebtorUserId(debtorId);
        AgreementResponseDTO created = agreementService.create(creditorId, agreeReq);
        agreementId = created.getId();

        // 5) 채무자 승인(엔드포인트) → ACCEPTED
        mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                        .header("Authorization", "Bearer " + debtorAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(AgreementStatus.ACCEPTED.name()));
    }

    private Long createPendingTransaction(BigDecimal amount) {
        TransactionResponseDTO tx = agreementService.createTransaction(debtorId, agreementId,
                amount);
        return tx.getId();
    }

    @Nested
    @DisplayName("상환 승인 API 테스트")
    class ConfirmTest {

        @Test
        @DisplayName("성공 - 확정 및 완납")
        @Rollback
        void confirm_success_and_complete() throws Exception {
            // given: 대여금액과 동일한 상환 요청 생성(PENDING)
            Long transactionId = createPendingTransaction(new BigDecimal("10000.00"));

            // when
            ResultActions ra = mockMvc.perform(put("/api/transactions/{id}/confirm", transactionId)
                    .header("Authorization", "Bearer " + creditorAccessToken));

            // then
            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message", containsString("상환 요청 승인 성공")))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                    .andDo(print());

            // 후속 상태 검증
            ItemResponseDTO after = itemService.get(itemId, creditorId);
            org.junit.jupiter.api.Assertions.assertEquals(ItemStatus.AVAILABLE, after.getStatus());
        }

        @Test
        @DisplayName("성공 - 확정하지만 미완납")
        void confirm_success_without_complete() throws Exception {
            // given: 일부 금액만 요청
            Long transactionId = createPendingTransaction(new BigDecimal("6000.00"));

            // when & then
            mockMvc.perform(put("/api/transactions/{id}/confirm", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 권한 없음(채무자가 호출)")
        void confirm_fail_forbidden() throws Exception {
            // given
            Long transactionId = createPendingTransaction(new BigDecimal("1000.00"));

            // when & then
            mockMvc.perform(put("/api/transactions/{id}/confirm", transactionId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 대상 없음")
        void confirm_fail_not_found() throws Exception {
            mockMvc.perform(put("/api/transactions/{id}/confirm", 999999L)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 이미 처리된 요청 재승인")
        void confirm_fail_conflict_when_already_processed() throws Exception {
            // given
            Long transactionId = createPendingTransaction(new BigDecimal("1000.00"));

            // 1차 확정 OK
            mockMvc.perform(put("/api/transactions/{id}/confirm", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk());

            // 2차 재확정 → 409
            mockMvc.perform(put("/api/transactions/{id}/confirm", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("상환 거절 API 테스트")
    class RejectTest {

        @Test
        @DisplayName("성공 - 거절")
        void reject_success() throws Exception {
            // given
            Long transactionId = createPendingTransaction(new BigDecimal("3000.00"));

            // when & then
            mockMvc.perform(put("/api/transactions/{id}/reject", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message", containsString("상환 요청 거절 성공")))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 권한 없음(채무자가 호출)")
        void reject_fail_forbidden() throws Exception {
            // given
            Long transactionId = createPendingTransaction(new BigDecimal("1000.00"));

            // when & then
            mockMvc.perform(put("/api/transactions/{id}/reject", transactionId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 대상 없음")
        void reject_fail_not_found() throws Exception {
            mockMvc.perform(put("/api/transactions/{id}/reject", 999999L)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 이미 처리된 요청 재거절")
        void reject_fail_conflict_when_already_processed() throws Exception {
            // given
            Long transactionId = createPendingTransaction(new BigDecimal("1000.00"));

            // 1차 거절 OK
            mockMvc.perform(put("/api/transactions/{id}/reject", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk());

            // 2차 재거절 → 409
            mockMvc.perform(put("/api/transactions/{id}/reject", transactionId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }
}
