package com.gamedevduo.heroarena.domain.user.repository;


import com.gamedevduo.heroarena.domain.user.entity.User;
import com.gamedevduo.heroarena.global.security.jwt.dto.UserCredential;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsUserByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsUserByName(String name);
    @Query("SELECT new com.gamedevduo.heroarena.global.security.jwt.dto.UserCredential(u.id, u.email, u.password) FROM User u WHERE u.id = :userId")
    Optional<UserCredential> findCredentialById(Long userId);
}