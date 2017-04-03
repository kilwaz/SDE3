package application.test.action;

public interface Action {
    /**
     * This method is extended by other action classes and handles the main work of the action including interpreting
     * the {@link application.test.TestParameter} values passed in, performing the action and also constructing the
     * {@link application.test.TestResult} which contains the results of the test.
     */
    void performAction();
}
