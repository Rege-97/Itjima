package com.itjima_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.item.Item;
import com.itjima_server.domain.item.ItemType;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.request.ItemUpdateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.item.NotFoundItemException;
import com.itjima_server.exception.item.NotInsertItemException;
import com.itjima_server.mapper.ItemMapper;
import com.itjima_server.util.FileResult;
import com.itjima_server.util.FileUtil;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private MultipartFile multipartFile;

    @Nested
    @DisplayName("대여 물품 등록 로직")
    class ItemCreateTest {

        private ItemCreateRequestDTO itemCreateRequestDTO;
        private Long userId;

        @BeforeEach
        void setUp() {
            itemCreateRequestDTO = new ItemCreateRequestDTO();
            itemCreateRequestDTO.setType(ItemType.OBJECT);
            itemCreateRequestDTO.setTitle("testItemTitle");
            itemCreateRequestDTO.setDescription("testItemDescription");
            userId = 1L;
        }

        @Test
        @DisplayName("성공")
        void item_create_success() {
            // given
            when(itemMapper.insert(any(Item.class))).thenReturn(1);

            // when
            ItemResponseDTO res = itemService.create(itemCreateRequestDTO, userId);

            // then
            assertNotNull(res);
            assertEquals(itemCreateRequestDTO.getTitle(), res.getTitle());
            assertEquals(itemCreateRequestDTO.getDescription(), res.getDescription());
            assertEquals(itemCreateRequestDTO.getType(), res.getType());
            assertEquals(userId, res.getUserId());

            verify(itemMapper, times(1)).insert(any(Item.class));
        }

        @Test
        @DisplayName("실패 - DB 저장 오류")
        void create_item_fail_when_insert_returns_zero() {
            // given
            when(itemMapper.insert(any(Item.class))).thenReturn(0);

            // when & then
            assertThrows(NotInsertItemException.class, () -> {
                itemService.create(itemCreateRequestDTO, userId);
            });
        }
    }

    @Nested
    @DisplayName("대여 물품 수정 로직")
    class ItemUpdateTest {

        ItemUpdateRequestDTO itemUpdateRequestDTO;
        private Long userId;
        private Long id;

        @BeforeEach
        void setUp() {
            itemUpdateRequestDTO = new ItemUpdateRequestDTO();
            itemUpdateRequestDTO.setTitle("testItemTitle");
            itemUpdateRequestDTO.setDescription("testItemDescription");
            userId = 1L;
            id = 1L;
        }

        @Test
        @DisplayName("성공")
        void item_update_success() {
            // given
            when(itemMapper.findById(id)).thenReturn(Item.builder()
                    .id(id)
                    .userId(userId)
                    .build());
            when(itemMapper.updateById(any(Item.class))).thenReturn(1);

            // when
            ItemResponseDTO res = itemService.update(itemUpdateRequestDTO, userId, id);

            // then
            assertNotNull(res);
            assertEquals(itemUpdateRequestDTO.getTitle(), res.getTitle());
            assertEquals(itemUpdateRequestDTO.getDescription(), res.getDescription());
            assertEquals(id, res.getId());
            assertEquals(userId, res.getUserId());

            verify(itemMapper, times(1)).findById(id);
            verify(itemMapper, times(1)).updateById(any(Item.class));
        }

        @Test
        @DisplayName("실패 - 물품을 찾을 수 없음")
        void update_item_fail_when_not_found_item() {
            // given
            when(itemMapper.findById(id)).thenReturn(null);

            // when & then
            assertThrows(NotFoundItemException.class,
                    () -> itemService.update(itemUpdateRequestDTO, userId, id));
        }

        @Test
        @DisplayName("실패 - 로그인한 사용자의 물품이 아님")
        void update_item_fail_when_not_author_item() {
            // given
            when(itemMapper.findById(id)).thenReturn(Item.builder()
                    .id(id)
                    .userId(2L)
                    .build());

            // when & then
            assertThrows(NotAuthorException.class,
                    () -> itemService.update(itemUpdateRequestDTO, userId, id));
        }

        @Test
        @DisplayName("실패 - DB 수정 오류")
        void update_item_fail_when_update_returns_zero() {
            // given
            when(itemMapper.findById(id)).thenReturn(Item.builder()
                    .id(id)
                    .userId(userId)
                    .build());
            when(itemMapper.updateById(any(Item.class))).thenReturn(0);

            // when & then
            assertThrows(UpdateFailedException.class,
                    () -> itemService.update(itemUpdateRequestDTO, userId, id));
        }
    }

    @Nested
    @DisplayName("이미지 저장 로직")
    class SaveImageServiceTest {

        private Long id;
        private Long userId;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(itemService, "uploadDir", "uploads");
            id = 1L;
            userId = 1L;
        }

        @Test
        @DisplayName("성공")
        void save_image_success() {
            try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
                // given
                FileResult mockFileResult = FileResult.builder()
                        .fileUrl("url")
                        .fileType("image/png")
                        .build();
                fileUtilMock.when(() -> FileUtil.save(multipartFile, "items", userId, "uploads"))
                        .thenReturn(mockFileResult);
                when(itemMapper.updateFileById(any())).thenReturn(1);

                // when
                FileResult result = itemService.saveImage(id, multipartFile);

                // then
                assertThat(result).isEqualTo(mockFileResult);
                fileUtilMock.verify(() -> FileUtil.save(multipartFile, "items", userId, "uploads"));
                verify(itemMapper).updateFileById(any());
            }
        }

        @Test
        @DisplayName("실패 - 파일 저장 실패")
        void saveImage_invalidFile_throwsException() {
            try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
                // given
                fileUtilMock.when(() -> FileUtil.save(multipartFile, "items", userId, "uploads"))
                        .thenReturn(null);

                // when & then
                assertThrows(IllegalArgumentException.class,
                        () -> itemService.saveImage(1L, multipartFile));
            }
        }

        @Test
        @DisplayName("실패 - DB 수정 실패 및 파일 저장 롤백")
        void saveImage_updateFails_rollbackAndDelete() {
            try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
                // given
                FileResult mockFileResult = FileResult.builder()
                        .fileUrl("url")
                        .fileType("image/png")
                        .build();
                fileUtilMock.when(() -> FileUtil.save(multipartFile, "items", userId, "uploads"))
                        .thenReturn(mockFileResult);
                when(itemMapper.updateFileById(any())).thenReturn(0);

                //when & then
                assertThrows(UpdateFailedException.class,
                        () -> itemService.saveImage(id, multipartFile));

                fileUtilMock.verify(() -> FileUtil.delete("url", "uploads"));
            }
        }
    }

    @Nested
    @DisplayName("대여 물품 리스트 조회 로직")
    class GetItemsServiceTest {

        private Long userId;
        private Long lastId;
        private int size;

        @BeforeEach
        void setUp() {
            userId = 1L;
            lastId = null; // 첫페이지
            size = 2;
        }

        @Test
        @DisplayName("성공 - 데이터 있음")
        void get_items_success() {
            // given
            List<Item> items = Arrays.asList(
                    Item.builder().id(1L).title("item1").userId(userId).build(),
                    Item.builder().id(2L).title("item2").userId(userId).build(),
                    Item.builder().id(3L).title("item3").userId(userId).build()
            );
            when(itemMapper.findByUserId(userId, lastId, size + 1)).thenReturn(
                    new ArrayList<>(items));

            // when
            PagedResultDTO<?> result = itemService.getList(userId, null, size);

            // then
            List<ItemResponseDTO> dtoList = (List<ItemResponseDTO>) result.getItems();
            assertThat(result.isHasNext()).isTrue();
            assertThat(result.getLastId()).isEqualTo(2L);
            assertThat(dtoList).hasSize(2);
            assertThat(dtoList.get(0).getId()).isEqualTo(1L);
            assertThat(dtoList.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공 - 데이터 없음")
        void get_items_success_not_found_item() {
            // given
            when(itemMapper.findByUserId(userId, null, 21))
                    .thenReturn(new ArrayList<>());

            // when
            PagedResultDTO<?> result = itemService.getList(userId, null, 20);

            // then
            assertThat(result.getItems()).isNull();
            assertThat(result.isHasNext()).isFalse();
            assertThat(result.getLastId()).isNull();
        }
    }

    @Nested
    @DisplayName("대여 물품 상세 조회 로직")
    class getItemServiceTest {

        private Long id;
        private Long userId;

        @BeforeEach
        void setUp() {
            id = 1L;
            userId = 1L;
        }

        @Test
        @DisplayName("성공")
        void get_items_success() {
            // given
            when(itemMapper.findById(id)).thenReturn(Item.builder()
                    .id(id)
                    .userId(userId)
                    .build());

            // when
            ItemResponseDTO res = itemService.get(id, userId);

            // then
            assertNotNull(res);
            assertEquals(id, res.getId());
            assertEquals(userId, res.getUserId());
        }

        @Test
        @DisplayName("실패 - 물품 찾을 수 없음")
        void get_item_fail_when_not_found_item() {
            // given
            when(itemMapper.findById(id)).thenReturn(null);

            // when & then
            assertThrows(NotFoundItemException.class, () -> itemService.get(id, userId));
        }

        @Test
        @DisplayName("실패 - 로그인한 사용자의 물품이 아님")
        void get_item_fail_when_not_author_item() {
            // given
            when(itemMapper.findById(id)).thenReturn(Item.builder()
                    .id(id)
                    .userId(20L)
                    .build());

            // when & then
            assertThrows(NotAuthorException.class, () -> itemService.get(id, userId));
        }
    }
}
