package com.zantrix.cpoe.controller;

import com.zantrix.cpoe.domain.MedicationRequestEntity;
import com.zantrix.cpoe.domain.ServiceRequestEntity;
import com.zantrix.cpoe.service.CpoeOrderService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cpoe")
public class CpoeOrderController {

    private final CpoeOrderService orderService;

    public CpoeOrderController(CpoeOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/drafts/medication/{patientId}")
    public ResponseEntity<List<MedicationRequestEntity>> getDraftMedications(@PathVariable UUID patientId) {
        return ResponseEntity.ok(orderService.getDraftMedications(patientId));
    }

    @GetMapping("/drafts/service/{patientId}")
    public ResponseEntity<List<ServiceRequestEntity>> getDraftServices(@PathVariable UUID patientId) {
        return ResponseEntity.ok(orderService.getDraftServices(patientId));
    }

    @PostMapping("/orders/medication/draft")
    public ResponseEntity<MedicationRequestEntity> saveDraftMedication(@RequestBody MedicationRequestEntity order) {
        return ResponseEntity.ok(orderService.saveDraftMedication(order));
    }

    @PostMapping("/orders/service/draft")
    public ResponseEntity<ServiceRequestEntity> saveDraftService(@RequestBody ServiceRequestEntity order) {
        return ResponseEntity.ok(orderService.saveDraftService(order));
    }

    @PostMapping("/orders/sign")
    @AuditLoggable
    public ResponseEntity<Void> signOrders(@RequestBody SignOrdersRequest request) {
        orderService.signOrders(request.medicationIds(), request.serviceIds());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/orders/medication/{id}/cancel")
    @AuditLoggable
    public ResponseEntity<Void> cancelMedicationOrder(@PathVariable UUID id, @RequestParam String reason) {
        orderService.cancelMedicationOrder(id, reason);
        return ResponseEntity.ok().build();
    }

    public record SignOrdersRequest(List<UUID> medicationIds, List<UUID> serviceIds) {}
}
