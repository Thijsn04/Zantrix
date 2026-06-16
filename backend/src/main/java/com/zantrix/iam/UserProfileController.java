package com.zantrix.iam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing the authenticated user's profile.
 * <p>
 * Provides endpoints for users to retrieve and update their personal preferences.
 * Actions here are isolated to the authenticated user context to prevent unauthorized access.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/iam/me")
class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    /**
     * Constructs a new {@link UserProfileController}.
     *
     * @param userProfileRepository The repository for user profiles.
     */
    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * Creates a default profile if none exists.
     *
     * @return The user's {@link UserProfile}.
     */
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

    /**
     * Updates the profile preferences of the currently authenticated user.
     *
     * @param updatedProfile The updated profile details.
     * @return The updated {@link UserProfile}.
     */
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
