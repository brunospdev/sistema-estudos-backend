package com.planejamais.controller;

import com.planejamais.dto.CalendarioResponse;
import com.planejamais.service.CalendarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;

    @GetMapping
    public ResponseEntity<CalendarioResponse> obter(
            @RequestParam("de") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam("ate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(calendarioService.obter(user.getUsername(), de, ate));
    }
}
