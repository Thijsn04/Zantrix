package com.zantrix.dwh;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dwh_fact_metric")
public class DwhFactEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private String metricName; // e.g. "occupancyRate", "waitlistSize"
    
    private Double metricValue;
    
    private String dimension1; // e.g. department
    
    private String dimension2; // e.g. timeframe
    
    private OffsetDateTime calculatedAt = OffsetDateTime.now();

    protected DwhFactEntity() {}

    public DwhFactEntity(String metricName, Double metricValue, String dimension1, String dimension2) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }
    public String getDimension1() { return dimension1; }
    public void setDimension1(String dimension1) { this.dimension1 = dimension1; }
    public String getDimension2() { return dimension2; }
    public void setDimension2(String dimension2) { this.dimension2 = dimension2; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
