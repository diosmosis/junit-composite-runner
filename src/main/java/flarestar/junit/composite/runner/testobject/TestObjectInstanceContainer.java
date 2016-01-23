package flarestar.junit.composite.runner.testobject;

import org.junit.internal.runners.model.ReflectiveCallable;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * TODO
 */
public class TestObjectInstanceContainer {
    public static final Stack<Object> currentTestInstance = new Stack<Object>();

    public static void setCurrentTestInstance(final Object instance) {
        if (currentTestInstance.empty() || currentTestInstance.peek().getClass() != instance.getClass()) {
            currentTestInstance.push(instance);
        }
    }

    public static Object invoke(final Method method, final Object... args) {
        try {
            return new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return method.invoke(currentTestInstance.peek(), args);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
