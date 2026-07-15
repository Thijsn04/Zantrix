package com.zantrix.terminology.web;

import com.zantrix.terminology.CodeValidation;
import com.zantrix.terminology.TermConcept;
import com.zantrix.terminology.internal.TerminologyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/terminology")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','PHARMACIST','ADMIN')")
public class TerminologyController {
    private final TerminologyService terminology;
    public TerminologyController(TerminologyService terminology) { this.terminology = terminology; }

    @GetMapping("/status")
    public Map<String, Object> status() { return terminology.status(); }

    @GetMapping("/snomed")
    public List<TermConcept> searchSnomed(@RequestParam String domain, @RequestParam String filter,
                                         @RequestParam(defaultValue = "20") int count,
                                         @RequestParam(defaultValue = "en") String language) {
        return terminology.searchSnomed(domain, filter, count, language);
    }

    @GetMapping("/expand")
    public List<TermConcept> expand(@RequestParam String url, @RequestParam(required = false) String filter,
                                    @RequestParam(defaultValue = "20") int count,
                                    @RequestParam(defaultValue = "en") String language) {
        return terminology.expand(url, filter, count, language);
    }

    @GetMapping("/validate-code")
    public CodeValidation validate(@RequestParam String system, @RequestParam String code,
                                   @RequestParam(required = false) String display) {
        return terminology.validate(system, code, display);
    }
}
