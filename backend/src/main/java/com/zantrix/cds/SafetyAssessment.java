package com.zantrix.cds;

import java.util.List;

public record SafetyAssessment(
        String knowledgeBase,
        String version,
        String coverage,
        boolean comprehensive,
        List<SafetyIssue> issues) {

    public SafetyAssessment {
        issues = List.copyOf(issues);
    }

    public boolean blocksPrescribing() {
        return issues.stream().anyMatch(issue -> "critical".equals(issue.severity()));
    }
}
