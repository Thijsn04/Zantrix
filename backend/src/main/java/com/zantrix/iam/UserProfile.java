package com.zantrix.iam;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a user's profile preferences.
 * <p>
 * Stores user-specific settings such as UI theme and language. It uses the Keycloak username
 * or subject ID as the primary key. Access to this entity should be restricted to the authenticated
 * user to maintain privacy.
 * </p>
 */
@Entity
@Table(name = "user_profiles")
class UserProfile {

    /**
     * The unique identifier of the user (Keycloak username or subject).
     */
    @Id
    private String id; 

    /**
     * The preferred UI theme (e.g., "light", "dark").
     */
    private String theme = "light";

    /**
     * The preferred language code (e.g., "en", "nl").
     */
    private String language = "en";

    /**
     * Default constructor for JPA.
     */
    protected UserProfile() {}

    /**
     * Constructs a new {@link UserProfile} with specified ID, theme, and language.
     *
     * @param id The unique identifier.
     * @param theme The preferred theme.
     * @param language The preferred language.
     */
    public UserProfile(String id, String theme, String language) {
        this.id = id;
        this.theme = theme;
        this.language = language;
    }

    /**
     * Retrieves the user ID.
     *
     * @return The user ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The user ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the user's preferred theme.
     *
     * @return The theme preference.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the user's preferred theme.
     *
     * @param theme The theme preference.
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Retrieves the user's preferred language.
     *
     * @return The language preference.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the user's preferred language.
     *
     * @param language The language preference.
     */
    public void setLanguage(String language) {
        this.language = language;
    }
}
