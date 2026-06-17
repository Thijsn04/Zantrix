package com.zantrix.terminology.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an asynchronous Terminology Import Job.
 * <p>
 * Tracks the state and outcome of terminology (SNOMED, LOINC, Custom CodeSystems)
 * import operations. This ensures that large terminology files are only imported
 * once and provides an audit trail for system administrators.
 * </p>
 */
@Entity
@Table(name = "trm_import_job")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the file being imported. */
    @Column(name = "file_name", unique = true, nullable = false)
    private String fileName;

    /** The current status of the import job (e.g., IN_PROGRESS, SUCCESS, FAILED). */
    @Column(name = "status", nullable = false)
    private String status;

    /** The timestamp when the import job completed or failed. */
    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    /** The error message generated during a failed import, if any. */
    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    /**
     * Default constructor for JPA.
     */
    public ImportJob() {}

    /**
     * Constructs a new ImportJob with the specified details.
     *
     * @param fileName   The name of the file being imported.
     * @param status     The initial status of the job.
     * @param importedAt The time the job was created or completed.
     */
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
