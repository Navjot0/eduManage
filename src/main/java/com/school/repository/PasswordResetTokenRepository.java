package com.school.repository;

import com.school.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByUserIdAndTokenAndUsedAtIsNull(UUID userId, String token);
    void deleteByUserId(UUID userId);
}
