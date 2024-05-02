package io.config.base;

import jakarta.annotation.Resource;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.util.function.Supplier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public abstract class JpaTestBase extends TestBase {

    @Resource
    protected EntityManager entityManager;

    private void clear() {
        entityManager.clear();
    }

    private void flush() {
        entityManager.flush();
    }

    protected void flushAndClear() {
        flush();
        clear();
    }

    protected void executeWithFlushAndClear(Runnable runnable) {
        try {
            runnable.run();
        } finally {
            flushAndClear();
        }
    }

    protected <T extends Entity> T executeWithFlushAndClear(Supplier<T> supplier) {
        try {
            return supplier.get();
        } finally {
            flushAndClear();
        }
    }

    protected static void printLine() {
        System.out.println("==============================================");
    }
}
