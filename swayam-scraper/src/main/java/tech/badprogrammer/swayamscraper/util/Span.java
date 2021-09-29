package tech.badprogrammer.swayamscraper.util;

import lombok.NonNull;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * Auto-closeable object measuring time intervals, in milliseconds.
 */
public final class Span implements AutoCloseable {

    private final String                   name;
    private final BiConsumer<String, Long> conclusion;
    private final long                     since;

    private Span(String name, BiConsumer<String, Long> conclusion) {
        this.name       = name;
        this.conclusion = conclusion;
        this.since      = System.currentTimeMillis();
    }

    @Override
    public void close() {
        final long now   = System.currentTimeMillis();
        final long delta = now - since;
        conclusion.accept(name, delta);
    }

    public static Span of(@NonNull String name, @NonNull Logger logger) {
        final Span result = new Span(name, (n, d) -> logger.warn("Time span '{}' = {} ms", n, d));
        return result;
    }
}
