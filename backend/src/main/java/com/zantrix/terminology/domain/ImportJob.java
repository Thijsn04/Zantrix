package com.zantrix.terminology.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trm_import_job")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", unique = true, nullable = false)
    private String fileName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    public ImportJob() {}

    public ImportJob(String fileName, String status, LocalDateTime importedAt) {
        this.fileName = fileName;
        this.status = status;
        this.importedAt = importedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
