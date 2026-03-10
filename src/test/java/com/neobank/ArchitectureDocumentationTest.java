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
        var documenter = new Documenter(ApplicationModules.of(NeoBankCoreApplication.class));

        documenter.writeDocumentationAsPlantUmlTo("target/modulith-docs");
        documenter.writeComponentDiagramsAsPlantUmlTo("target/modulith-docs");
        documenter.writeModuleCanvasesTo("target/modulith-docs");
    }
}
