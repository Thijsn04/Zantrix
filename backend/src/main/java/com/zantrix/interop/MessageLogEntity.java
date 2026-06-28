package com.zantrix.interop;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_log")
public class MessageLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID channelId;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public enum Status {
        PROCESSED, ERROR, RETRYING
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChannelId() { return channelId; }
    public void setChannelId(UUID channelId) { this.channelId = channelId; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
