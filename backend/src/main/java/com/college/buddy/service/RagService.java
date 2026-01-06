package com.college.buddy.service;

import com.college.buddy.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

        private final ChatClient chatClient;
        private final VectorStore vectorStore;
        private final EnrollmentRepository enrollmentRepository;
        private final ChatMemory chatMemory;

        public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                        EnrollmentRepository enrollmentRepository, ChatMemory chatMemory) {
                this.vectorStore = vectorStore;
                this.enrollmentRepository = enrollmentRepository;
                this.chatMemory = chatMemory;
                this.chatClient = chatClientBuilder
                                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                                .build();
        }

        public String chat(UUID userId, String query) {
                log.info("DEBUG: Chat request for user: {} with query: {}", userId, query);

                // 1. Get User's Context (Enrollment)
                var enrollment = enrollmentRepository.findByUserIdAndIsActiveTrue(userId)
                                .orElseThrow(() -> {
                                        log.error("DEBUG: No active enrollment found for user ID: {}", userId);
                                        return new RuntimeException("No active enrollment found for user");
                                });
                log.info("DEBUG: Found enrollment for department: {} and year: {}",
                                enrollment.getDepartment().getCode(), enrollment.getYear());

                // 2. Build Filter Expression
                String filter = String.format(
                                "department_id == '%s' && target_year == %d",
                                enrollment.getDepartment().getId(),
                                enrollment.getYear());

                log.info("DEBUG: Generated Filter Expression: {}", filter);

                // 3. Retrieve Documents
                // We've removed the "Text: " prefix workaround as we've switched to Cohere
                SearchRequest request = SearchRequest.builder()
                                .query(query)
                                .topK(5)
                                .filterExpression(filter)
                                .build();

                log.info("DEBUG: Performing similarity search with query: '{}' and filter: '{}'", request.getQuery(),
                                request.getFilterExpression());
                List<Document> docs = vectorStore.similaritySearch(request);
                log.info("DEBUG: Retrieved {} documents from vector store", docs.size());

                String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
                log.info("DEBUG: Context length: {}", context.length());

                // 4. Generate Response using ChatClient with Memory
                String systemText = "You are a helpful study assistant. Use the following context to answer the student's question.\n"
                                +
                                "Context:\n" + context;

                return chatClient.prompt()
                                .system(systemText)
                                .user(query)
                                .advisors(a -> a.param("chat_memory_conversation_id", userId.toString())
                                                .param("chat_memory_retrieve_size", 10))
                                .call()
                                .content();
        }

        public List<java.util.Map<String, String>> getChatHistory(UUID userId) {
                return new java.util.ArrayList<>();
        }
}
