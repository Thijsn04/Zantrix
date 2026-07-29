package com.zantrix.care;

import java.util.List;

public record CareTeamSummary(String id, String patientId, String name, String status,
                              List<Member> members) {

    public CareTeamSummary { members = List.copyOf(members); }

    public record Member(String practitionerId, String roleCode, String roleDisplay) { }
}
