package flarestar.junit.composite.runner.testobject;

import org.junit.internal.runners.model.ReflectiveCallable;

/**
 * TODO
 */
public class TestObjectInstanceContainer {
    public static Object currentTestInstance = null;

    public static void setCurrentInstance(final Class<?> testObjectClass) {
        try {
            currentTestInstance = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return testObjectClass.newInstance();
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
