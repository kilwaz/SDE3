package application.utils;

import application.error.Error;
import application.utils.managers.ThreadManager;
import org.apache.log4j.Logger;

public class SDEThread {
    private static Integer threadCounter = 0;
    private static Logger log = Logger.getLogger(SDEThread.class);
    private Thread thread;
    private Runnable runnable;
    private Integer id = -1;
    private String description = "";
    private String threadReference = null;

    public SDEThread(Runnable runnable, String description, String threadReference, Boolean doNotStart) {
        threadCounter++;
        this.id = threadCounter;
        this.description = description;
        this.runnable = runnable;
        this.threadReference = threadReference;

        thread = new Thread(runnable);
        if (doNotStart) {
            start();
        }
    }

    public void start() {
        threadCounter++;
        ThreadManager.getInstance().addThread(this);
        if (threadReference != null) {
            ThreadManager.getInstance().addThreadToCollection(threadReference, this);
        }
        thread.start();
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIsRunning() {
        return thread.isAlive();
    }

    public String getState() {
        return thread.getState().name();
    }

    public String getString() {
        return this.toString();
    }

    public Thread getThread() {
        return thread;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public String getThreadReference() {
        return threadReference;
    }

    public void setThreadReference(String threadReference) {
        this.threadReference = threadReference;
    }

    public void join() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Error.SDE_JOIN_THREAD.record().create(ex);
            }
        }
    }
}
