package com.zantrix.iam;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
class UserProfile {

    @Id
    private String id; // Keycloak username or subject

    private String theme = "light";
    private String language = "en";

    protected UserProfile() {}

    public UserProfile(String id, String theme, String language) {
        this.id = id;
        this.theme = theme;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
