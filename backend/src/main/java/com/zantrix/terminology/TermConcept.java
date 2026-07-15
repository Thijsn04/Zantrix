package com.zantrix.terminology;

public record TermConcept(String system, String version, String code, String display, boolean inactive) { }
