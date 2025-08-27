package com.itjima_server.service;

import com.itjima_server.domain.user.Provider;
import com.itjima_server.domain.user.RefreshToken;
import com.itjima_server.domain.user.User;
import com.itjima_server.dto.user.request.TokenRefreshRequestDTO;
import com.itjima_server.dto.user.request.UserFindEmailRequestDTO;
import com.itjima_server.dto.user.request.UserFindPasswordRequestDTO;
import com.itjima_server.dto.user.request.UserLoginRequestDTO;
import com.itjima_server.dto.user.request.UserRegisterRequestDTO;
import com.itjima_server.dto.user.response.KakaoTokenResponseDTO;
import com.itjima_server.dto.user.response.KakaoUserInfoDTO;
import com.itjima_server.dto.user.response.TokenResponseDTO;
import com.itjima_server.dto.user.response.UserFindEmailResponseDTO;
import com.itjima_server.dto.user.response.UserLoginResponseDTO;
import com.itjima_server.dto.user.response.UserResponseDTO;
import com.itjima_server.exception.common.InvalidStateException;
import com.itjima_server.exception.common.UpdateFailedException;
import com.itjima_server.exception.user.DuplicateUserFieldException;
import com.itjima_server.exception.user.InvalidRefreshTokenException;
import com.itjima_server.exception.user.LoginFailedException;
import com.itjima_server.exception.user.NotInsertUserException;
import com.itjima_server.mapper.RefreshTokenMapper;
import com.itjima_server.mapper.UserMapper;
import com.itjima_server.security.JwtTokenProvider;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 인증 관련 비즈니스 로직을 담당하는 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-28
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int EMAIL_CODE_LENGTH = 6;
    private static final int EMAIL_CODE_TTL_MINUTES = 5;
    private static final String EMAIL_CODE_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

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

    /**
     * 이메일 인증 처리
     *
     * @param token 인증할 인증번호
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String token) {
        User user = userMapper.findByEmailVerificationToken(token);
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
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
     * 카카오 로그인
     * <p>
     * 인가 코드로 카카오 액세스 토큰 발급 → 사용자 정보 조회 → 기존 계정 연동 또는 신규 생성 → 자체 JWT(access/refresh) 발급까지 처리한다.
     *
     * @param code 카카오 인가 코드(authorization_code)
     * @return 로그인 결과(JWT 포함)
     */
    public UserLoginResponseDTO kakaoLogin(String code) {
        String kakaoAccessToken = getKakaoAccessToken(code);
        KakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoAccessToken);
        User user = findOrCreateUser(userInfo);
        return issueJwtTokens(user);
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

        return issueJwtTokens(user);
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

    /**
     * 사용자의 이메일 찾기
     *
     * @param req 이메일을 찾기 위한 정보 DTO
     * @return 찾은 마스킹된 이메일
     */
    @Transactional(readOnly = true)
    public UserFindEmailResponseDTO findEmail(UserFindEmailRequestDTO req) {
        User user = userMapper.findByNameAndPhone(req.getName(), req.getPhone());
        if (user == null) {
            throw new IllegalArgumentException("입력하신 정보와 일치하는 사용자가 없습니다.");
        }
        return UserFindEmailResponseDTO.from(user.getEmail());
    }

    /**
     * 비밀번호 찾기(인증코드 발송)
     *
     * @param req 비밀번호를 찾기 위한 정보 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendPasswordResetCode(UserFindPasswordRequestDTO req) {
        User user = userMapper.findByNameAndPhoneAndEmail(req.getName(), req.getPhone(),
                req.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("입력하신 정보와 일치하는 사용자가 없습니다.");
        }

        String passwordResetCode = generateVerificationCode();

        user.setPasswordResetToken(passwordResetCode);
        user.setPasswordTokenGeneratedAt(LocalDateTime.now());

        emailService.sendPasswordReset(user.getEmail(), passwordResetCode);

        checkUpdateResult(
                userMapper.updatePasswordResetById(user.getId(), user.getPasswordResetToken(),
                        user.getPasswordTokenGeneratedAt(), null),
                "비밀번호 변경 요청이 정상적으로 완료되지 않았습니다.");
    }

    /**
     * 비밀번호 재설정
     *
     * @param code     인증 코드
     * @param password 새 비밀번호
     */
    @Transactional(rollbackFor = Exception.class)
    public void passwordReset(String code, String password) {
        User user = userMapper.findByPasswordResetToken(code);
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
        }

        LocalDateTime tokenGeneratedAt = user.getPasswordTokenGeneratedAt();
        if (tokenGeneratedAt == null) {
            throw new InvalidStateException("이미 처리된 토큰이거나 토큰 생성 시간이 기록되지 않았습니다.");
        }
        if (tokenGeneratedAt.plusMinutes(EMAIL_CODE_TTL_MINUTES).isBefore(LocalDateTime.now())) {
            throw new InvalidStateException("인증 시간이 만료되었습니다. 다시 시도해주세요.");
        }
        user.setPasswordResetToken(null);
        user.setPasswordTokenGeneratedAt(null);
        checkUpdateResult(
                userMapper.updatePasswordResetById(user.getId(), user.getPasswordResetToken(),
                        user.getPasswordTokenGeneratedAt(), passwordEncoder.encode(password)),
                "비밀번호 변경 중 오류가 발생했습니다.");
    }

    // ==========================
    // 내부 유틸리티 (카카오 로그인용)
    // ==========================

    /**
     * 카카오 액세스 토큰 발급
     * <p>
     * 인가 코드로 카카오 토큰 엔드포인트에 요청하여 액세스 토큰을 획득한다.
     *
     * @param code 인가 코드(authorization_code)
     * @return 카카오 액세스 토큰 문자열
     * @throws RuntimeException 카카오 API 통신 실패 또는 유효한 토큰을 받지 못한 경우
     */
    private String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params,
                headers);

        try {
            ResponseEntity<KakaoTokenResponseDTO> response = restTemplate.exchange(
                    kakaoTokenUri,
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    KakaoTokenResponseDTO.class
            );

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new RuntimeException("카카오로부터 액세스 토큰을 받아오지 못했습니다.");
            }
            return response.getBody().getAccessToken();

        } catch (RestClientException e) {
            throw new RuntimeException("카카오 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 카카오 사용자 정보 조회
     * <p>
     * 발급받은 액세스 토큰으로 사용자 정보 API를 호출하여 카카오 사용자 정보를 가져온다.
     *
     * @param accessToken 카카오 액세스 토큰
     * @return 카카오 사용자 정보 DTO
     * @throws RuntimeException 카카오 API 통신 실패 또는 유효한 사용자 정보를 받지 못한 경우
     */
    private KakaoUserInfoDTO getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoDTO> response = restTemplate.exchange(
                    kakaoUserInfoUri,
                    HttpMethod.POST,
                    kakaoUserInfoRequest,
                    KakaoUserInfoDTO.class
            );

            if (response.getBody() == null || response.getBody().getId() == null) {
                throw new RuntimeException("카카오로부터 사용자 정보를 받아오지 못했습니다.");
            }
            return response.getBody();

        } catch (RestClientException e) {
            throw new RuntimeException("카카오 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 카카오 사용자로 로컬 사용자 찾기/생성
     * <p>
     * 1) provider/providerId로 기존 사용자를 조회하고, 없으면<br> 2) 동일 이메일의 로컬 계정을 찾아 연동하며, 그것도 없으면<br> 3) 신규
     * 사용자를 생성한다.
     *
     * @param userInfo 카카오 사용자 정보
     * @return 로컬 DB의 User 엔티티
     */
    private User findOrCreateUser(KakaoUserInfoDTO userInfo) {
        // 1. provider와 providerId로 먼저 사용자를 찾기
        User user = userMapper.findByProviderAndProviderId(Provider.KAKAO, userInfo.getId());

        if (user != null) {
            // 2. 이미 카카오로 가입한 사용자는 바로 반환
            return user;
        }

        // 3. 카카오 정보는 없지만, 동일한 이메일의 로컬 계정이 있는지 확인
        String email = userInfo.getKakaoAccount().getEmail();
        User existingUser = userMapper.findByEmail(email);

        if (existingUser != null) {
            // 4. 이미 이메일로 가입한 사용자가 있다면, 계정을 연동
            existingUser.setProvider(Provider.KAKAO);
            existingUser.setProviderId(userInfo.getId());
            userMapper.updateProviderInfo(existingUser);
            return existingUser;
        } else {
            // 5. 정말 처음 방문한 신규 사용자일 경우, 새로 가입
            user = User.builder()
                    .name(userInfo.getProperties().getNickname())
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .provider(Provider.KAKAO)
                    .providerId(userInfo.getId())
                    .emailVerified(true) // 소셜 로그인은 이메일이 인증된 것으로 간주
                    .createdAt(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            return user;
        }
    }

    // ==========================
    // 내부 유틸리티 (JWT 발급 공통)
    // ==========================

    /**
     * 액세스/리프레시 토큰 발급 및 리프레시 토큰 저장
     *
     * @param user 토큰을 발급할 사용자
     * @return 로그인 응답 DTO(JWT 포함)
     */
    private UserLoginResponseDTO issueJwtTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshExpirationMs() / 1000);
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
     * 6자리 영문 대문자/숫자 조합의 이메일 인증코드를 생성
     *
     * @return 인증코드 문자열
     */
    private String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(EMAIL_CODE_LENGTH);
        for (int i = 0; i < EMAIL_CODE_LENGTH; i++) {
            int idx = SECURE_RANDOM.nextInt(EMAIL_CODE_POOL.length());
            sb.append(EMAIL_CODE_POOL.charAt(idx));
        }
        return sb.toString();
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
