package com.gamedevduo.heroarena.domain.auth.repository;

import com.gamedevduo.heroarena.domain.auth.entity.AuthCode;
import com.gamedevduo.heroarena.domain.auth.entity.enums.VerifyCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
    AuthCode findByEmail(String email);
    Optional<AuthCode> findByEmailAndType(String email, VerifyCodeType type);
    void deleteByEmailAndType(String email, VerifyCodeType type);

}
