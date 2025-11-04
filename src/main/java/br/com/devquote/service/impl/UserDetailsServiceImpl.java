package br.com.devquote.service.impl;
import br.com.devquote.entity.User;
import br.com.devquote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Carregando usuário: {}", username);

        User user = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        log.debug("Usuário encontrado: {}, Perfis: {}", user.getUsername(), user.getActiveProfileCodes());

        return user;
    }
}