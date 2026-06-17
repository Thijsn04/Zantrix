package com.zantrix;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Test class to verify the architectural integrity of the Zantrix Modular Monolith.
 * <p>
 * Ensures that the bounded contexts (IAM, Scheduling, PMI, Terminology) do not
 * violate architectural boundaries (e.g., cyclic dependencies, unauthorized access to
 * internal packages).
 * </p>
 */
class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(ZantrixApplication.class);

    /**
     * Verifies that the application's module structure complies with Spring Modulith rules.
     * Fails the build if any architectural constraints are violated.
     */
    @Test
    void verifyModulithStructure() {
        // This will fail if there are cyclic dependencies or illegal accesses
        modules.verify();
    }
    
    /**
     * Automatically generates PlantUML diagrams documenting the current module
     * dependencies and structure. Useful for architecture reviews and documentation.
     */
    @Test
    void createModuleDocumentation() {
        // Generates structural documentation
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
