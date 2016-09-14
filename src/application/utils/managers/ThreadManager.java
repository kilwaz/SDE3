package application.utils.managers;

import application.gui.Controller;
import application.utils.SDEThread;
import application.utils.SDEThreadCollection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ThreadManager {
    private static ThreadManager instnace;
    private ObservableList<SDEThread> runningThreads;
    private Integer activeThreads = 0;
    private HashMap<String, SDEThreadCollection> threadCollections = new HashMap<>();

    public ThreadManager() {
        instnace = this;
        runningThreads = FXCollections.observableArrayList();
    }

    public synchronized static ThreadManager getInstance() {
        if (instnace == null) {
            instnace = new ThreadManager();
        }

        return instnace;
    }

    // Synchronized method as we are accessing runningThreads list
    public synchronized void addThread(SDEThread thread) {
        runningThreads.add(thread);
    }

    // Synchronized method as we are accessing runningThreads list
    public synchronized void removeInactiveThreads() {
        List<SDEThread> threadsToRemove = runningThreads.stream().filter(thread -> !thread.getThread().isAlive()).collect(Collectors.toList());
        threadCollections.values().forEach(SDEThreadCollection::removeInactiveThreads);

        runningThreads.removeAll(threadsToRemove);
    }

    public synchronized void addThreadToCollection(String threadReference, SDEThread sdeThread) {
        if (threadCollections.get(threadReference) == null) {
            SDEThreadCollection sdeThreadCollection = new SDEThreadCollection(threadReference);
            sdeThreadCollection.addThread(sdeThread);
            threadCollections.put(threadReference, sdeThreadCollection);
        } else {
            SDEThreadCollection sdeThreadCollection = threadCollections.get(threadReference);
            sdeThreadCollection.addThread(sdeThread);
        }
    }

    public synchronized SDEThreadCollection getThreadCollection(String threadReference) {
        return threadCollections.get(threadReference);
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
            removeInactiveThreads();
        }
    }

    public ObservableList<SDEThread> getRunningThreads() {
        return runningThreads;
    }

    public synchronized Integer getActiveThreads() {
        return activeThreads;
    }
}
