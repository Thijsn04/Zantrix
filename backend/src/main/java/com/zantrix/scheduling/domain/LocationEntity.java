package com.zantrix.scheduling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity(name = "SchedulingLocationEntity")
@Table(name = "location")
public class LocationEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String name;

    private String type; // e.g. WARD, ROOM, BED

    private String characteristics; // e.g. ISOLATION, OXYGEN

    private String status; // ACTIVE, INACTIVE

    private UUID parentLocationId; // For hierarchical locations

    protected LocationEntity() {}

    public LocationEntity(String name, String type, String characteristics, String status, UUID parentLocationId) {
        this.name = name;
        this.type = type;
        this.characteristics = characteristics;
        this.status = status;
        this.parentLocationId = parentLocationId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCharacteristics() { return characteristics; }
    public void setCharacteristics(String characteristics) { this.characteristics = characteristics; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getParentLocationId() { return parentLocationId; }
    public void setParentLocationId(UUID parentLocationId) { this.parentLocationId = parentLocationId; }
}
