package com.napier.sem;

/**
 * Entrypoint helper for small demos/tests.
 */
public final class Main {
    /** Prevent instantiation of utility class. */
    private Main() { }

    /**
     * Simple demo showing fixed line lengths and no magic numbers inline.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        final int demoRepeat = 5;
        for (int i = 0; i < demoRepeat; i++) {
            System.out.println("SEM demo run " + (i + 1));
        }
        System.out.println(
                "Keep main small; the real app entry is com.napier.sem.App.");
    }
}
