package application.utils;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager {
    private static ThreadManager threadManager;
    private List<Thread> runningThreads;

    public ThreadManager() {
        threadManager = this;
        runningThreads = new ArrayList<Thread>();
    }

    public void addThread(Thread thread) {
        runningThreads.add(thread);
    }

    public void closeThreads() {
//        for (Thread thread : runningThreads) {
//            System.out.println("Is thread done .. " + thread.getState().name());
//        }
    }

    public static ThreadManager getInstance() {
        return threadManager;
    }
}
