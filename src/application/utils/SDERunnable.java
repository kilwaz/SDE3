package application.utils;

public class SDERunnable implements Runnable {

    @Override
    public void run() {
        ThreadManager.getInstance().threadStarted();
        threadRun();
        ThreadManager.getInstance().threadFinished();
    }

    public void threadRun() {
        // This should be overridden
    }
}