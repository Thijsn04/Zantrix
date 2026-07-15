package com.zantrix.problems.web;

import com.zantrix.problems.ProblemRequest;
import com.zantrix.problems.ProblemSummary;
import com.zantrix.problems.internal.ProblemService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class ProblemController {

    private final ProblemService problems;

    public ProblemController(ProblemService problems) { this.problems = problems; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemSummary add(@Valid @RequestBody ProblemRequest request) { return problems.add(request); }

    @GetMapping
    public List<ProblemSummary> list(@RequestParam String patientId,
                                     @RequestParam(defaultValue = "false") boolean includeResolved) {
        return problems.list(patientId, includeResolved);
    }

    @PostMapping("/{id}/resolve")
    public ProblemSummary resolve(@PathVariable String id, @RequestParam String patientId,
                                  @RequestParam(required = false) LocalDate resolvedDate) {
        return problems.resolve(id, patientId, resolvedDate);
    }
}
