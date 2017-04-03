package application.test.action.helpers;

import java.util.HashMap;

public class LoopTracker {
    private HashMap<String, Loop> loops = new HashMap<>();

    public Loop getLoop(String ref) {
        return loops.get(ref);
    }

    public void removeLoop(String ref) {
        loops.remove(ref);
    }

    public void setLoop(String ref, Loop loop) {
        loops.put(ref, loop);
    }
}
