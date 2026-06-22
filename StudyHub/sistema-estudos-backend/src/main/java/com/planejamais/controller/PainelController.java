package com.planejamais.controller;

import com.planejamais.dto.PainelHojeItemResponse;
import com.planejamais.service.PainelHojeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
public class PainelController {

    private final PainelHojeService painelHojeService;

    @GetMapping("/hoje")
    public ResponseEntity<List<PainelHojeItemResponse>> getPainelHoje(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(painelHojeService.getPainelHoje(user.getUsername()));
    }
}
