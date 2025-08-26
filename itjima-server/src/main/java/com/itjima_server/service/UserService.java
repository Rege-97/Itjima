package com.itjima_server.service;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.user.request.UserChangeProfileRequestDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.user.DuplicateUserFieldException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.mapper.AgreementMapper;
import com.itjima_server.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 수행하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final AgreementMapper agreementMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 대여 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param id     로그인한 사용자 ID
     * @param lastId 마지막으로 조회한 대여 ID
     * @param size   요청한 페이지 크기
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Transactional(readOnly = true)
    public PagedResultDTO<?> getRecentPartnerList(Long id, Long lastId, int size) {
        int sizePlusOne = size + 1;
        List<RecentPartnerResponseDTO> userList = agreementMapper.findRecentPartnersByUserId(id,
                lastId, sizePlusOne);

        if (userList == null || userList.isEmpty()) {
            return PagedResultDTO.from(null, false, null);
        }
        boolean hasNext = false;
        if (userList.size() == sizePlusOne) {
            hasNext = true;
            userList.remove(size);
        }

        lastId = userList.get(userList.size() - 1).getLastAgreementId();

        return PagedResultDTO.from(userList, hasNext, lastId);
    }

    /**
     * 로그인한 사용자의 프로필 조회
     *
     * @param id 로그인한 유저 ID
     * @return 조회된 유저 프로필 DTO
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getProfile(Long id) {
        User user = findById(id);
        return UserResponseDTO.from(user);
    }

    /**
     * 로그인한 사용자의 전화번호 또는 비밀번호 변경
     *
     * @param id  로그인한 유저 ID
     * @param req 변경할 데이터
     * @return 변경된 유저 프로필 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponseDTO changeProfile(Long id, UserChangeProfileRequestDTO req) {
        User user = findById(id);

        if (req == null || (req.getPassword() == null && req.getPhone() == null)) {
            throw new IllegalArgumentException("수정할 데이터가 없습니다.");
        }

        if (req.getPhone() != null) {
            if (userMapper.existsByPhone(req.getPhone())) {
                throw new DuplicateUserFieldException("이미 사용 중인 전화번호 입니다.");
            }
            user.setPhone(req.getPhone());
            checkUpdateResult(userMapper.updatePhoneById(id, req.getPhone()), "전화번호 변경에 실패했습니다.");
        }

        if (req.getPassword() != null) {
            String newPassword = passwordEncoder.encode(req.getPassword());
            user.setPassword(newPassword);
            checkUpdateResult(userMapper.updatePasswordById(id, newPassword), "비밀번호 변경에 실패했습니다.");
        }

        return UserResponseDTO.from(user);
    }

    // ==========================
    // 내부 유틸리티
    // ==========================

    /**
     * ID로 사용자 조회 (없으면 예외 발생)
     *
     * @param id 사용자 ID
     * @return 조회된 User
     * @throws NotFoundUserException 사용자가 존재하지 않을 경우
     */
    private User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new NotFoundUserException("해당 사용자의 정보를 찾을 수 없습니다.");
        }
        return user;
    }

    /**
     * UPDATE 실행 결과 검증 유틸리티
     *
     * @param result       실행된 row 수
     * @param errorMessage 실패 시 예외 메시지
     * @throws UpdateFailedException update 실패 시
     */
    private void checkUpdateResult(int result, String errorMessage) {
        if (result < 1) {
            throw new UpdateFailedException(errorMessage);
        }
    }
}
