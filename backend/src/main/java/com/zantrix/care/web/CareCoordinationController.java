package com.zantrix.care.web;

import com.zantrix.care.CareTeamRequest;
import com.zantrix.care.CareTeamSummary;
import com.zantrix.care.GoalRequest;
import com.zantrix.care.GoalSummary;
import com.zantrix.care.internal.CareCoordinationService;
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
@RequestMapping("/api/v1/care")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class CareCoordinationController {

    private final CareCoordinationService care;

    public CareCoordinationController(CareCoordinationService care) { this.care = care; }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public CareTeamSummary defineTeam(@Valid @RequestBody CareTeamRequest request) {
        return care.defineTeam(request);
    }

    @GetMapping("/teams")
    public List<CareTeamSummary> teams(@RequestParam String patientId) { return care.teams(patientId); }

    @PostMapping("/teams/{id}/stand-down")
    public CareTeamSummary standDownTeam(@PathVariable String id, @RequestParam String patientId) {
        return care.standDownTeam(id, patientId);
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalSummary addGoal(@Valid @RequestBody GoalRequest request) { return care.addGoal(request); }

    @GetMapping("/goals")
    public List<GoalSummary> goals(@RequestParam String patientId,
                                   @RequestParam(defaultValue = "false") boolean includeClosed) {
        return care.goals(patientId, includeClosed);
    }

    @PostMapping("/goals/{id}/close")
    public GoalSummary closeGoal(@PathVariable String id, @RequestParam String patientId,
                                 @RequestParam String outcome) {
        return care.closeGoal(id, patientId, outcome);
    }
}
