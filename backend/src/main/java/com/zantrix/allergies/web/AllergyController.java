package com.zantrix.allergies.web;

import com.zantrix.allergies.AllergyRequest;
import com.zantrix.allergies.AllergySummary;
import com.zantrix.allergies.internal.AllergyService;
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
@RequestMapping("/api/v1/allergies")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class AllergyController {
    private final AllergyService allergies;
    public AllergyController(AllergyService allergies) { this.allergies = allergies; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AllergySummary add(@Valid @RequestBody AllergyRequest request) { return allergies.add(request); }

    @GetMapping
    public List<AllergySummary> list(@RequestParam String patientId,
                                     @RequestParam(defaultValue = "false") boolean includeInactive) {
        return allergies.list(patientId, includeInactive);
    }

    @PostMapping("/{id}/inactivate")
    public AllergySummary inactivate(@PathVariable String id, @RequestParam String patientId,
                                     @RequestParam(defaultValue = "false") boolean enteredInError) {
        return allergies.inactivate(id, patientId, enteredInError);
    }
}
