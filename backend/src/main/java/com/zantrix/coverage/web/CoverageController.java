package com.zantrix.coverage.web;

import com.zantrix.coverage.CoverageRequest;
import com.zantrix.coverage.CoverageSummary;
import com.zantrix.coverage.internal.CoverageService;
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
@RequestMapping("/api/v1/coverage")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','ADMIN')")
public class CoverageController {

    private final CoverageService coverage;

    public CoverageController(CoverageService coverage) { this.coverage = coverage; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoverageSummary add(@Valid @RequestBody CoverageRequest request) { return coverage.add(request); }

    @GetMapping
    public List<CoverageSummary> list(@RequestParam String patientId,
                                      @RequestParam(defaultValue = "false") boolean includeCancelled) {
        return coverage.list(patientId, includeCancelled);
    }

    @PostMapping("/{id}/cancel")
    public CoverageSummary cancel(@PathVariable String id, @RequestParam String patientId) {
        return coverage.cancel(id, patientId);
    }
}
