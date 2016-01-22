package flarestar.junit.composite.runner.chain;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class FrameworkMethodChainLink extends FrameworkMethod {
    private FrameworkMethod wrapped;
    private RunNotifier runNotifier;
    private ParentRunner<?> nextRunner;
    private Method nextRunnerRunChild;

    public FrameworkMethodChainLink(FrameworkMethod wrapped, RunNotifier runNotifier, ParentRunner<?> nextRunner) {
        super(wrapped.getMethod());

        this.wrapped = wrapped;
        this.runNotifier = runNotifier;
        this.nextRunner = nextRunner;

        try {
            this.nextRunnerRunChild = nextRunner.getClass().getDeclaredMethod("runChild", Object.class, RunNotifier.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find runChild method in ParentRunner.", e);
        }

        this.nextRunnerRunChild.setAccessible(true);
    }

    @Override
    public Object invokeExplosively(Object target, Object... params) throws Throwable {
        // TODO: ok to ignore parameters here? i guess it depends on the Runner if they are used.
        return new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                return nextRunnerRunChild.invoke(nextRunner, wrapped, runNotifier);
            }
        }.run();
    }
}
