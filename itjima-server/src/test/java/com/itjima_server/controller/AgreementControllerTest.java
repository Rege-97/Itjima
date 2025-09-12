package com.itjima_server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.item.ItemType;
import com.itjima_server.domain.transaction.TransactionStatus;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.dto.transaction.request.TransactionCreateRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.service.AgreementService;
import com.itjima_server.service.ItemService;
import com.itjima_server.service.AuthService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AgreementController 통합 테스트")
public class AgreementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private AgreementService agreementService;

    private Long creditorId;
    private Long debtorId;
    private String creditorAccessToken;
    private String debtorAccessToken;
    private Long itemId;

    @BeforeEach
    void setupUsersAndItem() {
        // 1) 회원 2명 생성
        UserRegisterRequestDTO reg1 = new UserRegisterRequestDTO();
        reg1.setName("Creditor");
        reg1.setEmail("creditor@example.com");
        reg1.setPassword("password123!");
        reg1.setPhone("01011112222");
        UserResponseDTO creditor = authService.register(reg1);
        UserRegisterRequestDTO reg2 = new UserRegisterRequestDTO();
        reg2.setName("Debtor");
        reg2.setEmail("debtor@example.com");
        reg2.setPassword("password123!");
        reg2.setPhone("01011113333");
        UserResponseDTO debtor = authService.register(reg2);

        creditorId = creditor.getId();
        debtorId = debtor.getId();

        // 2) 로그인 후 토큰
        UserLoginResponseDTO creditorLogin =
                authService.login(new UserLoginRequestDTO("creditor@example.com", "password123!"));
        UserLoginResponseDTO debtorLogin =
                authService.login(new UserLoginRequestDTO("debtor@example.com", "password123!"));
        creditorAccessToken = creditorLogin.getAccessToken();
        debtorAccessToken = debtorLogin.getAccessToken();

        // 3) 채권자 물품 1개 생성
        ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
        itemReq.setType(ItemType.OBJECT);
        itemReq.setTitle("대여 품목 A");
        itemReq.setDescription("설명 A");
        ItemResponseDTO itemRes = itemService.create(itemReq, creditorId);
        itemId = itemRes.getId();
    }

    @Nested
    @DisplayName("대여 생성 API")
    class CreateApi {

        @Test
        @DisplayName("성공")
        void create_success() throws Exception {
            // given
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("10000.00"));
            req.setDueAt(LocalDate.now().plusDays(3));
            req.setTerms("3일 대여");
            req.setDebtorUserId(debtorId);

            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions ra = mockMvc.perform(post("/api/agreements")
                    .header("Authorization", "Bearer " + creditorAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            ra.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message", containsString("대여 요청 성공")))
                    .andExpect(jsonPath("$.data.itemId").value(itemId))
                    .andExpect(jsonPath("$.data.status").value(AgreementStatus.PENDING.name()))
                    .andExpect(jsonPath("$.data.creditor.user.id").value(creditorId))
                    .andExpect(jsonPath("$.data.debtor.user.id").value(debtorId))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 자기 자신에게 요청")
        void create_fail_self_request() throws Exception {
            // given
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("1000"));
            req.setDueAt(LocalDate.now().plusDays(1));
            req.setTerms("terms");
            req.setDebtorUserId(creditorId); // 자기 자신

            String json = objectMapper.writeValueAsString(req);

            // when & then
            mockMvc.perform(post("/api/agreements")
                            .header("Authorization", "Bearer " + creditorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - debtor 없음")
        void create_fail_debtor_not_found() throws Exception {
            // given
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("1000"));
            req.setDueAt(LocalDate.now().plusDays(1));
            req.setTerms("terms");
            req.setDebtorUserId(999999L);

            String json = objectMapper.writeValueAsString(req);

            // when & then
            mockMvc.perform(post("/api/agreements")
                            .header("Authorization", "Bearer " + creditorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - item 없음")
        void create_fail_item_not_found() throws Exception {
            // given
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(999999L);
            req.setAmount(new BigDecimal("1000"));
            req.setDueAt(LocalDate.now().plusDays(1));
            req.setTerms("terms");
            req.setDebtorUserId(debtorId);

            String json = objectMapper.writeValueAsString(req);

            // when & then
            mockMvc.perform(post("/api/agreements")
                            .header("Authorization", "Bearer " + creditorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - validation (dueAt 과거)")
        void create_fail_validation_dueAt_past() throws Exception {
            // given
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("1000.12"));
            req.setDueAt(LocalDate.now().minusDays(3)); // 과거
            req.setTerms("terms");
            req.setDebtorUserId(debtorId);

            String json = objectMapper.writeValueAsString(req);

            // when & then
            mockMvc.perform(post("/api/agreements")
                            .header("Authorization", "Bearer " + creditorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 승인 API")
    class AcceptApi {

        private Long agreementId;

        @BeforeEach
        void prepareAgreement() {
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("5000"));
            req.setDueAt(LocalDate.now().plusDays(2));
            req.setTerms("ok");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();
        }

        @Test
        @DisplayName("성공")
        void accept_success() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(AgreementStatus.ACCEPTED.name()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 채무자가 아님")
        void accept_forbidden_when_not_debtor() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 이미 처리된 상태에서 재승인 시")
        void accept_conflict_when_already_processed() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 거절 API")
    class RejectApi {

        private Long agreementId;

        @BeforeEach
        void prepareAgreement() {
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("7000"));
            req.setDueAt(LocalDate.now().plusDays(5));
            req.setTerms("rejectable");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();
        }

        @Test
        @DisplayName("성공")
        void reject_success() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/reject", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(AgreementStatus.REJECTED.name()))
                    .andDo(print());

            ItemResponseDTO after = itemService.get(itemId, creditorId);
            org.junit.jupiter.api.Assertions.assertEquals(ItemStatus.AVAILABLE, after.getStatus());
        }

        @Test
        @DisplayName("실패")
        void reject_forbidden_when_not_debtor() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/reject", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 취소 API")
    class CancelApi {

        private Long agreementId;

        @BeforeEach
        void prepareAgreement() {
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("7000"));
            req.setDueAt(LocalDate.now().plusDays(5));
            req.setTerms("cancelable");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();
        }

        @Test
        @DisplayName("성공")
        void cancel_success() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/cancel", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(AgreementStatus.CANCELED.name()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 채권자만 가능")
        void cancel_forbidden_when_not_creditor() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/cancel", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 완료 API")
    class CompleteApi {

        private Long agreementId;

        @BeforeEach
        void prepareAcceptedAgreement() {
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("12000"));
            req.setDueAt(LocalDate.now().plusDays(2));
            req.setTerms("complete later");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();

            try {
                mockMvc.perform(put("/api/agreements/{id}/accept", agreementId)
                                .header("Authorization", "Bearer " + debtorAccessToken))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("성공")
        void complete_success() throws Exception {
            // when & then
            mockMvc.perform(put("/api/agreements/{id}/complete", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(AgreementStatus.COMPLETED.name()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - PENDING에서 완료 시도")
        void complete_conflict_from_pending() throws Exception {
            // given
            AgreementCreateRequestDTO req;
            req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("5000"));
            req.setDueAt(LocalDate.now().plusDays(5));
            req.setTerms("pending");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.complete(creditorId, agreementId);
            Long pendingAgreementId = created.getId();

            // when & then
            mockMvc.perform(put("/api/agreements/{id}/complete", pendingAgreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 상세 조회 API")
    class GetApi {

        private Long agreementId;

        @BeforeEach
        void prepareAgreement() {
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(itemId);
            req.setAmount(new BigDecimal("9000"));
            req.setDueAt(LocalDate.now().plusDays(3));
            req.setTerms("detail");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();
        }

        @Test
        @DisplayName("성공")
        void get_success_as_creditor() throws Exception {
            // when & then
            mockMvc.perform(get("/api/agreements/{id}", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.agreementId").value(agreementId))
                    .andExpect(jsonPath("$.data.creditor.id").value(creditorId))
                    .andExpect(jsonPath("$.data.debtor.id").value(debtorId))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 제3자 접근")
        void get_forbidden_for_third_party() throws Exception {
            // given
            UserRegisterRequestDTO reg3 = new UserRegisterRequestDTO();
            reg3.setName("Third");
            reg3.setEmail("third@example.com");
            reg3.setPassword("password123!");
            reg3.setPhone("01022223333");
            authService.register(reg3);
            UserLoginResponseDTO thirdLogin =
                    authService.login(new UserLoginRequestDTO("third@example.com", "password123!"));

            // when & then
            mockMvc.perform(get("/api/agreements/{id}", agreementId)
                            .header("Authorization", "Bearer " + thirdLogin.getAccessToken()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID")
        void get_not_found() throws Exception {
            // when & then
            mockMvc.perform(get("/api/agreements/{id}", 999999L)
                            .header("Authorization", "Bearer " + creditorAccessToken))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("대여 목록 조회 API")
    class ListApi {

        @BeforeEach
        void prepareManyAgreements() {
            for (int i = 0; i < 3; i++) {
                AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();

                ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
                itemReq.setType(ItemType.OBJECT);
                itemReq.setTitle("아이템-" + i);
                itemReq.setDescription("설명-" + i);
                long newItemId = itemService.create(itemReq, creditorId).getId();
                req.setItemId(newItemId);

                req.setAmount(new BigDecimal("1000"));
                req.setDueAt(LocalDate.now().plusDays(7));
                req.setTerms("t");
                req.setDebtorUserId(debtorId);
                agreementService.create(creditorId, req);
            }
        }

        @Test
        @DisplayName("성공 - 채권자 관점 첫 페이지 size=2")
        void list_first_page_as_creditor() throws Exception {
            // when & then
            ResultActions ra = mockMvc.perform(get("/api/agreements")
                    .header("Authorization", "Bearer " + creditorAccessToken)
                    .param("role", AgreementPartyRole.CREDITOR.name())
                    .param("size", "2"));

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items.length()", is(2)))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.lastId", notNullValue()))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("상환 요청 API")
    class CreateTransactionApi {

        private long agreementId;

        @BeforeEach
        void setUpMoneyAgreementAccepted() {
            // 금전 아이템 생성 (채권자 소유)
            ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
            itemReq.setType(ItemType.MONEY);
            itemReq.setTitle("금전 대여 아이템");
            itemReq.setDescription("설명");
            ItemResponseDTO itemRes = itemService.create(itemReq, creditorId);
            long moneyItemId = itemRes.getId();

            // 대여 생성(채권자 → 채무자)
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(moneyItemId);
            req.setAmount(new BigDecimal("10000.00"));
            req.setDueAt(LocalDate.now().plusDays(7));
            req.setTerms("money-only");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();

            // 채무자 승인 → ACCEPTED
            agreementService.accept(debtorId, agreementId);
        }

        @Test
        @DisplayName("성공")
        void create_transaction_success() throws Exception {
            // given
            TransactionCreateRequestDTO body = new TransactionCreateRequestDTO();
            body.setAmount(new BigDecimal("5000.00"));
            String json = objectMapper.writeValueAsString(body);

            // when
            ResultActions ra = mockMvc.perform(
                    post("/api/agreements/{id}/transactions", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
            );

            // then
            ra.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message", containsString("상환 요청 성공")))
                    .andExpect(jsonPath("$.data.amount").value(5000.00))
                    .andExpect(jsonPath("$.data.status").value(TransactionStatus.PENDING.name()));
        }

        @Test
        @DisplayName("실패 - 금전 대여가 아님(OBJECT)")
        void create_transaction_fail_not_money_item() throws Exception {
            // given: 물건 아이템으로 새 대여 생성
            ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
            itemReq.setType(ItemType.OBJECT);
            itemReq.setTitle("물건 아이템");
            itemReq.setDescription("설명");
            ItemResponseDTO obj = itemService.create(itemReq, creditorId);

            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(obj.getId());
            req.setAmount(new BigDecimal("10000.00"));
            req.setDueAt(LocalDate.now().plusDays(7));
            req.setTerms("object");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            long objectAgreementId = created.getId();
            agreementService.accept(debtorId, objectAgreementId);

            TransactionCreateRequestDTO body = new TransactionCreateRequestDTO();
            body.setAmount(new BigDecimal("1000.00"));
            String json = objectMapper.writeValueAsString(body);

            // when & then
            mockMvc.perform(post("/api/agreements/{id}/transactions", objectAgreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("실패 - 남은 금액 초과")
        void create_transaction_fail_over_amount() throws Exception {
            // given
            TransactionCreateRequestDTO body = new TransactionCreateRequestDTO();
            body.setAmount(new BigDecimal("20000.00")); // 총액 10,000 초과
            String json = objectMapper.writeValueAsString(body);

            mockMvc.perform(post("/api/agreements/{id}/transactions", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("실패 - 본인 권한 아님(채권자가 호출)")
        void create_transaction_fail_forbidden() throws Exception {
            TransactionCreateRequestDTO body = new TransactionCreateRequestDTO();
            body.setAmount(new BigDecimal("1000.00"));
            String json = objectMapper.writeValueAsString(body);

            mockMvc.perform(post("/api/agreements/{id}/transactions", agreementId)
                            .header("Authorization", "Bearer " + creditorAccessToken) // 채권자 토큰
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden()) // NotAuthorException → 403
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("실패 - 요청 본문 없음")
        void create_transaction_fail_missing_body() throws Exception {
            mockMvc.perform(post("/api/agreements/{id}/transactions", agreementId)
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("상환(거래) 목록 조회 API")
    class GetTransactionListApi {

        private long agreementId;

        @BeforeEach
        void setUpAcceptedMoneyAgreementWithTransactions() {
            // 1) 금전 아이템 생성
            ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
            itemReq.setType(ItemType.MONEY);
            itemReq.setTitle("금전 대여 아이템");
            itemReq.setDescription("설명");
            ItemResponseDTO itemRes = itemService.create(itemReq, creditorId);
            long moneyItemId = itemRes.getId();

            // 2) 대여 생성 후 채무자가 수락하여 ACCEPTED로 변경
            AgreementCreateRequestDTO req = new AgreementCreateRequestDTO();
            req.setItemId(moneyItemId);
            req.setAmount(new BigDecimal("10000.00"));
            req.setDueAt(LocalDate.now().plusDays(7));
            req.setTerms("money-only");
            req.setDebtorUserId(debtorId);
            AgreementResponseDTO created = agreementService.create(creditorId, req);
            agreementId = created.getId();
            agreementService.accept(debtorId, agreementId);

            // 3) 거래 3건 생성
            agreementService.createTransaction(debtorId, agreementId, new BigDecimal("1000"));
            agreementService.createTransaction(debtorId, agreementId, new BigDecimal("1500"));
            agreementService.createTransaction(debtorId, agreementId, new BigDecimal("2000"));
        }

        @Test
        @DisplayName("성공 - 첫 페이지, 다음 페이지 있음(size=2)")
        void get_transaction_list_success_has_next() throws Exception {
            ResultActions ra = mockMvc.perform(
                    get("/api/agreements/{id}/transactions", agreementId)
                            .param("size", "2")
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .accept(MediaType.APPLICATION_JSON)
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.items", hasSize(2)))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.lastId", notNullValue()));
        }

        @Test
        @DisplayName("성공 - 다음 페이지 없음(lastId로 이어받기)")
        void get_transaction_list_success_no_next() throws Exception {
            MvcResult firstResult = mockMvc.perform(
                            get("/api/agreements/{id}/transactions", agreementId)
                                    .param("size", "2")
                                    .header("Authorization", "Bearer " + debtorAccessToken)
                                    .accept(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isOk())
                    .andReturn();

            String body = firstResult.getResponse().getContentAsString();
            String lastId = body.replaceAll(".*\"lastId\"\\s*:\\s*(\\d+).*", "$1");

            ResultActions ra = mockMvc.perform(
                    get("/api/agreements/{id}/transactions", agreementId)
                            .param("size", "2")
                            .param("lastId", lastId)
                            .header("Authorization", "Bearer " + debtorAccessToken)
                            .accept(MediaType.APPLICATION_JSON)
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.items", hasSize(1))) // 총 3건 → 2 + 1
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.lastId", notNullValue()));
        }
    }
}
