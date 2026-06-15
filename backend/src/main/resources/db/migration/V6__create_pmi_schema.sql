CREATE TABLE patient (
    id UUID PRIMARY KEY,
    bsn VARCHAR(20) UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    merged_into_id UUID,
    fhir_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merged_into FOREIGN KEY (merged_into_id) REFERENCES patient (id)
);

CREATE INDEX idx_patient_bsn ON patient(bsn);
CREATE INDEX idx_patient_last_name ON patient(last_name);
CREATE INDEX idx_patient_birth_date ON patient(birth_date);
CREATE INDEX idx_patient_status ON patient(status);
