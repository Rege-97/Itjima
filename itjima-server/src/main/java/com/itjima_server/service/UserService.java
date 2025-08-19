package com.itjima_server.service;

import com.itjima_server.domain.Provider;
import com.itjima_server.domain.User;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.exception.DuplicateUserFieldException;
import com.itjima_server.exception.NotInsertUserException;
import com.itjima_server.mapper.UserMapper;
import com.itjima_server.security.CustomUserDetails;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 담당하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-19
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 신규 사용자 회원가입 처리
     *
     * @param req 회원가입 요청 DTO
     * @return 등록된 사용자 응답 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponseDTO register(UserRegisterRequestDTO req) {
        if (userMapper.existsByEmail(req.getEmail())) {
            throw new DuplicateUserFieldException("이미 사용 중인 이메일 입니다.");
        }

        if (userMapper.existsByPhone(req.getPhone())) {
            throw new DuplicateUserFieldException("이미 사용 중인 전화번호 입니다.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .provider(Provider.LOCAL)
                .createdAt(LocalDateTime.now())
                .build();

        int result = userMapper.insert(user);
        if (result < 1) {
            throw new NotInsertUserException("회원가입이 정상적으로 되지 않았습니다.");
        }
        return UserResponseDTO.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다." + email);
        }
        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(),
                Collections.emptyList());
    }
}
