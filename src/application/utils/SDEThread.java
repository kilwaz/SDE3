package application.utils;

import application.utils.managers.ThreadManager;

public class SDEThread {
    private Thread thread;
    private Runnable runnable;
    private Integer id = -1;
    private static Integer threadCounter = 0;
    private String description = "";

    public SDEThread(Runnable runnable, String description) {
        threadCounter++;
        this.id = threadCounter;
        this.description = description;
        this.runnable = runnable;

        thread = new Thread(runnable);
        ThreadManager.getInstance().addThread(this);
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

    public String getString() {
        return this.toString();
    }

    public Thread getThread() {
        return thread;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
