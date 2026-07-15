package com.zantrix.documentation.web;

import com.zantrix.documentation.NoteRequest;
import com.zantrix.documentation.NoteSummary;
import com.zantrix.documentation.internal.DocumentationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class DocumentationController {
    private final DocumentationService notes;
    public DocumentationController(DocumentationService notes) { this.notes = notes; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public NoteSummary draft(@Valid @RequestBody NoteRequest request) { return notes.draft(request); }

    @PutMapping("/{id}")
    public NoteSummary update(@PathVariable String id, @Valid @RequestBody NoteRequest request) {
        return notes.update(id, request);
    }

    @PostMapping("/{id}/sign")
    public NoteSummary sign(@PathVariable String id, @RequestParam String patientId,
                            @RequestParam String signerId) { return notes.sign(id, patientId, signerId); }

    @PostMapping("/{id}/addendum")
    public NoteSummary addendum(@PathVariable String id, @RequestParam String patientId,
                                @RequestParam String authorId, @RequestBody String text) {
        return notes.addendum(id, patientId, authorId, text);
    }

    @GetMapping
    public List<NoteSummary> list(@RequestParam String patientId) { return notes.list(patientId); }
}
