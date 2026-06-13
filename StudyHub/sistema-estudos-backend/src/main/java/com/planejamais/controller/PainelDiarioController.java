package com.planejamais.controller;

import com.planejamais.dto.AssuntoResponse;
import com.planejamais.service.PainelDiarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/painel-diario")
@RequiredArgsConstructor
public class PainelDiarioController {

    private final PainelDiarioService painelDiarioService;

    @GetMapping
    public ResponseEntity<List<AssuntoResponse>> getPainelDiario(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(painelDiarioService.getPainelDiario(user.getUsername()));
    }
}
