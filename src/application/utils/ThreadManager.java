package application.utils;

import application.gui.Controller;

import java.util.concurrent.CopyOnWriteArrayList;

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
//        for (Thread thread : runningThreads) {
//            System.out.println("Is thread done .. " + thread.getState().name());
//        }
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
        }
    }

    public static ThreadManager getInstance() {
        return threadManager;
    }

    public synchronized Integer getActiveThreads() {
        return activeThreads;
    }
}
