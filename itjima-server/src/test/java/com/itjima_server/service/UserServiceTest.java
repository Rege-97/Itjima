package com.itjima_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itjima_server.domain.RefreshToken;
import com.itjima_server.dto.user.request.TokenRefreshRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.TokenResponseDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.exception.user.DuplicateUserFieldException;
import com.itjima_server.exception.user.InvalidRefreshTokenException;
import com.itjima_server.exception.user.LoginFailedException;
import com.itjima_server.exception.user.NotInsertUserException;
import com.itjima_server.mapper.RefreshTokenMapper;
import com.itjima_server.mapper.UserMapper;
import com.itjima_server.security.JwtTokenProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.itjima_server.domain.User;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private UserRegisterRequestDTO userRegisterRequestDTO;
    private UserLoginRequestDTO userLoginRequestDTO;
    private TokenRefreshRequestDTO tokenRefreshRequestDTO;
    private User fakeUser;
    private RefreshToken fakeRefreshToken;

    @Nested
    @DisplayName("회원가입 로직")
    class RegisterServiceTest {

        @BeforeEach
        void setUp() {
            userRegisterRequestDTO = new UserRegisterRequestDTO();
            userRegisterRequestDTO.setName("testUser");
            userRegisterRequestDTO.setEmail("test@example.com");
            userRegisterRequestDTO.setPassword("password123!");
            userRegisterRequestDTO.setPhone("01012345678");
        }

        @Test
        @DisplayName("성공")
        void register_success() {
            // given
            when(userMapper.existsByEmail(userRegisterRequestDTO.getEmail())).thenReturn(false);
            when(userMapper.existsByPhone(userRegisterRequestDTO.getPhone())).thenReturn(false);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // when
            UserResponseDTO response = userService.register(userRegisterRequestDTO);

            // then
            assertNotNull(response);
            assertEquals(userRegisterRequestDTO.getEmail(), response.getEmail());
            assertEquals(userRegisterRequestDTO.getName(), response.getName());

            verify(passwordEncoder, times(1)).encode(userRegisterRequestDTO.getPassword());
            verify(userMapper, times(1)).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void register_fail_when_email_is_duplicate() {
            // given
            when(userMapper.existsByEmail(userRegisterRequestDTO.getEmail())).thenReturn(true);

            // when & then
            assertThrows(DuplicateUserFieldException.class, () -> userService.register(
                    userRegisterRequestDTO));

            verify(userMapper, never()).existsByPhone(anyString());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복된 전화번호")
        void register_fail_when_phone_is_duplicate() {
            // given
            when(userMapper.existsByEmail(userRegisterRequestDTO.getEmail())).thenReturn(false);
            when(userMapper.existsByPhone(userRegisterRequestDTO.getPhone())).thenReturn(true);

            // when & then
            assertThrows(DuplicateUserFieldException.class, () -> userService.register(
                    userRegisterRequestDTO));

            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - DB 저장 오류")
        void register_fail_when_insert_returns_zero() {
            // given
            when(userMapper.existsByEmail(userRegisterRequestDTO.getEmail())).thenReturn(false);
            when(userMapper.existsByPhone(userRegisterRequestDTO.getPhone())).thenReturn(false);
            when(userMapper.insert(any(User.class))).thenReturn(0);

            // when & then
            assertThrows(NotInsertUserException.class, () -> userService.register(
                    userRegisterRequestDTO));
        }
    }

    @Nested
    @DisplayName("로그인 로직")
    class LoginServiceTest {

        @BeforeEach
        void setUp() {
            userLoginRequestDTO = new UserLoginRequestDTO();
            userLoginRequestDTO.setEmail("test@example.com");
            userLoginRequestDTO.setPassword("password123!");

            fakeUser = User.builder()
                    .id(1L) // ID 추가
                    .email(userLoginRequestDTO.getEmail())
                    .password(passwordEncoder.encode(userLoginRequestDTO.getPassword()))
                    .build();
        }

        @Test
        @DisplayName("성공 - 기존 Refresh Token 업데이트")
        void login_success_and_update_token() {
            // given
            when(userMapper.findByEmail(userLoginRequestDTO.getEmail())).thenReturn(fakeUser);
            when(passwordEncoder.matches(userLoginRequestDTO.getPassword(),
                    fakeUser.getPassword())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(fakeUser)).thenReturn("access_token");
            when(jwtTokenProvider.generateRefreshToken(fakeUser)).thenReturn("refresh_token");

            when(refreshTokenMapper.existsByUserId(fakeUser.getId())).thenReturn(true);

            // when
            UserLoginResponseDTO res = userService.login(userLoginRequestDTO);

            // then
            assertNotNull(res);
            assertEquals("access_token", res.getAccessToken());
            assertEquals("refresh_token", res.getRefreshToken());

            verify(refreshTokenMapper, times(1)).update(any(RefreshToken.class));
            verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
        }

        @Test
        @DisplayName("성공 - 신규 Refresh Token 생성")
        void login_success_and_insert_token() {
            // given
            when(userMapper.findByEmail(userLoginRequestDTO.getEmail())).thenReturn(fakeUser);
            when(passwordEncoder.matches(userLoginRequestDTO.getPassword(),
                    fakeUser.getPassword())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(fakeUser)).thenReturn("access_token");
            when(jwtTokenProvider.generateRefreshToken(fakeUser)).thenReturn("refresh_token");

            when(refreshTokenMapper.existsByUserId(fakeUser.getId())).thenReturn(false);

            // when
            UserLoginResponseDTO res = userService.login(userLoginRequestDTO);

            // then
            assertNotNull(res);
            assertEquals("access_token", res.getAccessToken());
            assertEquals("refresh_token", res.getRefreshToken());

            verify(refreshTokenMapper, times(1)).insert(any(RefreshToken.class));
            verify(refreshTokenMapper, never()).update(any(RefreshToken.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void login_fail_when_not_exist_email() {
            // given
            when(userMapper.findByEmail(userLoginRequestDTO.getEmail())).thenReturn(null);

            // when & then
            assertThrows(LoginFailedException.class, () -> userService.login(userLoginRequestDTO));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void login_fail_when_not_exist_password() {
            // given
            User mismatchUser = User.builder().password("duplicate").build();
            when(userMapper.findByEmail(userLoginRequestDTO.getEmail())).thenReturn(mismatchUser);
            when(passwordEncoder.matches(userLoginRequestDTO.getPassword(),
                    mismatchUser.getPassword())).thenReturn(false);

            // when & then
            assertThrows(LoginFailedException.class, () -> userService.login(userLoginRequestDTO));
        }
    }

    @Nested
    @DisplayName("Access Token 리프레쉬 로직")
    class RefreshAccessTokenServiceTest {

        @BeforeEach
        void setUp() {
            tokenRefreshRequestDTO = new TokenRefreshRequestDTO();
            tokenRefreshRequestDTO.setRefreshToken("refresh_token");
            fakeRefreshToken = RefreshToken.builder()
                    .id(1L)
                    .userId(1L)
                    .token("refresh_token")
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .build();

            fakeUser = User.builder()
                    .id(1L) // ID 추가
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password123!"))
                    .build();
        }

        @Test
        @DisplayName("성공")
        void refresh_access_token_success() {
            // given
            when(refreshTokenMapper.findByToken(
                    tokenRefreshRequestDTO.getRefreshToken())).thenReturn(fakeRefreshToken);
            when(userMapper.findById(fakeRefreshToken.getId())).thenReturn(fakeUser);
            when(jwtTokenProvider.generateAccessToken(fakeUser)).thenReturn("new_access_token");

            // when
            TokenResponseDTO res = userService.refreshAccessToken(tokenRefreshRequestDTO);

            // then
            assertNotNull(res);
            assertEquals("new_access_token", res.getAccessToken());
            assertEquals(tokenRefreshRequestDTO.getRefreshToken(), res.getRefreshToken());

            verify(refreshTokenMapper, times(1)).findByToken(
                    tokenRefreshRequestDTO.getRefreshToken());
            verify(userMapper, times(1)).findById(fakeRefreshToken.getId());
            verify(jwtTokenProvider, times(1)).generateAccessToken(fakeUser);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 리프레쉬 토큰")
        void refresh_access_token_fail_when_not_exist_refresh_token() {
            // given
            when(refreshTokenMapper.findByToken(
                    tokenRefreshRequestDTO.getRefreshToken())).thenReturn(null);

            // when & then
            assertThrows(InvalidRefreshTokenException.class,
                    () -> userService.refreshAccessToken(tokenRefreshRequestDTO));
        }

        @Test
        @DisplayName("실패 - 만료된 리프레쉬 토큰")
        void refresh_access_token_fail_when_over_expired_refresh_token() {
            // given
            when(refreshTokenMapper.findByToken(
                    tokenRefreshRequestDTO.getRefreshToken())).thenReturn(
                    RefreshToken.builder()
                            .userId(1L)
                            .expiryDate(LocalDateTime.now().minusDays(1))
                            .build());

            // when & then
            assertThrows(InvalidRefreshTokenException.class,
                    () -> userService.refreshAccessToken(tokenRefreshRequestDTO));
            verify(refreshTokenMapper, times(1)).deleteByUserId(fakeUser.getId());
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void refresh_access_token_fail_when_not_found_user() {
            // given
            when(refreshTokenMapper.findByToken(
                    tokenRefreshRequestDTO.getRefreshToken())).thenReturn(fakeRefreshToken);
            when(userMapper.findById(fakeRefreshToken.getId())).thenReturn(null);

            // when & then
            assertThrows(UsernameNotFoundException.class,
                    () -> userService.refreshAccessToken(tokenRefreshRequestDTO));
        }
    }

    @Nested
    @DisplayName("로그아웃 로직")
    class LogoutServiceTest {

        private long id;

        @BeforeEach
        void setUp() {
            id = 1L;
            fakeUser = User.builder()
                    .id(1L) // ID 추가
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password123!"))
                    .build();
        }

        @Test
        @DisplayName("성공")
        void logout_success() {
            // given
            when(userMapper.findById(id)).thenReturn(fakeUser);

            // when
            userService.logout(id);

            // then
            verify(userMapper, times(1)).findById(id);
            verify(refreshTokenMapper, times(1)).deleteByUserId(id);
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void logout_fail_when_not_exist_user() {
            // given
            when(userMapper.findById(id)).thenReturn(null);

            // when & then
            assertThrows(UsernameNotFoundException.class, () -> userService.logout(id));
        }
    }
}
