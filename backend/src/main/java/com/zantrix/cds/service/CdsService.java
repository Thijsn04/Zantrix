package com.zantrix.cds.service;

import com.zantrix.cds.domain.CdsAlertEntity;
import com.zantrix.cds.domain.AlertSeverity;
import com.zantrix.cds.repository.CdsAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CdsService {

    private final CdsAlertRepository repository;

    public CdsService(CdsAlertRepository repository) {
        this.repository = repository;
    }

    public List<CdsAlertEntity> getActiveAlerts(UUID patientId) {
        return repository.findByPatientIdAndDismissedFalseOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public CdsAlertEntity createAlert(UUID patientId, AlertSeverity severity, String title, String message) {
        return repository.save(new CdsAlertEntity(patientId, severity, title, message));
    }

    @Transactional
    public void dismissAlert(UUID alertId, UUID dismissedBy) {
        CdsAlertEntity alert = repository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setDismissed(true);
        alert.setDismissedAt(OffsetDateTime.now());
        alert.setDismissedBy(dismissedBy);
        repository.save(alert);
    }
}
