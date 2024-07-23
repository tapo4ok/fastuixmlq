package mdk.fastxmlmenu;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Profiling {
    public static final Logger LOGGER = Logger.getLogger("Profiling");
    private static Map<String, Long> startTimes = new HashMap<>();
    private static Map<String, Long> elapsedTimes = new HashMap<>();

    public static void start(String section) {
        startTimes.put(section, System.nanoTime());
    }

    public static void end(String section) {
        if (!startTimes.containsKey(section)) {
            throw new IllegalArgumentException("Section " + section + " was not started.");
        }
        elapsedTimes.put(section, System.nanoTime() - startTimes.get(section));
        startTimes.remove(section);
    }

    public static long getElapsedTime(String section) {
        if (!elapsedTimes.containsKey(section)) {
            throw new IllegalArgumentException("Section " + section + " has no recorded elapsed time.");
        }
        return elapsedTimes.get(section);
    }

    public static void printProfilingResults() {
        for (Map.Entry<String, Long> entry : elapsedTimes.entrySet()) {
            LOGGER.log(Level.INFO, "Section " + entry.getKey() + ": " + TimeUnit.NANOSECONDS.toMillis(entry.getValue()) + " ms");
        }
    }

    public static void printProfilingResult(String section) {
        if (!elapsedTimes.containsKey(section)) {
            throw new IllegalArgumentException("Section " + section + " has no recorded elapsed time.");
        }

    }

    public static void clear() {
        elapsedTimes.clear();
    }
}