package com.itjima_server.service;

import com.itjima_server.domain.Item;
import com.itjima_server.dto.request.ItemCreateRequestDTO;
import com.itjima_server.dto.request.ItemUpdateRequestDTO;
import com.itjima_server.dto.response.ItemResponseDTO;
import com.itjima_server.exception.common.NotAuthorException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.item.NotFoundItemException;
import com.itjima_server.exception.item.NotInsertItemException;
import com.itjima_server.mapper.ItemMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;

    @Transactional(rollbackFor = Exception.class)
    public ItemResponseDTO create(ItemCreateRequestDTO req, Long userId) {
        Item item = Item.builder()
                .userId(userId)
                .type(req.getType())
                .title(req.getTitle())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        int result = itemMapper.insert(item);

        if (result < 1) {
            throw new NotInsertItemException("물품 등록 중 알 수 없는 오류가 발생했습니다.");
        }

        return ItemResponseDTO.from(item);
    }

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
        int result = itemMapper.update(item);

        if (result < 1) {
            throw new UpdateFailedException("물품 업데이트 중 알 수 없는 오류가 발생했습니다.");
        }

        return ItemResponseDTO.from(item);
    }
}
