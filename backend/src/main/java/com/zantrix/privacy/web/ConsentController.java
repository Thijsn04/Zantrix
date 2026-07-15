package com.zantrix.privacy.web;

import com.zantrix.privacy.ConsentRequest;
import com.zantrix.privacy.ConsentSummary;
import com.zantrix.privacy.internal.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/consents")
@PreAuthorize("hasAnyRole('PHYSICIAN','PRIVACY_OFFICER')")
public class ConsentController {
    private final ConsentService consents;
    public ConsentController(ConsentService consents){this.consents=consents;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ConsentSummary create(@Valid @RequestBody ConsentRequest request){return consents.create(request);}
    @GetMapping public List<ConsentSummary> list(@RequestParam String patientId){return consents.list(patientId);}
    @PostMapping("/{id}/revoke") public ConsentSummary revoke(@PathVariable String id,@RequestParam String patientId){return consents.revoke(id,patientId);}
}
