package application.test;

import java.util.HashMap;

public class LoopTracker {
    private static HashMap<String, Loop> loops = new HashMap<>();

    public static Loop getLoop(String ref) {
        return loops.get(ref);
    }

    public static void removeLoop(String ref) {
        loops.remove(ref);
    }

    public static void setLoop(String ref, Loop loop) {
        loops.put(ref, loop);
    }
}
