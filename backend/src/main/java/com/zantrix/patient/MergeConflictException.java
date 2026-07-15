package com.zantrix.patient;

import java.util.List;

public final class MergeConflictException extends RuntimeException {
    private final List<String> changedResources;
    public MergeConflictException(List<String> changedResources) {
        super("Patient unmerge cannot proceed because repointed resources changed after merge");
        this.changedResources=List.copyOf(changedResources);
    }
    public List<String> changedResources(){return changedResources;}
}
