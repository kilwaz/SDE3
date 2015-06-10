package application.utils;

import application.gui.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ThreadManager {
    private static ThreadManager threadManager;
    private CopyOnWriteArrayList<SDEThread> runningThreads;
    private Integer activeThreads = 0;

    public ThreadManager() {
        threadManager = this;
        runningThreads = new CopyOnWriteArrayList<>();
    }

    public synchronized void addThread(SDEThread thread) {
        runningThreads.add(thread);
    }

    public void closeThreads() {
        List<SDEThread> threadsToRemove = runningThreads.stream().filter(thread -> !thread.getThread().isAlive()).collect(Collectors.toList());

        runningThreads.removeAll(threadsToRemove);
    }

    public synchronized void threadStarted() {
        activeThreads++;
        if (Controller.getInstance() != null) {
            Controller.getInstance().updateThreadCount(activeThreads);
        }
    }

    public synchronized void threadFinished() {
        activeThreads--;
        if (Controller.getInstance() != null) {
            Controller.getInstance().updateThreadCount(activeThreads);
            closeThreads();
        }
    }

    public static ThreadManager getInstance() {
        return threadManager;
    }

    public synchronized Integer getActiveThreads() {
        return activeThreads;
    }
}
