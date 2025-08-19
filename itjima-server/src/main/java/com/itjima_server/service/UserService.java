package com.itjima_server.service;

import com.itjima_server.domain.Provider;
import com.itjima_server.domain.RefreshToken;
import com.itjima_server.domain.User;
import com.itjima_server.dto.request.UserLoginRequestDTO;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.UserLoginResponseDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.exception.DuplicateUserFieldException;
import com.itjima_server.exception.LoginFailedException;
import com.itjima_server.exception.NotInsertUserException;
import com.itjima_server.mapper.RefreshTokenMapper;
import com.itjima_server.mapper.UserMapper;
import com.itjima_server.security.JwtTokenProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponseDTO login(UserLoginRequestDTO req) {
        User user = userMapper.findByEmail(req.getEmail());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new LoginFailedException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);

        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(
                jwtTokenProvider.getRefreshExpirationMs() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .userId(user.getId())
                .expiryDate(expiryDate)
                .build();

        if (refreshTokenMapper.existsByUserId(user.getId())) {
            refreshTokenMapper.update(refreshToken);
        } else {
            refreshTokenMapper.insert(refreshToken);
        }

        return new UserLoginResponseDTO(accessToken, refreshTokenString, "Bearer",
                jwtTokenProvider.getAccessExpirationMs());
    }
}
