package application.utils;

import application.utils.managers.ThreadManager;

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