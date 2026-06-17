package com.zantrix.terminology.api;

import com.zantrix.terminology.domain.Concept;
import com.zantrix.terminology.repository.ConceptRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminology")
public class TerminologySearchController {
    
    private final ConceptRepository conceptRepository;

    public TerminologySearchController(ConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @GetMapping("/search")
    public List<Concept> searchConcepts(@RequestParam String query) {
        return conceptRepository.findByPreferredTermContainingIgnoreCase(query);
    }
}
