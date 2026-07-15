package com.zantrix.administration.web;

import com.zantrix.administration.DirectorySummary;
import com.zantrix.administration.FeatureFlagView;
import com.zantrix.administration.LocationRequest;
import com.zantrix.administration.OrganizationRequest;
import com.zantrix.administration.PractitionerRequest;
import com.zantrix.administration.PractitionerSummary;
import com.zantrix.administration.internal.AdministrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/v1/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdministrationController {
    private final AdministrationService administration;
    public AdministrationController(AdministrationService administration){this.administration=administration;}
    @PostMapping("/organizations") @ResponseStatus(HttpStatus.CREATED)
    public DirectorySummary organization(@Valid @RequestBody OrganizationRequest request){return administration.createOrganization(request);}
    @GetMapping("/organizations") public List<DirectorySummary> organizations(){return administration.organizations();}
    @PostMapping("/locations") @ResponseStatus(HttpStatus.CREATED)
    public DirectorySummary location(@Valid @RequestBody LocationRequest request){return administration.createLocation(request);}
    @GetMapping("/locations") public List<DirectorySummary> locations(@RequestParam String organizationId){return administration.locations(organizationId);}
    @PostMapping("/practitioners") @ResponseStatus(HttpStatus.CREATED)
    public PractitionerSummary practitioner(@Valid @RequestBody PractitionerRequest request){return administration.createPractitioner(request);}
    @GetMapping("/practitioners") public List<PractitionerSummary> practitioners(){return administration.practitioners();}
    @GetMapping("/features") public List<FeatureFlagView> flags(){return administration.flags();}
    @PutMapping("/features/{name}") public FeatureFlagView flag(@PathVariable String name,@RequestParam boolean enabled){return administration.setFlag(name,enabled);}
}
