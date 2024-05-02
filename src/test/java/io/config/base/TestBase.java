package io.config.base;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

@ActiveProfiles("test")
@TestConstructor(autowireMode = AutowireMode.ALL)
public abstract class TestBase {
}
