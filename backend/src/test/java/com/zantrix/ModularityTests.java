package com.zantrix;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies the modular monolith boundaries. This fails the build if a module
 * reaches into another module's internals or if a dependency cycle appears.
 */
class ModularityTests {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(ZantrixApplication.class).verify();
    }
}
