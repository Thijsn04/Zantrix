package com.zantrix.cpoe.service;

import com.zantrix.cpoe.domain.MedicationRequestEntity;
import com.zantrix.cpoe.domain.ServiceRequestEntity;
import com.zantrix.cpoe.domain.OrderStatus;
import com.zantrix.cpoe.repository.MedicationRequestRepository;
import com.zantrix.cpoe.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CpoeOrderService {

    private final MedicationRequestRepository medRepo;
    private final ServiceRequestRepository srvRepo;

    public CpoeOrderService(MedicationRequestRepository medRepo, ServiceRequestRepository srvRepo) {
        this.medRepo = medRepo;
        this.srvRepo = srvRepo;
    }

    public List<MedicationRequestEntity> getDraftMedications(UUID patientId) {
        return medRepo.findByPatientIdAndStatus(patientId, OrderStatus.DRAFT);
    }

    public List<ServiceRequestEntity> getDraftServices(UUID patientId) {
        return srvRepo.findByPatientIdAndStatus(patientId, OrderStatus.DRAFT);
    }

    @Transactional
    public MedicationRequestEntity saveDraftMedication(MedicationRequestEntity order) {
        order.setStatus(OrderStatus.DRAFT);
        return medRepo.save(order);
    }

    @Transactional
    public ServiceRequestEntity saveDraftService(ServiceRequestEntity order) {
        order.setStatus(OrderStatus.DRAFT);
        return srvRepo.save(order);
    }

    @Transactional
    public void signOrders(List<UUID> medicationIds, List<UUID> serviceIds) {
        if (medicationIds != null) {
            List<MedicationRequestEntity> meds = medRepo.findAllById(medicationIds);
            for (MedicationRequestEntity med : meds) {
                if (med.getStatus() != OrderStatus.DRAFT) {
                    throw new IllegalStateException("Only DRAFT orders can be signed.");
                }
                // TODO: Call CDS Module to check for interactions before signing
                med.setStatus(OrderStatus.ACTIVE);
                med.setSignedAt(OffsetDateTime.now());
                medRepo.save(med);
            }
        }
        
        if (serviceIds != null) {
            List<ServiceRequestEntity> srvs = srvRepo.findAllById(serviceIds);
            for (ServiceRequestEntity srv : srvs) {
                if (srv.getStatus() != OrderStatus.DRAFT) {
                    throw new IllegalStateException("Only DRAFT orders can be signed.");
                }
                srv.setStatus(OrderStatus.ACTIVE);
                srv.setSignedAt(OffsetDateTime.now());
                srvRepo.save(srv);
            }
        }
    }

    @Transactional
    public void cancelMedicationOrder(UUID orderId, String reason) {
        MedicationRequestEntity order = medRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(OrderStatus.CANCELLED);
        // Reason could be saved to audit log
        medRepo.save(order);
    }
}
