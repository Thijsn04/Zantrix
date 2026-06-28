package com.zantrix.interop;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "integration_channel")
public class IntegrationChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Protocol protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "jsonb")
    private String config; // Stored as JSON string for simplicity, or could use Hypersistence utils

    public enum Protocol {
        MLLP, HTTP, SFTP, MLLP_TLS
    }

    public enum Direction {
        INBOUND, OUTBOUND
    }

    public enum Status {
        ACTIVE, SUSPENDED, ERROR
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
}
