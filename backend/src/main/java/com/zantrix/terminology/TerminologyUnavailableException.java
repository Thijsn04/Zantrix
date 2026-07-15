package com.zantrix.terminology;

public final class TerminologyUnavailableException extends RuntimeException {
    public TerminologyUnavailableException(Throwable cause) {
        super("The configured terminology service is unavailable", cause);
    }
}
