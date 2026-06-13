package com.planejamais.controller;

import com.planejamais.dto.*;
import com.planejamais.service.EstudoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estudo")
@RequiredArgsConstructor
public class EstudoController {

    private final EstudoService estudoService;

    @GetMapping("/pomodoro")
    public ResponseEntity<PomodoroResponse> obterPomodoro(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(estudoService.obterPomodoro(user.getUsername()));
    }

    @PostMapping("/sessao")
    public ResponseEntity<SessaoEstudoResponse> registrarSessao(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(estudoService.registrarSessao(user.getUsername()));
    }

    @PutMapping("/pomodoro/config")
    public ResponseEntity<PomodoroResponse> salvarConfig(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PomodoroConfigRequest request
    ) {
        return ResponseEntity.ok(estudoService.salvarConfig(user.getUsername(), request));
    }
}
