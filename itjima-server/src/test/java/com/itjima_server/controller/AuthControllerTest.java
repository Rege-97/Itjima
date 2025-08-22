package com.itjima_server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.dto.user.request.TokenRefreshRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.service.UserService;
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

    private UserRegisterRequestDTO userRegisterRequestDTO;
    private UserLoginRequestDTO userLoginRequestDTO;


    @Nested
    @DisplayName("회원가입 API 통합 테스트")
    class RegisterApiTest {

        @BeforeEach
        void setUp() {
            userRegisterRequestDTO = new UserRegisterRequestDTO();
            userRegisterRequestDTO.setName("testUser");
            userRegisterRequestDTO.setEmail("test@example.com");
            userRegisterRequestDTO.setPassword("password123!");
            userRegisterRequestDTO.setPhone("01012341234");
        }

        @Test
        @DisplayName("성공")
        void register_success() throws Exception {
            // given
            String json = objectMapper.writeValueAsString(userRegisterRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("회원 가입 성공"))
                    .andExpect(jsonPath("$.data.email").value(userRegisterRequestDTO.getEmail()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void register_fail_with_duplicate_email() throws Exception {
            // given
            // 첫 번째 요청은 성공해야 함
            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRegisterRequestDTO)));

            // when & then - 동일한 정보로 두 번째 요청
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterRequestDTO))
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

    @Nested
    @DisplayName("로그인 API 통합테스트")
    class LoginApiTest {

        @Autowired
        private UserService userService;

        @BeforeEach
        void setUp() {
            userRegisterRequestDTO = new UserRegisterRequestDTO();
            userRegisterRequestDTO.setName("testUser");
            userRegisterRequestDTO.setEmail("test@example.com");
            userRegisterRequestDTO.setPassword("password123!");
            userRegisterRequestDTO.setPhone("01011111111");

            userService.register(userRegisterRequestDTO);

            userLoginRequestDTO = new UserLoginRequestDTO();
            userLoginRequestDTO.setEmail("test@example.com");
            userLoginRequestDTO.setPassword("password123!");
        }

        @Test
        @DisplayName("성공")
        void login_success() throws Exception {
            // given
            String json = objectMapper.writeValueAsString(userLoginRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void login_fail_with_wrong_password() throws Exception {
            // given
            userLoginRequestDTO.setPassword("wrongPassword123!");
            String json = objectMapper.writeValueAsString(userLoginRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void login_fail_with_unregistered_email() throws Exception {
            // given
            userLoginRequestDTO.setEmail("unregisteredEmail@example.com");
            String json = objectMapper.writeValueAsString(userLoginRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."))
                    .andDo(print());
        }


        @ParameterizedTest(name = "[{index}] {2}")
        @DisplayName("실패 - 유효성 검사")
        @CsvSource(value = {
                "'', 'password123!', '이메일은 필수입니다.'",
                "'test@example.com', '', '비밀번호는 필수입니다.'",
                "'invalid-email', 'password123!', '올바른 이메일 형식이 아닙니다.'",
                "'test@example.com', 'short', '비밀번호는 8~64자여야 합니다.'",
                "'test@example.com', 'password123', '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.'"
        }, nullValues = {"''"})
        void login_fail_with_validation(String email, String password, String expectedMessage)
                throws Exception {
            // given
            UserLoginRequestDTO invalidReq = new UserLoginRequestDTO();
            invalidReq.setEmail(email);
            invalidReq.setPassword(password);

            String json = objectMapper.writeValueAsString(invalidReq);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message",
                            org.hamcrest.Matchers.containsString(expectedMessage)))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API 통합테스트")
    class RefreshAccessTokenApiTest {

        @Autowired
        private UserService userService;

        private UserLoginResponseDTO userLoginResponseDTO;
        private TokenRefreshRequestDTO tokenRefreshRequestDTO;

        @BeforeEach
        void setUp() {
            userRegisterRequestDTO = new UserRegisterRequestDTO();
            userRegisterRequestDTO.setName("testUser");
            userRegisterRequestDTO.setEmail("test@example.com");
            userRegisterRequestDTO.setPassword("password123!");
            userRegisterRequestDTO.setPhone("01011111111");

            userService.register(userRegisterRequestDTO);

            userLoginRequestDTO = new UserLoginRequestDTO();
            userLoginRequestDTO.setEmail("test@example.com");
            userLoginRequestDTO.setPassword("password123!");

            userLoginResponseDTO = userService.login(userLoginRequestDTO);
            tokenRefreshRequestDTO = new TokenRefreshRequestDTO();
            tokenRefreshRequestDTO.setRefreshToken(userLoginResponseDTO.getRefreshToken());
        }

        @Test
        @DisplayName("성공")
        void refreshAccessToken_success() throws Exception {
            // given
            String refreshTokenString = userLoginResponseDTO.getRefreshToken();
            String json = objectMapper.writeValueAsString(tokenRefreshRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.accessToken",
                            not(refreshTokenString)))
                    .andExpect(jsonPath("$.data.refreshToken").value(
                            userLoginResponseDTO.getRefreshToken()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰")
        void refreshAccessToken_fail_with_invalid_token() throws Exception {
            // given
            tokenRefreshRequestDTO.setRefreshToken("this_is_an_invalid_token");
            String json = objectMapper.writeValueAsString(tokenRefreshRequestDTO);

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 리프레쉬 토큰입니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("로그아웃 API 통합테스트")
    class LogoutApiTest {

        @Autowired
        private UserService userService;

        private UserLoginResponseDTO userLoginResponseDTO;

        @BeforeEach
        void setUp() {
            userRegisterRequestDTO = new UserRegisterRequestDTO();
            userRegisterRequestDTO.setName("testUser");
            userRegisterRequestDTO.setEmail("test@example.com");
            userRegisterRequestDTO.setPassword("password123!");
            userRegisterRequestDTO.setPhone("01011111111");

            userService.register(userRegisterRequestDTO);

            userLoginRequestDTO = new UserLoginRequestDTO();
            userLoginRequestDTO.setEmail("test@example.com");
            userLoginRequestDTO.setPassword("password123!");

            userLoginResponseDTO = userService.login(userLoginRequestDTO);
        }

        @Test
        @DisplayName("성공")
        void logout_success() throws Exception {
            // given
            String accessToken = userLoginResponseDTO.getAccessToken();

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 토큰 미포함")
        void logout_fail_with_not_request_token() throws Exception {

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 잘못된 토큰")
        void logout_fail_with_wrong_token() throws Exception {
            String accessToken = "this_is_an_invalid_token";
            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andDo(print());
        }
    }
}