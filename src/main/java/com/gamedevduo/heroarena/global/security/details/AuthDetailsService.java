package com.gamedevduo.heroarena.global.security.details;

import com.gamedevduo.heroarena.domain.user.repository.UserRepository;
import com.gamedevduo.heroarena.global.security.jwt.dto.UserCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {
        Long id = Long.parseLong(username);

        UserCredential credential = userRepository.findCredentialById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다" + id));

        return new AuthDetails(credential);
    }
}
