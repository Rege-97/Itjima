package com.itjima_server.mapper;

import com.itjima_server.domain.user.Provider;
import com.itjima_server.domain.user.User;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    boolean existsByEmail(@Param("email") String email);

    boolean existsByPhone(@Param("phone") String phone);

    int insert(User user);

    User findById(@Param("id") Long id);

    User findByEmail(@Param("email") String email);

    int updatePhoneById(@Param("id") Long id, @Param("phone") String phone);

    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    int updateEmailVerification(User user);

    User findByEmailVerificationToken(
            @Param("emailVerificationToken") String emailVerificationToken);

    int deleteById(@Param("id") Long id);

    User findByProviderAndProviderId(@Param("provider") Provider provider,
            @Param("providerId") String providerId);

    int updateProviderInfo(User user);

    User findByNameAndPhone(@Param("name") String name, @Param("phone") String phone);

    User findByNameAndPhoneAndEmail(@Param("name") String name, @Param("phone") String phone,
            @Param("email") String email);

    int updatePasswordResetById(@Param("id") Long id,
            @Param("passwordResetToken") String passwordResetToken,
            @Param("passwordTokenGeneratedAt")
            LocalDateTime passwordResetTokenGeneratedAt,
            @Param("password") String password);

    User findByPasswordResetToken(@Param("passwordResetToken") String passwordResetToken);

    int updateDeleteStatusById(@Param("id") Long id);
}
