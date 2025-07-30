package com.gamedevduo.heroarena.domain.auth.repository;

import com.gamedevduo.heroarena.domain.auth.entity.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
    AuthCode findByEmail(String email);
    void deleteByEmail(String email);
}
