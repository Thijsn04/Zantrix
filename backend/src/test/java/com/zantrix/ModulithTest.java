package com.zantrix;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(ZantrixApplication.class);

    @Test
    void verifyModulithStructure() {
        // This will fail if there are cyclic dependencies or illegal accesses
        modules.verify();
    }
    
    @Test
    void createModuleDocumentation() {
        // Generates structural documentation
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
