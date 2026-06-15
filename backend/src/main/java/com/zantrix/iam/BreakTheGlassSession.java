package com.zantrix.iam;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "break_the_glass_sessions")
public class BreakTheGlassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String reason;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public BreakTheGlassSession() {}

    public BreakTheGlassSession(String username, String reason, LocalDateTime expiresAt) {
        this.username = username;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getReason() { return reason; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
