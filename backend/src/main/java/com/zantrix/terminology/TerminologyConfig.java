package com.zantrix.terminology;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class for the Terminology module.
 * <p>
 * Enables asynchronous processing capabilities, primarily supporting the
 * background ingestion of massive terminology datasets like SNOMED CT without
 * blocking main application threads.
 * </p>
 */
@Configuration
@EnableAsync
public class TerminologyConfig {
}
