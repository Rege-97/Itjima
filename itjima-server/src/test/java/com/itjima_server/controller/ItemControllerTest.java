package com.itjima_server.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.domain.ItemType;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.request.ItemUpdateRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.service.ItemService;
import com.itjima_server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private String accessToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        UserRegisterRequestDTO userRegisterRequestDTO = new UserRegisterRequestDTO();
        userRegisterRequestDTO.setName("testUser");
        userRegisterRequestDTO.setEmail("itemTest@example.com");
        userRegisterRequestDTO.setPassword("password123!");
        userRegisterRequestDTO.setPhone("01012345678");
        userService.register(userRegisterRequestDTO);

        UserLoginRequestDTO userLoginRequestDTO = new UserLoginRequestDTO();
        userLoginRequestDTO.setEmail("itemTest@example.com");
        userLoginRequestDTO.setPassword("password123!");
        UserLoginResponseDTO loginResponse = userService.login(userLoginRequestDTO);
        accessToken = loginResponse.getAccessToken();
        userId = loginResponse.getId();
    }

    @Nested
    @DisplayName("품목 등록 API")
    class CreateItemApiTest {

        @Test
        @DisplayName("성공")
        void create_item_success() throws Exception {
            // given
            ItemCreateRequestDTO req = new ItemCreateRequestDTO();
            req.setType(ItemType.OBJECT);
            req.setTitle("테스트 물품");
            req.setDescription("테스트 설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/items")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("물품 등록 성공"))
                    .andExpect(jsonPath("$.data.title").value("테스트 물품"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 제목 없음 (유효성 검사)")
        void create_item_fail_with_blank_title() throws Exception {
            // given
            ItemCreateRequestDTO req = new ItemCreateRequestDTO();
            req.setType(ItemType.OBJECT);
            req.setTitle(""); // 제목을 비움
            req.setDescription("설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/items")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 토큰 없음 (인증 실패)")
        void create_item_fail_with_no_token() throws Exception {
            // given
            ItemCreateRequestDTO req = new ItemCreateRequestDTO();
            req.setType(ItemType.OBJECT);
            req.setTitle("테스트 물품");
            req.setDescription("테스트 설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/items")
                    // .header("Authorization", "Bearer " + accessToken) // 토큰 제거
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isUnauthorized()) // 401 Unauthorized
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("품목 수정 API")
    class UpdateItemApiTest {

        private Long itemId;

        @BeforeEach
        void setup() {
            ItemCreateRequestDTO createDto = new ItemCreateRequestDTO();
            createDto.setType(ItemType.OBJECT);
            createDto.setTitle("수정 전 제목");
            createDto.setDescription("수정 전 설명");
            ItemResponseDTO item = itemService.create(createDto, userId);
            itemId = item.getId();
        }

        @Test
        @DisplayName("성공")
        void update_item_success() throws Exception {
            // given
            ItemUpdateRequestDTO req = new ItemUpdateRequestDTO();
            req.setTitle("수정 후 제목");
            req.setDescription("수정 후 설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(put("/api/items/{id}", itemId)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.title").value("수정 후 제목"))
                    .andExpect(jsonPath("$.data.description").value("수정 후 설명"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 품목")
        void update_item_fail_with_not_found() throws Exception {
            // given
            ItemUpdateRequestDTO req = new ItemUpdateRequestDTO();
            req.setTitle("수정 시도");
            req.setDescription("설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(put("/api/items/{id}", 9999L) // 존재하지 않는 ID
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isNotFound()) // 404 Not Found
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 품목 (권한 없음)")
        void update_item_fail_with_not_author() throws Exception {
            // given
            UserRegisterRequestDTO otherUserDto = new UserRegisterRequestDTO();
            otherUserDto.setName("otherUser");
            otherUserDto.setEmail("other@example.com");
            otherUserDto.setPassword("password123!");
            otherUserDto.setPhone("01011112222");
            userService.register(otherUserDto);
            UserLoginResponseDTO otherLogin = userService.login(new UserLoginRequestDTO("other@example.com", "password123!"));
            String otherAccessToken = otherLogin.getAccessToken();

            ItemUpdateRequestDTO req = new ItemUpdateRequestDTO();
            req.setTitle("수정 시도");
            req.setDescription("설명");
            String json = objectMapper.writeValueAsString(req);

            // when
            ResultActions resultActions = mockMvc.perform(put("/api/items/{id}", itemId)
                    .header("Authorization", "Bearer " + otherAccessToken) // 다른 사용자 토큰 사용
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // then
            resultActions
                    .andExpect(status().isForbidden()) // 403 Forbidden
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("품목 이미지 등록 API")
    class SaveImageApiTest {

        private Long itemId;

        @BeforeEach
        void setup() {
            ItemCreateRequestDTO createDto = new ItemCreateRequestDTO();
            createDto.setType(ItemType.OBJECT);
            createDto.setTitle("이미지 테스트용 물품");
            createDto.setDescription("설명");
            ItemResponseDTO item = itemService.create(createDto, userId);
            itemId = item.getId();
        }

        @Test
        @DisplayName("성공")
        void save_image_success() throws Exception {
            // given
            MockMultipartFile mockImageFile = new MockMultipartFile(
                    "img",
                    "test.jpg",
                    "image/jpeg",
                    "image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/items/{id}/file", itemId)
                            .file(mockImageFile)
                            .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이미지 저장 성공"))
                    .andExpect(jsonPath("$.data.fileUrl").exists())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("품목 목록/상세 조회 API")
    class GetItemApiTest {

        private Long itemId1;

        @BeforeEach
        void setUp() {
            for (int i = 1; i <= 5; i++) {
                ItemCreateRequestDTO req = new ItemCreateRequestDTO();
                req.setType(ItemType.MONEY);
                req.setTitle("물품 " + i);
                req.setDescription("설명 " + i);
                ItemResponseDTO item = itemService.create(req, userId);
                if (i == 1) itemId1 = item.getId();
            }
        }

        @Test
        @DisplayName("목록 조회 성공 - 첫 페이지")
        void get_item_list_success_first_page() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(get("/api/items")
                    .header("Authorization", "Bearer " + accessToken)
                    .param("size", "3"));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items.length()").value(3))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andDo(print());
        }

        @Test
        @DisplayName("상세 조회 성공")
        void get_item_detail_success() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(get("/api/items/{id}", itemId1)
                    .header("Authorization", "Bearer " + accessToken));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(itemId1))
                    .andExpect(jsonPath("$.data.title").value("물품 1"))
                    .andDo(print());
        }

        @Test
        @DisplayName("상세 조회 실패 - 다른 사용자 품목")
        void get_item_detail_fail_with_not_author() throws Exception {
            // given
            UserRegisterRequestDTO otherUserDto = new UserRegisterRequestDTO();
            otherUserDto.setName("otherUser");
            otherUserDto.setEmail("other@example.com");
            otherUserDto.setPassword("password123!");
            otherUserDto.setPhone("01011112222");
            userService.register(otherUserDto);
            UserLoginResponseDTO otherLogin = userService.login(new UserLoginRequestDTO("other@example.com", "password123!"));
            String otherAccessToken = otherLogin.getAccessToken();

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/items/{id}", itemId1)
                    .header("Authorization", "Bearer " + otherAccessToken));

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }
}