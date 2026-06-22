package com.planejamais.controller;

import com.planejamais.dto.FeedbackResponse;
import com.planejamais.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<FeedbackResponse> obter(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(feedbackService.obter(user.getUsername()));
    }
}
