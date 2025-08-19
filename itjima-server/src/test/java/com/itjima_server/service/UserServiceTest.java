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

import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.exception.DuplicateUserFieldException;
import com.itjima_server.exception.NotInsertUserException;
import com.itjima_server.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.itjima_server.domain.User;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private UserRegisterRequestDTO req;

    @BeforeEach
    void setUp() {
        req = new UserRegisterRequestDTO();
        req.setName("testUser");
        req.setEmail("test@example.com");
        req.setPassword("password123!");
        req.setPhone("01012345678");
    }

    @Nested
    @DisplayName("회원가입 로직")
    class RegisterServiceTest {
        @Test
        @DisplayName("성공")
        void register_success() {
            // given
            when(userMapper.findByEmail(req.getEmail())).thenReturn(false);
            when(userMapper.findByPhone(req.getPhone())).thenReturn(false);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // when
            UserResponseDTO response = userService.register(req);

            // then
            assertNotNull(response);
            assertEquals(req.getEmail(), response.getEmail());
            assertEquals(req.getName(), response.getName());

            verify(passwordEncoder, times(1)).encode(req.getPassword());
            verify(userMapper, times(1)).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void register_fail_when_email_is_duplicate() {
            // given
            when(userMapper.findByEmail(req.getEmail())).thenReturn(true);

            // when & then
            assertThrows(DuplicateUserFieldException.class, () -> userService.register(req));

            verify(userMapper, never()).findByPhone(anyString());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복된 전화번호")
        void register_fail_when_phone_is_duplicate() {
            // given
            when(userMapper.findByEmail(req.getEmail())).thenReturn(false);
            when(userMapper.findByPhone(req.getPhone())).thenReturn(true);

            // when & then
            assertThrows(DuplicateUserFieldException.class, () -> userService.register(req));

            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("실패 - DB 저장 오류")
        void register_fail_when_insert_returns_zero() {
            // given
            when(userMapper.findByEmail(req.getEmail())).thenReturn(false);
            when(userMapper.findByPhone(req.getPhone())).thenReturn(false);
            when(userMapper.insert(any(User.class))).thenReturn(0);

            // when & then
            assertThrows(NotInsertUserException.class, () -> userService.register(req));
        }
    }
}
