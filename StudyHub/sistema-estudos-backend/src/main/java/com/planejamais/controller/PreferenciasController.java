package com.planejamais.controller;

import com.planejamais.dto.PreferenciasRequest;
import com.planejamais.dto.PreferenciasResponse;
import com.planejamais.service.PreferenciasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferencias")
@RequiredArgsConstructor
public class PreferenciasController {

    private final PreferenciasService preferenciasService;

    @GetMapping
    public ResponseEntity<PreferenciasResponse> obter(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(preferenciasService.getOrCreate(user.getUsername()));
    }

    @PutMapping
    public ResponseEntity<PreferenciasResponse> atualizar(
            @Valid @RequestBody PreferenciasRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(preferenciasService.update(user.getUsername(), request));
    }
}
