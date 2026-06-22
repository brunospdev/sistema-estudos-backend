package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.PasswordResetToken;
import com.planejamais.entity.RefreshToken;
import com.planejamais.entity.Usuario;
import com.planejamais.exception.ConflictException;
import com.planejamais.exception.UnauthorizedException;
import com.planejamais.repository.PasswordResetTokenRepository;
import com.planejamais.repository.RefreshTokenRepository;
import com.planejamais.repository.UsuarioRepository;
import com.planejamais.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
public class AuthService extends BaseService {

    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final PreferenciasService preferenciasService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:Lax}")
    private String cookieSameSite;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       LoginAttemptService loginAttemptService,
                       PreferenciasService preferenciasService) {
        super(usuarioRepository);
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.loginAttemptService = loginAttemptService;
        this.preferenciasService = preferenciasService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("E-mail já cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .trialEndsAt(LocalDateTime.now().plusDays(14))
                .subscriptionStatus("trial")
                .build();

        usuarioRepository.save(usuario);
        preferenciasService.criarPreferenciasPadrao(usuario);
        log.info("Novo usuário registrado: {}", usuario.getEmail());

        return buildAuthResponse(usuario, response);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response, String clientIp) {
        loginAttemptService.assertIpNotAbusive(clientIp);
        loginAttemptService.assertNotLocked(request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    loginAttemptService.recordFailure(request.getEmail(), clientIp);
                    return new UnauthorizedException("Credenciais inválidas.");
                });

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            loginAttemptService.recordFailure(request.getEmail(), clientIp);
            throw new UnauthorizedException("Credenciais inválidas.");
        }

        loginAttemptService.recordSuccess(request.getEmail(), clientIp);
        log.info("Login bem-sucedido: {}", usuario.getEmail());
        return buildAuthResponse(usuario, response);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente.");
        }

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido."));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new UnauthorizedException("Refresh token expirado.");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUsuario(), response);
    }

    @Transactional
    public void logout(String rawRefreshToken, String email) {
        if (email != null) {
            Usuario usuario = getUsuario(email);
            refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        } else if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHashAndRevokedFalse(hashToken(rawRefreshToken))
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
        log.info("Logout realizado para: {}", email != null ? email : "token");
    }

    @Transactional
    public MessageResponse solicitarResetSenha(PasswordResetRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
        if (usuarioOpt.isEmpty()) {
            return new MessageResponse("Se o e-mail existir, enviaremos instruções de recuperação.");
        }

        Usuario usuario = usuarioOpt.get();
        passwordResetTokenRepository.deleteByUsuario_Id(usuario.getId());

        String rawToken = generateRawToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(hashToken(rawToken))
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(resetToken);

        log.info("Token de reset gerado para {} (stub — integração de e-mail pendente)", usuario.getEmail());
        return new MessageResponse("Se o e-mail existir, enviaremos instruções de recuperação.");
    }

    @Transactional
    public MessageResponse confirmarResetSenha(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(hashToken(request.getToken()))
                .orElseThrow(() -> new UnauthorizedException("Token de recuperação inválido."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Token de recuperação expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        resetToken.setUsed(true);
        usuarioRepository.save(usuario);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());

        log.info("Senha redefinida para: {}", usuario.getEmail());
        return new MessageResponse("Senha redefinida com sucesso.");
    }

    private AuthResponse buildAuthResponse(Usuario usuario, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(usuario.getEmail());
        String rawRefresh = generateRawToken();

        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hashToken(rawRefresh))
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build());

        addRefreshCookie(response, rawRefresh);
        return new AuthResponse(accessToken, usuario.getNome(), usuario.getEmail());
    }

    private void addRefreshCookie(HttpServletResponse response, String rawRefresh) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, rawRefresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
