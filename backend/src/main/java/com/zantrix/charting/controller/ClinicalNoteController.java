package com.zantrix.charting.controller;

import com.zantrix.charting.domain.ClinicalNoteEntity;
import com.zantrix.charting.domain.ClinicalNoteAddendumEntity;
import com.zantrix.charting.service.ClinicalNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.zantrix.iam.AuditLoggable;

@RestController
@RequestMapping("/api/v1/charting/notes")
public class ClinicalNoteController {

    private final ClinicalNoteService noteService;

    public ClinicalNoteController(ClinicalNoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalNoteEntity>> getNotes(@PathVariable UUID patientId) {
        return ResponseEntity.ok(noteService.getNotesForPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<ClinicalNoteEntity> saveDraft(@RequestBody ClinicalNoteEntity note) {
        return ResponseEntity.ok(noteService.saveDraft(note));
    }

    @PostMapping("/{id}/sign")
    @AuditLoggable
    public ResponseEntity<ClinicalNoteEntity> signNote(@PathVariable UUID id, @RequestParam UUID authorId) {
        // In a real scenario, authorId comes from the security context (JWT)
        return ResponseEntity.ok(noteService.signNote(id, authorId));
    }

    @PostMapping("/{id}/addendum")
    public ResponseEntity<ClinicalNoteAddendumEntity> addAddendum(
            @PathVariable UUID id,
            @RequestParam UUID authorId,
            @RequestBody AddendumRequest request) {
        return ResponseEntity.ok(noteService.addAddendum(id, authorId, request.content()));
    }
    
    @GetMapping("/{id}/addendums")
    public ResponseEntity<List<ClinicalNoteAddendumEntity>> getAddendums(@PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getAddendums(id));
    }

    @PutMapping("/{id}/error")
    public ResponseEntity<ClinicalNoteEntity> markEnteredInError(
            @PathVariable UUID id, 
            @RequestParam String reason) {
        return ResponseEntity.ok(noteService.markEnteredInError(id, reason));
    }

    public record AddendumRequest(String content) {}
}
