package sde.application.utils;

import sde.application.error.Error;
import org.apache.log4j.Logger;

public class SDEThread {
    public static Integer threadCounter = 0;
    private static Logger log = Logger.getLogger(SDEThread.class);
    private Thread thread;
    private Runnable runnable;
    private Integer id = -1;
    private String description = "";
    private String threadReference = null;

    public SDEThread(Runnable runnable, String description, String threadReference, Boolean startNow) {
        threadCounter++;
        this.id = threadCounter;
        this.description = description;
        this.runnable = runnable;
        this.threadReference = threadReference;

        thread = new Thread(runnable);
        if (startNow) {
            start();
        }
    }

    public void start() {
        // Create a new thread to setup this thread
        SDEThreadSetup sdeThreadSetup = new SDEThreadSetup(this);
        Thread startupThread = new Thread(sdeThreadSetup);
        startupThread.start();

        // Make sure all the setup is done before we start the actual SDEThread
        try {
            startupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
