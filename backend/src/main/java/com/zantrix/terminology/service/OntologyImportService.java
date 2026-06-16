package com.zantrix.terminology.service;

import com.zantrix.terminology.domain.ImportJob;
import com.zantrix.terminology.repository.ImportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Service responsible for automatically importing medical terminologies into the FHIR server.
 * This pipeline scans the 'data/terminologies' folder on startup and processes any new
 * SNOMED CT, LOINC, or custom JSON CodeSystem files. It ensures terminologies are only
 * imported once by tracking them in the ImportJobRepository.
 */
@Service
public class OntologyImportService {

    private static final Logger log = LoggerFactory.getLogger(OntologyImportService.class);

    private final ImportJobRepository importJobRepository;
    private final RestTemplate restTemplate;

    @Value("${server.port:8080}")
    private String serverPort;

    public OntologyImportService(ImportJobRepository importJobRepository) {
        this.importJobRepository = importJobRepository;
        this.restTemplate = new RestTemplate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkAndImportTerminologies() {
        log.info("Starting automated terminology import pipeline check...");

        Path baseDir = Paths.get("data", "terminologies");
        if (!Files.exists(baseDir)) {
            log.info("No terminology folder found at {}. Skipping.", baseDir.toAbsolutePath());
            return;
        }

        // SNOMED CT
        processFolder(baseDir.resolve("snomed-ct"), "http://snomed.info/sct", true);
        // LOINC
        processFolder(baseDir.resolve("loinc"), "http://loinc.org", true);
        // Custom/ICD-10/DHD JSON
        processFolder(baseDir.resolve("custom"), null, false);
    }

    private void processFolder(Path folder, String systemUrl, boolean isZipUpload) {
        if (!Files.exists(folder)) {
            return;
        }

        File[] files = folder.toFile().listFiles((dir, name) -> name.endsWith(".zip") || name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (importJobRepository.findByFileName(file.getName()).isPresent()) {
                log.info("Terminology file {} was already imported. Skipping.", file.getName());
                continue;
            }

            log.info("Found new terminology file: {}. Starting import...", file.getName());
            ImportJob job = new ImportJob(file.getName(), "IN_PROGRESS", LocalDateTime.now());
            importJobRepository.save(job);

            try {
                if (isZipUpload && file.getName().endsWith(".zip")) {
                    uploadZipToFhir(file, systemUrl);
                } else if (file.getName().endsWith(".json")) {
                    uploadJsonCodeSystem(file);
                }
                
                job.setStatus("SUCCESS");
                job.setImportedAt(LocalDateTime.now());
                importJobRepository.save(job);
                log.info("Successfully imported terminology file: {}", file.getName());
                
            } catch (Exception e) {
                log.error("Failed to import terminology file: {}", file.getName(), e);
                job.setStatus("FAILED");
                job.setErrorMessage(e.getMessage());
                importJobRepository.save(job);
            }
        }
    }

    private void uploadZipToFhir(File file, String systemUrl) {
        String url = "http://localhost:" + serverPort + "/fhir/$upload-external-code-system";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("system", systemUrl);
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, requestEntity, String.class);
    }

    private void uploadJsonCodeSystem(File file) throws Exception {
        String url = "http://localhost:" + serverPort + "/fhir/CodeSystem";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonContent = Files.readString(file.toPath());
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonContent, headers);
        
        restTemplate.postForEntity(url, requestEntity, String.class);
    }
}
