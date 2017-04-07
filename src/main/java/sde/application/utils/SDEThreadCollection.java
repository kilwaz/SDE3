package sde.application.utils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SDEThreadCollection {
    private static Logger log = Logger.getLogger(SDEThreadCollection.class);
    private List<SDEThread> threads = new ArrayList<>();
    private String reference = "";

    public SDEThreadCollection(String reference) {
        this.reference = reference;
    }

    public synchronized void addThread(SDEThread sdeThread) {
//        log.info("Adding thread to collection " + reference);
        threads.add(sdeThread);
    }

    public List<SDEThread> getThreads() {
        return threads;
    }

    public synchronized void removeInactiveThreads() {
        List<SDEThread> threadsToRemove = threads.stream().filter(thread -> !thread.getThread().isAlive()).collect(Collectors.toList());
        threads.removeAll(threadsToRemove);
    }

    // Join this collection and wait for all threads to finish
    public void join() {
        List<SDEThread> threadCopy = new ArrayList<>();
        threadCopy.addAll(threads);

        threadCopy.forEach(SDEThread::join);
    }
}
