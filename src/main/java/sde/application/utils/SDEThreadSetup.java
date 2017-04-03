package sde.application.utils;

import sde.application.utils.managers.ThreadManager;
import org.apache.log4j.Logger;

public class SDEThreadSetup implements Runnable {
    private SDEThread sdeThread;
    private static Logger log = Logger.getLogger(SDEThreadSetup.class);

    public SDEThreadSetup(SDEThread sdeThread) {
        this.sdeThread = sdeThread;
    }

    @Override
    public void run() {
        SDEThread.threadCounter++;
        ThreadManager.getInstance().addThread(sdeThread);
        if (sdeThread.getThreadReference() != null) {
            ThreadManager.getInstance().addThreadToCollection(sdeThread.getThreadReference(), sdeThread);
        }
    }
}
