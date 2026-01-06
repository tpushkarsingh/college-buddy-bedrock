package com.college.buddy.controller;

import com.college.buddy.entity.User;
import com.college.buddy.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'FACULTY_ASSISTANT')")
    public ResponseEntity<Void> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("departmentCode") String departmentCode,
            @RequestParam("targetYear") Integer targetYear,
            @RequestParam(value = "targetSection", required = false) String targetSection,
            @RequestParam("subject") String subject,
            @AuthenticationPrincipal User user) throws IOException {
        documentService.uploadDocument(file, user.getId(), departmentCode, targetYear, targetSection, subject);
        return ResponseEntity.ok().build();
    }
}
