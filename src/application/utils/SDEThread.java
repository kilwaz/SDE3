package application.utils;

public class SDEThread {
    private Thread thread;
    private Runnable runnable;

    public SDEThread(Runnable runnable) {
        this.runnable = runnable;

        thread = new Thread(runnable);
        ThreadManager.getInstance().addThread(this);
        thread.start();
    }

    public Thread getThread() {
        return thread;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
