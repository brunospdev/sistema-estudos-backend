package com.planejamais.controller;

import com.planejamais.dto.EventoResponse;
import com.planejamais.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar(
            @RequestParam(defaultValue = "todos") String filtro,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(eventoService.listar(user.getUsername(), filtro));
    }
}
