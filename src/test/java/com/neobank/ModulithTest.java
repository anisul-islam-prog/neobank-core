package com.neobank;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithTest {

    @Test
    void verifyModularArchitecture() {
        ApplicationModules modules = ApplicationModules.of(NeoBankCoreApplication.class);
        modules.verify();
    }
}
