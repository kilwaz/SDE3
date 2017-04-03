package sde.application.utils;

import sde.application.utils.managers.ThreadManager;

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