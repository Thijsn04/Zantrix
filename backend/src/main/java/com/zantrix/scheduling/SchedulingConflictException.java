package com.zantrix.scheduling;

import java.util.List;

public final class SchedulingConflictException extends RuntimeException {

    private final List<AppointmentSummary> conflicts;

    public SchedulingConflictException(List<AppointmentSummary> conflicts) {
        super("The requested resource is already booked");
        this.conflicts = List.copyOf(conflicts);
    }

    public List<AppointmentSummary> conflicts() {
        return conflicts;
    }
}
