package com.zantrix.encounter.web;

import com.zantrix.encounter.EncounterRequest;
import com.zantrix.encounter.EncounterSummary;
import com.zantrix.encounter.internal.EncounterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/encounters")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class EncounterController {

    private final EncounterService encounters;

    public EncounterController(EncounterService encounters) {
        this.encounters = encounters;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncounterSummary create(@Valid @RequestBody EncounterRequest request) {
        return encounters.create(request);
    }

    @GetMapping
    public List<EncounterSummary> list(@RequestParam String patientId) {
        return encounters.list(patientId);
    }

    @GetMapping("/{id}")
    public EncounterSummary get(@PathVariable String id, @RequestParam String patientId) {
        return encounters.get(id, patientId);
    }

    @PostMapping("/{id}/start")
    public EncounterSummary start(@PathVariable String id, @RequestParam String patientId) {
        return encounters.start(id, patientId);
    }

    @PostMapping("/{id}/finish")
    public EncounterSummary finish(@PathVariable String id, @RequestParam String patientId) {
        return encounters.finish(id, patientId);
    }

    @PostMapping("/{id}/cancel")
    public EncounterSummary cancel(@PathVariable String id, @RequestParam String patientId) {
        return encounters.cancel(id, patientId);
    }
}
