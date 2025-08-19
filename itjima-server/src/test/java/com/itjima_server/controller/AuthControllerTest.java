package com.itjima_server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterRequestDTO req;

    @BeforeEach
    void setUp() {
        req = new UserRegisterRequestDTO();
        req.setName("testUser");
        req.setEmail("test@example.com");
        req.setPassword("password123!");
        req.setPhone("01012341234");
    }

    @Nested
    @DisplayName("회원가입 API 통합 테스트")
    class RegisterApiTest {

        @Test
        @DisplayName("성공")
        void register_success() throws Exception {
            // given
            String json = objectMapper.writeValueAsString(req);

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("회원 가입 성공"))
                    .andExpect(jsonPath("$.data.email").value(req.getEmail()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void register_fail_with_duplicate_email() throws Exception {
            // given
            // 첫 번째 요청은 성공해야 함
            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));

            // when & then - 동일한 정보로 두 번째 요청
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isConflict()) // 409 상태 코드 검증
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일 입니다."))
                    .andDo(print());
        }

        @ParameterizedTest(name = "[{index}] {4}")
        @DisplayName("실패 - 유효성 검사")
        @CsvSource(value = {
                "'', 'test@example.com', 'password123!', '01012345678', '이름은 필수입니다.'",
                "'testUser', '', 'password123!', '01012345678', '이메일은 필수입니다.'",
                "'testUser', 'test@example.com', '', '01012345678', '비밀번호는 필수입니다.'",
                "'testUser', 'test@example.com', 'password123!', '', '전화번호는 필수입니다.'",
                "'t', 'test@example.com', 'password123!', '01012345678', '이름은 2~32자여야 합니다.'",
                "'testUser', 'invalid-email', 'password123!', '01012345678', '올바른 이메일 형식이 아닙니다.'",
                "'testUser', 'test@example.com', 'short', '01012345678', '비밀번호는 8~64자여야 합니다.'",
                "'testUser', 'test@example.com', 'password123', '01012345678', '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.'",
                "'testUser', 'test@example.com', 'password123!', '12345', '전화번호는 숫자 10~11자리여야 합니다.'"
        })
        void register_fail_with_validation(String name, String email, String password, String phone,
                String expectedMessage) throws Exception {
            // given
            UserRegisterRequestDTO invalidReq = new UserRegisterRequestDTO();
            // CsvSource에서 빈 문자열('')을 null로 변환하는 것을 방지하기 위해 명시적으로 처리
            if (name != null) {
                invalidReq.setName(name);
            }
            if (email != null) {
                invalidReq.setEmail(email);
            }
            if (password != null) {
                invalidReq.setPassword(password);
            }
            if (phone != null) {
                invalidReq.setPhone(phone);
            }

            String json = objectMapper.writeValueAsString(invalidReq);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString(expectedMessage)))
                    .andDo(print());
        }
    }
}