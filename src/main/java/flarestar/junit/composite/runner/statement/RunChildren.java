package flarestar.junit.composite.runner.statement;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class RunChildren extends Statement {
    private ParentRunner<?> runner;
    private RunNotifier notifier;

    public RunChildren(ParentRunner<?> runner, RunNotifier notifier) {
        this.runner = runner;
        this.notifier = notifier;
    }

    @Override
    public void evaluate() throws Throwable {
        final Method runChildrenMethod = ParentRunner.class.getDeclaredMethod("runChildren", RunNotifier.class);
        runChildrenMethod.setAccessible(true);

        new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                return runChildrenMethod.invoke(runner, notifier);
            }
        }.run();
    }
}
