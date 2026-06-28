package com.zantrix.charting.service;

import com.zantrix.charting.domain.ClinicalNoteEntity;
import com.zantrix.charting.domain.ClinicalNoteAddendumEntity;
import com.zantrix.charting.domain.NoteStatus;
import com.zantrix.charting.repository.ClinicalNoteRepository;
import com.zantrix.charting.repository.ClinicalNoteAddendumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClinicalNoteService {

    private final ClinicalNoteRepository noteRepository;
    private final ClinicalNoteAddendumRepository addendumRepository;

    public ClinicalNoteService(ClinicalNoteRepository noteRepository, ClinicalNoteAddendumRepository addendumRepository) {
        this.noteRepository = noteRepository;
        this.addendumRepository = addendumRepository;
    }

    public List<ClinicalNoteEntity> getNotesForPatient(UUID patientId) {
        return noteRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public ClinicalNoteEntity saveDraft(ClinicalNoteEntity note) {
        if (note.getId() != null) {
            ClinicalNoteEntity existing = noteRepository.findById(note.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
            if (existing.getStatus() == NoteStatus.FINAL || existing.getStatus() == NoteStatus.AMENDED) {
                throw new IllegalStateException("Cannot modify a signed note.");
            }
        }
        note.setStatus(NoteStatus.IN_PROGRESS);
        return noteRepository.save(note);
    }

    @Transactional
    public ClinicalNoteEntity signNote(UUID noteId, UUID authorId) {
        ClinicalNoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
            
        // Simplified authorization check
        if (!note.getAuthorId().equals(authorId) && note.getSupervisorId() != null && !note.getSupervisorId().equals(authorId)) {
             throw new SecurityException("Only the author or supervisor can sign this note.");
        }

        if (note.getStatus() == NoteStatus.FINAL) {
            throw new IllegalStateException("Note is already signed.");
        }

        note.setStatus(NoteStatus.FINAL);
        note.setSignedAt(OffsetDateTime.now());
        return noteRepository.save(note);
    }

    @Transactional
    public ClinicalNoteAddendumEntity addAddendum(UUID noteId, UUID authorId, String content) {
        ClinicalNoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (note.getStatus() != NoteStatus.FINAL && note.getStatus() != NoteStatus.AMENDED) {
            throw new IllegalStateException("Can only add addendum to a signed note.");
        }

        note.setStatus(NoteStatus.AMENDED);
        noteRepository.save(note);

        ClinicalNoteAddendumEntity addendum = new ClinicalNoteAddendumEntity(noteId, authorId, content);
        return addendumRepository.save(addendum);
    }
    
    @Transactional
    public ClinicalNoteEntity markEnteredInError(UUID noteId, String reason) {
        ClinicalNoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        note.setStatus(NoteStatus.ENTERED_IN_ERROR);
        // Note: Reason should ideally be stored in an audit log or an extension field.
        return noteRepository.save(note);
    }
    
    public List<ClinicalNoteAddendumEntity> getAddendums(UUID noteId) {
        return addendumRepository.findByOriginalNoteIdOrderByAddedAtAsc(noteId);
    }
}
