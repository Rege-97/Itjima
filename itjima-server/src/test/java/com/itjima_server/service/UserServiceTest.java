
package com.itjima_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.user.Provider;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.user.request.UserChangeProfileRequestDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.user.DuplicateUserFieldException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.mapper.AgreementMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private AgreementMapper agreementMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Long userId;
    private int size;
    User baseUser;

    private RecentPartnerResponseDTO row1;
    private RecentPartnerResponseDTO row2;
    private RecentPartnerResponseDTO row3;

    @BeforeEach
    void setUp() {
        userId = 10L;
        baseUser = User.builder()
                .id(userId)
                .name("UserA")
                .email("usera@example.com")
                .password("ENC_OLD")
                .phone("01011112222")
                .provider(Provider.LOCAL)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Nested
    @DisplayName("최근 대여 사용자 목록 조회")
    class GetRecentPartnerListTest {

        @BeforeEach
        void setUp() {
            userId = 10L;
            size = 2;

            row1 = new RecentPartnerResponseDTO();
            row1.setId(201L);
            row1.setName("test");
            row1.setEmail("test@example.com");
            row1.setPhone("010-3333-3333");
            row1.setLastAgreementId(2002L);
            row1.setLastContactAt(LocalDateTime.now().minusHours(3));

            row2 = new RecentPartnerResponseDTO();
            row2.setId(202L);
            row2.setName("test");
            row2.setEmail("test@example.com");
            row2.setPhone("010-4444-4444");
            row2.setLastAgreementId(2001L);
            row2.setLastContactAt(LocalDateTime.now().minusHours(4));

            row3 = new RecentPartnerResponseDTO();
            row3.setId(203L);
            row3.setName("test");
            row3.setEmail("test@example.com");
            row3.setPhone("010-5555-5555");
            row3.setLastAgreementId(2000L);
            row3.setLastContactAt(LocalDateTime.now().minusHours(5));
        }

        @Test
        @DisplayName("성공 - 다음 페이지 없음")
        void recentPartners_success_basic() {
            // given
            List<RecentPartnerResponseDTO> rows = new ArrayList<>(Arrays.asList(row1, row2));

            when(agreementMapper.findRecentPartnersByUserId(userId, null, size + 1))
                    .thenReturn(rows);

            // when
            PagedResultDTO<?> res = userService.getRecentPartnerList(userId, null, size);

            // then
            assertNotNull(res);
            assertEquals(2, res.getItems().size());
            assertFalse(res.isHasNext());
        }

        @Test
        @DisplayName("성공 - 다음 페이지 존재")
        void recentPartners_success_hasNext() {
            // given
            List<RecentPartnerResponseDTO> rows = new ArrayList<>(Arrays.asList(row1, row2, row3));
            when(agreementMapper.findRecentPartnersByUserId(userId, null, size + 1))
                    .thenReturn(rows);
            // when
            PagedResultDTO<?> res = userService.getRecentPartnerList(userId, null, size);

            // then
            assertNotNull(res);
            assertEquals(size, res.getItems().size());
            assertTrue(res.isHasNext());
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {

        @Test
        @DisplayName("성공 - 존재하는 사용자")
        void get_profile_success() {
            when(userMapper.findById(userId)).thenReturn(baseUser);

            UserResponseDTO res = userService.getProfile(userId);

            assertEquals(userId.longValue(), res.getId());
            assertEquals(baseUser.getEmail(), res.getEmail());
            assertEquals(baseUser.getPhone(), res.getPhone());
            verify(userMapper, times(1)).findById(userId);
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void get_profile_not_found() {
            when(userMapper.findById(999L)).thenReturn(null);
            assertThrows(NotFoundUserException.class,
                    () -> userService.getProfile(baseUser.getId()));
            verify(userMapper, times(1)).findById(baseUser.getId());
        }
    }

    @Nested
    @DisplayName("프로필 변경")
    class ChangeProfile {

        @Test
        @DisplayName("성공 - 전화번호만 변경")
        void change_profile_phone_only_success() {
            when(userMapper.findById(userId)).thenReturn(baseUser);
            when(userMapper.existsByPhone("01077776666")).thenReturn(false);
            when(userMapper.updatePhoneById(userId, "01077776666")).thenReturn(1);

            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01077776666");

            UserResponseDTO res = userService.changeProfile(userId, req);

            assertEquals("01077776666", res.getPhone());
            verify(userMapper, times(1)).updatePhoneById(userId, "01077776666");
            verify(userMapper, times(0)).updatePasswordById(userId, "ANY");
        }

        @Test
        @DisplayName("성공 - 비밀번호만 변경")
        void change_profile_password_only_success() {
            when(userMapper.findById(userId)).thenReturn(baseUser);
            when(passwordEncoder.encode("NewPass123!")).thenReturn("ENC_NewPass123!");
            when(userMapper.updatePasswordById(userId, "ENC_NewPass123!")).thenReturn(1);

            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPassword("NewPass123!");

            UserResponseDTO res = userService.changeProfile(userId, req);

            assertEquals("01011112222", res.getPhone()); // 전화는 그대로
            verify(userMapper, times(1)).updatePasswordById(userId, "ENC_NewPass123!");
            verify(userMapper, times(0)).updatePhoneById(userId, "ANY");
        }

        @Test
        @DisplayName("성공 - 전화번호+비밀번호 동시 변경")
        void change_profile_both_success() {
            when(userMapper.findById(userId)).thenReturn(baseUser);
            when(userMapper.existsByPhone("01066665555")).thenReturn(false);
            when(userMapper.updatePhoneById(userId, "01066665555")).thenReturn(1);
            when(passwordEncoder.encode("Mix1234!")).thenReturn("ENC_Mix1234!");
            when(userMapper.updatePasswordById(userId, "ENC_Mix1234!")).thenReturn(1);

            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01066665555");
            req.setPassword("Mix1234!");

            UserResponseDTO res = userService.changeProfile(userId, req);

            assertEquals("01066665555", res.getPhone());
            verify(userMapper, times(1)).updatePhoneById(userId, "01066665555");
            verify(userMapper, times(1)).updatePasswordById(userId, "ENC_Mix1234!");
        }

        @Test
        @DisplayName("실패 - 요청 본문 null 또는 변경 필드 없음")
        void change_profile_bad_request() {
            when(userMapper.findById(userId)).thenReturn(baseUser);

            assertThrows(IllegalArgumentException.class,
                    () -> userService.changeProfile(userId, null));

            UserChangeProfileRequestDTO empty = new UserChangeProfileRequestDTO();
            assertThrows(IllegalArgumentException.class,
                    () -> userService.changeProfile(userId, empty));
        }

        @Test
        @DisplayName("실패 - 전화번호 중복")
        void change_profile_phone_duplicate() {
            when(userMapper.findById(userId)).thenReturn(baseUser);
            when(userMapper.existsByPhone("01099998888")).thenReturn(true);

            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01099998888");

            assertThrows(DuplicateUserFieldException.class,
                    () -> userService.changeProfile(userId, req));

            verify(userMapper, times(1)).findById(userId);
            verify(userMapper, times(1)).existsByPhone("01099998888");
        }


        @Test
        @DisplayName("실패 - 전화번호 업데이트 결과 0건")
        void change_profile_phone_update_failed() {
            when(userMapper.findById(userId)).thenReturn(baseUser);
            when(userMapper.existsByPhone("01012121212")).thenReturn(false);
            when(userMapper.updatePhoneById(userId, "01012121212")).thenReturn(0);

            UserChangeProfileRequestDTO req = new UserChangeProfileRequestDTO();
            req.setPhone("01012121212");

            assertThrows(UpdateFailedException.class, () -> userService.changeProfile(userId, req));
        }
    }
}
