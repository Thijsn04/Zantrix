package com.zantrix.cds;

public final class MedicationCodeValidationException extends RuntimeException {
    private final boolean unavailable;

    public MedicationCodeValidationException(String message, boolean unavailable, Throwable cause) {
        super(message, cause);
        this.unavailable = unavailable;
    }

    public boolean unavailable() { return unavailable; }
}
