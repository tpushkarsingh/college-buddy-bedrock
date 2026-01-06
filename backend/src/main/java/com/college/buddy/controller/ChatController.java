package com.college.buddy.controller;

import com.college.buddy.entity.User;
import com.college.buddy.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        String query = request.get("query");
        String answer = ragService.chat(user.getId(), query);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, String>>> getChatHistory(
            @AuthenticationPrincipal User user) {
        List<Map<String, String>> history = ragService.getChatHistory(user.getId());
        return ResponseEntity.ok(history);
    }
}
