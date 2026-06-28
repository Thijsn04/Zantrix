package com.zantrix.charting.controller;

import com.zantrix.charting.domain.SmartTemplateEntity;
import com.zantrix.charting.repository.SmartTemplateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/charting/templates")
public class SmartTemplateController {

    private final SmartTemplateRepository repository;

    public SmartTemplateController(SmartTemplateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<SmartTemplateEntity>> getTemplates(@RequestParam UUID authorId) {
        // Returns both global (authorId = null) and personal templates
        return ResponseEntity.ok(repository.findByAuthorIdOrAuthorIdIsNull(authorId));
    }
    
    @PostMapping
    public ResponseEntity<SmartTemplateEntity> createTemplate(@RequestBody SmartTemplateEntity template) {
        return ResponseEntity.ok(repository.save(template));
    }
}
