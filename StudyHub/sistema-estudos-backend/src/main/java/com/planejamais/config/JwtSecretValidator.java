package com.planejamais.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
public class JwtSecretValidator implements ApplicationRunner {

    private static final String DEFAULT_SECRET = "minha-chave-secreta-desenvolvimento-local-32chars";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.equals(DEFAULT_SECRET)) {
            throw new IllegalStateException(
                    "JWT_SECRET inválido em produção. Defina uma chave forte via variável de ambiente."
            );
        }
        if (jwtSecret.getBytes().length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres.");
        }
        log.info("JWT secret validado com sucesso.");
    }
}
