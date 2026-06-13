package com.planejamais.service;

import com.planejamais.entity.LoginAttempt;
import com.planejamais.entity.Usuario;
import com.planejamais.exception.TooManyRequestsException;
import com.planejamais.repository.LoginAttemptRepository;
import com.planejamais.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lockout-minutes:15}")
    private int lockoutMinutes;

    public void assertNotLocked(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario != null && usuario.getLockedUntil() != null
                && usuario.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new TooManyRequestsException(
                    "Conta temporariamente bloqueada. Tente novamente mais tarde."
            );
        }
    }

    public void assertIpNotAbusive(String ip) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutMinutes);
        if (loginAttemptRepository.countFailedByIpSince(ip, since) >= maxAttempts * 3) {
            throw new TooManyRequestsException(
                    "Muitas tentativas deste endereço. Tente novamente mais tarde."
            );
        }
    }

    @Transactional
    public void recordSuccess(String email, String ip) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(email.toLowerCase().trim())
                .ipAddress(ip)
                .successful(true)
                .build());

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            usuario.setLockedUntil(null);
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void recordFailure(String email, String ip) {
        String normalizedEmail = email.toLowerCase().trim();
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(normalizedEmail)
                .ipAddress(ip)
                .successful(false)
                .build());

        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutMinutes);
        long failures = loginAttemptRepository.countFailedByEmailSince(normalizedEmail, since);

        if (failures >= maxAttempts) {
            usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                usuario.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                usuarioRepository.save(usuario);
            });
        }
    }
}
