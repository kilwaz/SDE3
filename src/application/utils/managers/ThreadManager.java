package application.utils.managers;

import application.gui.Controller;
import application.utils.SDEThread;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class ThreadManager {
    private static ThreadManager threadManager;
    private ObservableList<SDEThread> runningThreads;
    private Integer activeThreads = 0;

    public ThreadManager() {
        threadManager = this;
        runningThreads = FXCollections.observableArrayList();
    }

    // Synchronized method as we are accessing runningThreads list
    public synchronized void addThread(SDEThread thread) {
        runningThreads.add(thread);
    }

    // Synchronized method as we are accessing runningThreads list
    public synchronized void closeThreads() {
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

    public ObservableList<SDEThread> getRunningThreads() {
        return runningThreads;
    }

    public static ThreadManager getInstance() {
        if (threadManager == null) {
            threadManager = new ThreadManager();
        }
        return threadManager;
    }

    public synchronized Integer getActiveThreads() {
        return activeThreads;
    }
}
