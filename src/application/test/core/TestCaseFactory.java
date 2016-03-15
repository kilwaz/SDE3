package application.test.core;


import application.error.Error;

public class TestCaseFactory {
    public static <TestClass extends TestCase> TestCase<TestClass> create(Class<TestClass> testClass) {
        try {
            return testClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Error.CREATE_NEW_INSTANCE_ERROR.record().create(ex);
        }

        return null;
    }
}
