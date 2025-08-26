
package com.itjima_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import com.itjima_server.mapper.AgreementMapper;
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
class UserServiceTest {

    @Mock
    private AgreementMapper agreementMapper;

    @InjectMocks
    private UserService userService;

    private Long userId;
    private int size;

    private RecentPartnerResponseDTO row1;
    private RecentPartnerResponseDTO row2;
    private RecentPartnerResponseDTO row3;

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
}
