package com.zantrix.iam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam/me")
class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping
    public UserProfile getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return userProfileRepository.findById(username)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(username, "light", "en");
                    return userProfileRepository.save(newProfile);
                });
    }

    @PutMapping
    public UserProfile updateMyProfile(@RequestBody UserProfile updatedProfile) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        UserProfile profile = userProfileRepository.findById(username)
                .orElse(new UserProfile(username, "light", "en"));
                
        if (updatedProfile.getTheme() != null) profile.setTheme(updatedProfile.getTheme());
        if (updatedProfile.getLanguage() != null) profile.setLanguage(updatedProfile.getLanguage());
        
        return userProfileRepository.save(profile);
    }
}
