package com.zantrix.privacy.web;

import com.zantrix.platform.security.EmergencyAccessReview;
import com.zantrix.platform.security.EmergencyAccessReviewRecorder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/privacy/emergency-reviews") @PreAuthorize("hasRole('PRIVACY_OFFICER')")
public class EmergencyAccessReviewController {
    private final EmergencyAccessReviewRecorder reviews;
    public EmergencyAccessReviewController(EmergencyAccessReviewRecorder reviews){this.reviews=reviews;}
    @GetMapping public List<EmergencyAccessReview> open(){return reviews.openReviews();}
    @PostMapping("/{id}/review") public EmergencyAccessReview review(@PathVariable UUID id,@RequestParam String outcome){
        return reviews.review(id,outcome);
    }
}
