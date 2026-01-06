package com.college.buddy.service;

import com.college.buddy.entity.Department;
import com.college.buddy.entity.User;
import com.college.buddy.repository.DepartmentRepository;
import com.college.buddy.repository.DocumentRepository;
import com.college.buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    @Value("${AWS_ACCESS_KEY_ID:NOT_SET}")
    private String awsAccessKey;

    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Transactional
    public void uploadDocument(
            MultipartFile file,
            UUID uploaderId,
            String departmentCode,
            Integer targetYear,
            String targetSection,
            String subject) throws IOException {

        log.info("DEBUG: Starting document upload for file: {}", file.getOriginalFilename());

        var uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var department = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 1. Read PDF
        Resource resource = new ByteArrayResource(file.getBytes());
        var reader = new PagePdfDocumentReader(resource);
        var rawDocuments = reader.read();
        log.info("DEBUG: Documents read from PDF: {}", rawDocuments.size());

        // 2. Split into chunks
        var splitter = new TokenTextSplitter();
        var documentsToSplit = splitter.apply(rawDocuments);
        log.info("DEBUG: Chunks generated: {}", documentsToSplit.size());

        // 3. Process Chunks
        List<org.springframework.ai.document.Document> processedDocuments = new ArrayList<>();
        int i = 0;
        for (org.springframework.ai.document.Document doc : documentsToSplit) {
            // Recreating without the prefix workaround as we've switched to Cohere
            var newDoc = new org.springframework.ai.document.Document(
                    doc.getId(),
                    doc.getText(),
                    doc.getMetadata());

            newDoc.getMetadata().put("department_id", department.getId().toString());
            newDoc.getMetadata().put("target_year", targetYear);
            if (targetSection != null && !targetSection.isBlank()) {
                newDoc.getMetadata().put("target_section", targetSection);
            }
            newDoc.getMetadata().put("subject", subject);
            newDoc.getMetadata().put("filename", file.getOriginalFilename());

            processedDocuments.add(newDoc);
            i++;
        }

        // 4. Store in Vector Store
        log.info("DEBUG: Sending {} documents to Vector Store...", processedDocuments.size());
        vectorStore.add(processedDocuments);

        // 5. Save Record to Database
        var docEntity = com.college.buddy.entity.Document.builder()
                .filename(file.getOriginalFilename())
                .uploadedBy(uploader)
                .department(department)
                .targetYear(targetYear)
                .targetSection(targetSection)
                .subject(subject)
                .vectorIds(processedDocuments.stream().map(org.springframework.ai.document.Document::getId).toList())
                .build();

        documentRepository.save(docEntity);
        log.info("DEBUG: Document upload process completed successfully.");
    }
}
