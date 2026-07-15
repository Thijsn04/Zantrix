package com.zantrix.patient.internal;

import java.util.List;

record MergeManifest(List<MergedResource> resources) {
    MergeManifest { resources=List.copyOf(resources); }
    record MergedResource(String resourceType,String id,String postMergeFingerprint) { }
}
