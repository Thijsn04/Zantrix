package com.zantrix.audit.web;

import com.zantrix.audit.AuditQuery;
import com.zantrix.audit.AuditRecord;
import com.zantrix.audit.AuditTrailVerifier;
import com.zantrix.audit.AuditVerificationResult;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController @RequestMapping("/api/v1/audit") @PreAuthorize("hasRole('PRIVACY_OFFICER')")
public class AuditController {
    private final AuditQuery query;private final AuditTrailVerifier verifier;
    public AuditController(AuditQuery query,AuditTrailVerifier verifier){this.query=query;this.verifier=verifier;}
    @GetMapping("/events") public Page<AuditRecord> events(@RequestParam(required=false)String patientId,
            @RequestParam(required=false)String actor,@RequestParam(required=false)String outcome,
            @RequestParam(required=false)Boolean breakTheGlass,@RequestParam(required=false)Instant from,
            @RequestParam(required=false)Instant to,@RequestParam(defaultValue="0")int page,
            @RequestParam(defaultValue="100")int size){
        return query.search(patientId,actor,outcome,breakTheGlass,from,to,page,size);
    }
    @GetMapping("/integrity") public AuditVerificationResult verify(){return verifier.verify();}
}
