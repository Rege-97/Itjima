package com.itjima_server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.domain.item.ItemType;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.dto.user.request.UserChangeProfileRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.service.AuthService;
import com.itjima_server.service.ItemService;
import java.math.BigDecimal;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;
    @Autowired
    private ItemService itemService;

    private Long userAId;
    private String userAAccessToken;

    @BeforeEach
    void setup() {
        // 사용자 A 등록/로그인
        UserRegisterRequestDTO regA = new UserRegisterRequestDTO();
        regA.setName("UserA");
        regA.setEmail("usera@example.com");
        regA.setPassword("password123!");
        regA.setPhone("01011112222");
        UserResponseDTO a = authService.register(regA);
        userAId = a.getId();

        UserLoginResponseDTO aLogin =
                authService.login(new UserLoginRequestDTO("usera@example.com", "password123!"));
        userAAccessToken = aLogin.getAccessToken();
    }

    @Nested
    @DisplayName("최근 대여 사용자 목록 조회 API")
    class GetRecentPartnersApi {

        @BeforeEach
        void setUpRecentPartners() throws Exception {
            // 파트너 3명 등록 + 각각 대여 1건씩 생성
            for (int i = 1; i <= 3; i++) {
                // 1) 파트너 등록
                UserRegisterRequestDTO reg = new UserRegisterRequestDTO();
                reg.setName("Partner" + i);
                reg.setEmail("p" + i + "@example.com");
                reg.setPassword("password123!");
                reg.setPhone("0102222000" + i);
                UserResponseDTO partner = authService.register(reg);

                // 2) 아이템 생성 (로그인 사용자 userAId 소유)
                ItemCreateRequestDTO itemReq = new ItemCreateRequestDTO();
                itemReq.setType(ItemType.MONEY);
                itemReq.setTitle("대여 아이템 " + i);
                itemReq.setDescription("설명 " + i);
                ItemResponseDTO itemRes = itemService.create(itemReq, userAId);

                // 3) 대여 생성 (userA → 채권자, partner → 채무자) - 컨트롤러 경로로 생성
                AgreementCreateRequestDTO agrReq = new AgreementCreateRequestDTO();
                agrReq.setItemId(itemRes.getId());
                agrReq.setAmount(new BigDecimal("1000").multiply(BigDecimal.valueOf(i)));
                agrReq.setDueAt(LocalDateTime.now().plusDays(2L * i));
                agrReq.setTerms("조건 " + i);
                agrReq.setDebtorUserId(partner.getId());

                String json = objectMapper.writeValueAsString(agrReq);

                mockMvc.perform(post("/api/agreements")
                                .header("Authorization", "Bearer " + userAAccessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isCreated());
            }
        }

        @Test
        @DisplayName("성공 - 첫 페이지, 다음 페이지 있음(size=2)")
        void get_recent_partners_success_has_next() throws Exception {
            ResultActions ra = mockMvc.perform(
                    get("/api/users/recent-partners")
                            .param("size", "2")
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .accept(MediaType.APPLICATION_JSON)
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.items", hasSize(2)))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.lastId", notNullValue()))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 - 다음 페이지 없음(lastId로 이어받기)")
        void get_recent_partners_success_no_next() throws Exception {
            MvcResult firstResult = mockMvc.perform(
                            get("/api/users/recent-partners")
                                    .param("size", "2")
                                    .header("Authorization", "Bearer " + userAAccessToken)
                                    .accept(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isOk())
                    .andReturn();

            String body = firstResult.getResponse().getContentAsString();
            String lastId = body.replaceAll(".*\"lastId\"\\s*:\\s*(\\d+).*", "$1");

            ResultActions ra = mockMvc.perform(
                    get("/api/users/recent-partners")
                            .param("size", "2")
                            .param("lastId", lastId)
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .accept(MediaType.APPLICATION_JSON)
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    // 총 3건 → 2 + 1
                    .andExpect(jsonPath("$.data.items", hasSize(1)))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.lastId", notNullValue()))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로필 조회 API TEST")
    class GetProfileApi {

        @Test
        @DisplayName("성공 - 내 프로필 반환")
        void get_me_success() throws Exception {
            ResultActions ra = mockMvc.perform(
                    get("/api/users/me")
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .accept(MediaType.APPLICATION_JSON)
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(userAId))
                    .andExpect(jsonPath("$.data.email").value("usera@example.com"))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로필 변경 API TEST")
    class ChangeProfileApi {

        @Test
        @DisplayName("성공 - 전화번호만 변경")
        void change_phone_only_success() throws Exception {
            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01077776666");

            ResultActions ra = mockMvc.perform(
                    patch("/api/users/me")
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.phone").value("01077776666"))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 - 비밀번호만 변경")
        void change_password_only_success() throws Exception {
            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setNewPassword("NewPass123!");

            ResultActions ra = mockMvc.perform(
                    patch("/api/users/me")
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(userAId))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 - 전화번호+비밀번호 동시 변경")
        void change_both_success() throws Exception {
            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01066665555");
            req.setNewPassword("Mix1234!");

            ResultActions ra = mockMvc.perform(
                    patch("/api/users/me")
                            .header("Authorization", "Bearer " + userAAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            );

            ra.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.phone").value("01066665555"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효성 오류(하이픈 포함 전화번호)")
        void change_invalid_phone_format() throws Exception {
            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("010-1234-5678"); // 숫자만 허용

            mockMvc.perform(
                            patch("/api/users/me")
                                    .header("Authorization", "Bearer " + userAAccessToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 전화번호 중복")
        void change_phone_duplicate() throws Exception {
            // 중복 대상 유저 생성
            UserRegisterRequestDTO regB = new UserRegisterRequestDTO();
            regB.setName("UserB");
            regB.setEmail("userb@example.com");
            regB.setPassword("password123!");
            regB.setPhone("01099998888");
            authService.register(regB);

            // A가 같은 번호로 변경 시도 → 409
            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01099998888");

            mockMvc.perform(
                            patch("/api/users/me")
                                    .header("Authorization", "Bearer " + userAAccessToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 본문 없음")
        void change_missing_body() throws Exception {
            mockMvc.perform(
                            patch("/api/users/me")
                                    .header("Authorization", "Bearer " + userAAccessToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest());
        }
    }

}
