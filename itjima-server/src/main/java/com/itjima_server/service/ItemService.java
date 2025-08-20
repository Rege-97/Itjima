package com.itjima_server.service;

import com.itjima_server.domain.Item;
import com.itjima_server.dto.request.ItemCreateRequestDTO;
import com.itjima_server.dto.response.ItemResponseDTO;
import com.itjima_server.exception.NotInsertItemException;
import com.itjima_server.mapper.ItemMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private ItemMapper itemMapper;

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
            throw new NotInsertItemException("물품 등록이 정상적으로 되지 않았습니다.");
        }

        return ItemResponseDTO.from(item);
    }
}
