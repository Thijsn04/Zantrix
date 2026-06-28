package com.zantrix.adt;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;

@Entity(name = "AdtLocationEntity")
@Table(name = "location")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name; // e.g., "Cardiology Ward A"

    @Enumerated(EnumType.STRING)
    private Level level; // BUILDING, FLOOR, WARD, ROOM, BED

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private LocationEntity parent;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ElementCollection
    private List<String> characteristics;

    public enum Level {
        BUILDING, FLOOR, WARD, ROOM, BED
    }

    public enum Status {
        OCCUPIED, AVAILABLE, CLEANING_NEEDED, OUT_OF_ORDER
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public LocationEntity getParent() { return parent; }
    public void setParent(LocationEntity parent) { this.parent = parent; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<String> getCharacteristics() { return characteristics; }
    public void setCharacteristics(List<String> characteristics) { this.characteristics = characteristics; }
}
