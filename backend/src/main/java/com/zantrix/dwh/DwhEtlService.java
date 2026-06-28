package com.zantrix.dwh;

import org.springframework.stereotype.Service;
import java.util.List;
import com.zantrix.adt.LocationRepository;
import com.zantrix.adt.LocationEntity;
import com.zantrix.scheduling.repository.AppointmentRepository;

@Service
public class DwhEtlService {

    private final DwhFactRepository factRepository;
    private final LocationRepository locationRepository;
    private final AppointmentRepository appointmentRepository;

    public DwhEtlService(DwhFactRepository factRepository,
                         LocationRepository locationRepository,
                         AppointmentRepository appointmentRepository) {
        this.factRepository = factRepository;
        this.locationRepository = locationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // In a real system, this would be a scheduled task or event-driven listener extracting from ADT and Scheduling
    public void runEtlProcess() {
        // Calculate aggregations from operational tables and save them
        List<LocationEntity> beds = locationRepository.findByLevel(LocationEntity.Level.BED);
        long totalBeds = beds.size();
        long occupiedBeds = beds.stream().filter(b -> b.getStatus() == LocationEntity.Status.OCCUPIED).count();
        double occupancyRate = totalBeds == 0 ? 0 : ((double) occupiedBeds / totalBeds) * 100.0;
        
        long activeAdmissions = occupiedBeds; // For simplicity, admissions match occupied beds
        long waitlistSize = appointmentRepository.count(); // Using total appointments as waitlist size proxy for now
        
        factRepository.save(new DwhFactEntity("occupancyRate", occupancyRate, "general_ward", "daily"));
        factRepository.save(new DwhFactEntity("activeAdmissions", (double) activeAdmissions, "hospital", "daily"));
        factRepository.save(new DwhFactEntity("waitlistSize", (double) waitlistSize, "cardiology", "daily"));
        factRepository.save(new DwhFactEntity("systemUptime", 99.9, "infrastructure", "daily"));
    }
    
    public List<DwhFactEntity> getRealtimeMetrics() {
        // Ensure data is fresh
        runEtlProcess();
        return factRepository.findAll();
    }
}
