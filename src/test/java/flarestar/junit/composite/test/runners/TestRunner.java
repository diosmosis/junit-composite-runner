package flarestar.junit.composite.test.runners;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class TestRunner extends ParentRunner<FrameworkMethod> {
    protected TestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        throw new RuntimeException("TestRunner.getChildren() should not get called!");
    }

    @Override
    protected Description describeChild(FrameworkMethod frameworkMethod) {
        throw new RuntimeException("TestRunner.describeChild() should not get called!");
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        System.out.println("in TestRunner.runChild");

        Description description = this.describeChild(method);
        runLeaf(methodBlock(method), description, notifier);
    }

    private Statement methodBlock(FrameworkMethod method) {
        Object testObject;
        try {
            testObject = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return getTestClass().getOnlyConstructor().newInstance(new Object[0]);
                }
            }.run();
        } catch (Throwable throwable) {
            return new Fail(throwable);
        }

        return new InvokeMethod(method, testObject);
    }
}
