package com.zantrix.terminology.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "value_set")
public class ValueSetEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private String url;
    
    private String name;
    
    private String status;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @jakarta.persistence.Column(columnDefinition = "jsonb")
    private String compose; // The rules for the value set

    protected ValueSetEntity() {}

    public ValueSetEntity(String url, String name, String status, String compose) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.compose = compose;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCompose() { return compose; }
    public void setCompose(String compose) { this.compose = compose; }
}
