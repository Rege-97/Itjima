package com.itjima_server.service;

import com.itjima_server.domain.user.Provider;
import com.itjima_server.domain.user.RefreshToken;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.user.request.TokenRefreshRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.TokenResponseDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.user.DuplicateUserFieldException;
import com.itjima_server.exception.user.InvalidRefreshTokenException;
import com.itjima_server.exception.user.LoginFailedException;
import com.itjima_server.exception.user.NotFoundUserException;
import com.itjima_server.exception.user.NotInsertUserException;
import com.itjima_server.mapper.RefreshTokenMapper;
import com.itjima_server.mapper.UserMapper;
import com.itjima_server.security.JwtTokenProvider;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 담당하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-20
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int EMAIL_CODE_LENGTH = 6;
    private static final int EMAIL_CODE_TTL_MINUTES = 3;
    private static final String EMAIL_CODE_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * 신규 사용자 회원가입 처리
     * <p>
     * 이메일과 휴대전화 검증 및 비밀번호 암호화
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

        String verificationCode = generateVerificationCode();

        User user = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .provider(Provider.LOCAL)
                .createdAt(LocalDateTime.now())
                .emailVerified(false)
                .emailVerificationToken(verificationCode)
                .emailTokenGeneratedAt(LocalDateTime.now())
                .build();

        int result = userMapper.insert(user);
        if (result < 1) {
            throw new NotInsertUserException("회원가입이 정상적으로 되지 않았습니다.");
        }

        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return UserResponseDTO.from(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String token) {
        User user = userMapper.findByEmailVerificationToken(token);
        if (user == null) {
            throw new NotFoundUserException("유효하지 않은 인증 토큰입니다.");
        }

        LocalDateTime tokenGeneratedAt = user.getEmailTokenGeneratedAt();
        if (tokenGeneratedAt == null) {
            throw new InvalidStateException("이미 처리된 토큰이거나 토큰 생성 시간이 기록되지 않았습니다.");
        }

        if (tokenGeneratedAt.plusMinutes(EMAIL_CODE_TTL_MINUTES).isBefore(LocalDateTime.now())) {
            userMapper.deleteById(user.getId());
            throw new InvalidStateException("인증 시간이 만료되었습니다. 회원가입을 다시 시도해주세요.");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailTokenGeneratedAt(null);
        userMapper.updateEmailVerification(user);
    }


    /**
     * 로그인 로직
     * <p>
     * 액세스 토큰과 리프레쉬 토큰 발급(또는 업데이트)
     *
     * @param req 로그인 요청 DTO
     * @return 로그인된 유저와 토큰 정보
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponseDTO login(UserLoginRequestDTO req) {
        User user = userMapper.findByEmail(req.getEmail());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new LoginFailedException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new LoginFailedException("이메일 인증이 필요합니다. 가입하신 이메일을 확인해주세요.");
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

        return UserLoginResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .build();
    }

    /**
     * 액세스 토큰 재발급 로직
     * <p>
     * 리프레쉬 토큰의 유효성 검사 후 액세스 토큰 재발급
     *
     * @param req 요청 받은 리프레쉬 토큰
     * @return 새로운 액세스 토큰 정보
     */
    @Transactional(rollbackFor = Exception.class)
    public TokenResponseDTO refreshAccessToken(TokenRefreshRequestDTO req) {
        String refreshTokenString = req.getRefreshToken();

        RefreshToken refreshToken = refreshTokenMapper.findByToken(refreshTokenString);
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenMapper.deleteByUserId(refreshToken.getUserId());
            throw new InvalidRefreshTokenException("만료된 리프레쉬 토큰입니다. 다시 로그인해주세요.");
        }

        User user = userMapper.findById(refreshToken.getUserId());
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .build();
    }

    /**
     * 로그아웃 로직
     * <p>
     * 로그아웃 수행 시 리프레쉬 토큰 삭제
     *
     * @param id 인증된 유저의 PK
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
        refreshTokenMapper.deleteByUserId(user.getId());
    }

    private String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(EMAIL_CODE_LENGTH);
        for (int i = 0; i < EMAIL_CODE_LENGTH; i++) {
            int idx = SECURE_RANDOM.nextInt(EMAIL_CODE_POOL.length());
            sb.append(EMAIL_CODE_POOL.charAt(idx));
        }
        return sb.toString();
    }
}
