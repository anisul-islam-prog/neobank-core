package com.neobank;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Generates C4/PlantUML architecture documentation using Spring Modulith's Documenter.
 * Output is written to target/modulith-docs/.
 */
class ArchitectureDocumentationTest {

    @Test
    void generateDocumentation() {
        ApplicationModules modules = ApplicationModules.of(NeoBankCoreApplication.class);
        Documenter documenter = new Documenter(modules, "target/modulith-docs");

        documenter.writeDocumentation();
        documenter.writeModulesAsPlantUml();
        documenter.writeModuleCanvases();
    }
}
