package flarestar.junit.composite.runner.testobject;

import org.junit.internal.runners.model.ReflectiveCallable;

import java.lang.reflect.Method;

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

    public static Object invoke(final Method method, final Object... args) {
        try {
            return new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return method.invoke(currentTestInstance, args);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
