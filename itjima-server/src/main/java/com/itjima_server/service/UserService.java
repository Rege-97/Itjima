package com.itjima_server.service;

import com.itjima_server.domain.Provider;
import com.itjima_server.domain.User;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.exception.DuplicateUserFieldException;
import com.itjima_server.exception.NotInsertUserException;
import com.itjima_server.mapper.UserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO register(UserRegisterRequestDTO req) {
        if (userMapper.findByEmail(req.getEmail())) {
            throw new DuplicateUserFieldException("이미 사용 중인 이메일 입니다.");
        }

        if(userMapper.findByPhone(req.getPhone())){
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
}
