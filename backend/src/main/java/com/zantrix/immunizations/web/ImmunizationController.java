package com.zantrix.immunizations.web;

import com.zantrix.immunizations.ImmunizationRequest;
import com.zantrix.immunizations.ImmunizationSummary;
import com.zantrix.immunizations.internal.ImmunizationService;
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
@RequestMapping("/api/v1/immunizations")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class ImmunizationController {

    private final ImmunizationService immunizations;

    public ImmunizationController(ImmunizationService immunizations) { this.immunizations = immunizations; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImmunizationSummary record(@Valid @RequestBody ImmunizationRequest request) {
        return immunizations.record(request);
    }

    @GetMapping
    public List<ImmunizationSummary> list(@RequestParam String patientId,
                                          @RequestParam(defaultValue = "false") boolean includeEnteredInError) {
        return immunizations.list(patientId, includeEnteredInError);
    }

    @PostMapping("/{id}/entered-in-error")
    public ImmunizationSummary markEnteredInError(@PathVariable String id, @RequestParam String patientId,
                                                  @RequestParam String reason) {
        return immunizations.markEnteredInError(id, patientId, reason);
    }
}
