package com.neobank.onboarding.web;

import com.neobank.onboarding.OnboardingTestConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation for WebMvc tests in the onboarding module.
 * Uses @WithMockUser for simpler authentication mocking.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
    controllers = OnboardingController.class,
    properties = {
        "spring.security.filter.order=0",
        "logging.level.org.springframework.security=DEBUG"
    }
)
@Import({OnboardingTestConfig.class, OnboardingControllerWebMvcTest.TestSecurityConfig.class})
@WithMockUser(username = "test-user", roles = {"CUSTOMER_RETAIL"})
@interface OnboardingWebMvcTest {
}
