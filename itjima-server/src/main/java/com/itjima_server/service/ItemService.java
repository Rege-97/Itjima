package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.item.Item;
import com.itjima_server.domain.item.ItemStatus;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 대여 물품 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-22
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ItemMapper itemMapper;

    /**
     * 대여물품 등록 처리
     *
     * @param req    대여물품 등록 요청 DTO
     * @param userId 로그인한 사용자 ID
     * @return 대여물품 등록 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ItemResponseDTO create(ItemCreateRequestDTO req, Long userId) {
        Item item = Item.builder()
                .userId(userId)
                .type(req.getType())
                .title(req.getTitle())
                .description(req.getDescription())
                .status(ItemStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();

        int result = itemMapper.insert(item);

        if (result < 1) {
            throw new NotInsertItemException("물품 등록 중 알 수 없는 오류가 발생했습니다.");
        }

        return ItemResponseDTO.from(item);
    }

    /**
     * 대여물품 수정 처리
     *
     * @param req    대여물품 수정 요청 DTO
     * @param userId 로그인한 사용자 id
     * @param id     수정할 물품 id
     * @return 수정 결과 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ItemResponseDTO update(ItemUpdateRequestDTO req, Long userId, Long id) {
        Item item = itemMapper.findById(id);
        if (item == null) {
            throw new NotFoundItemException("해당 물품을 찾을 수 없습니다.");
        }

        if (item.getUserId() != userId) {
            throw new NotAuthorException("로그인한 사용자의 물품이 아닙니다.");
        }

        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        int result = itemMapper.updateById(item);

        if (result < 1) {
            throw new UpdateFailedException("물품 업데이트 중 알 수 없는 오류가 발생했습니다.");
        }

        return ItemResponseDTO.from(item);
    }

    /**
     * 이미지 저장 및 DB 경로 수정 처리
     *
     * @param id  이미지를 저장할 물품 id
     * @param img 저장할 이미지 파일
     * @return 저장된 경로 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FileResult saveImage(Long id, MultipartFile img) {

        FileResult fileResult = FileUtil.save(img, "items", id, uploadDir);

        if (fileResult == null) {
            throw new IllegalArgumentException("이미지 파일이 유효하지 않습니다.");
        }

        try {
            Item item = Item.builder()
                    .id(id)
                    .fileUrl(fileResult.getFileUrl())
                    .fileType(fileResult.getFileType())
                    .build();

            int result = itemMapper.updateFileById(item);

            if (result < 1) {
                throw new UpdateFailedException("물품 이미지 정보 업데이트에 실패했습니다.");
            }

            return fileResult;

        } catch (Exception e) {
            FileUtil.delete(fileResult.getFileUrl(), uploadDir);
            throw e;
        }
    }

    /**
     * 대여 물품 리스트 조회
     *
     * @param userId 로그인한 사용자 id
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @return 대여 물품 리스트 응답 DTO
     */
    public PagedResultDTO<?> getList(Long userId, Long lastId, int size) {
        int sizePlusOne = size + 1;
        List<Item> itemList = itemMapper.findByUserId(userId, lastId, sizePlusOne);

        if (itemList == null || itemList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }
        boolean hasNext = false;
        if (itemList.size() == sizePlusOne) {
            hasNext = true;
            itemList.remove(size);
        }

        List<ItemResponseDTO> items = new ArrayList<>();

        for (Item item : itemList) {
            items.add(ItemResponseDTO.from(item));
        }

        lastId = itemList.get(itemList.size() - 1).getId();

        return PagedResultDTO.from(items, hasNext, lastId);
    }

    /**
     * 대여 물품 상세 조회
     *
     * @param id     조회할 물품 id
     * @param userId 로그인한 사용자 id
     * @return 조회된 물품 응답 DTO
     */
    public ItemResponseDTO get(Long id, Long userId) {
        Item item = itemMapper.findById(id);
        if (item == null) {
            throw new NotFoundItemException("해당 물품을 찾을 수 없습니다.");
        }

        if (item.getUserId() != userId) {
            throw new NotAuthorException("로그인한 사용자의 물품이 아닙니다.");
        }

        return ItemResponseDTO.from(item);
    }
}
