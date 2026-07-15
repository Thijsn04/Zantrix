package com.zantrix.terminology;

/** Fail-closed validation boundary used before coded clinical data is persisted. */
public interface TerminologyValidator {
    void requireValid(String system, String code, String display);
}
