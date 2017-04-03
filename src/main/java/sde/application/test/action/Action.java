package sde.application.test.action;

import sde.application.test.TestParameter;

public interface Action {
    /**
     * This method is extended by other action classes and handles the main work of the action including interpreting
     * the {@link TestParameter} values passed in, performing the action and also constructing the
     * {@link application.test.TestResult} which contains the results of the test.
     */
    void performAction();
}
