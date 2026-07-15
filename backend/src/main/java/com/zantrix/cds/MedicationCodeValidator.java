package com.zantrix.cds;

/** Validates that a prescription code is a current RxNorm ingredient concept. */
public interface MedicationCodeValidator {
    void requireIngredient(String rxCui, String expectedDisplay);
}
