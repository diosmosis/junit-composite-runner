package flarestar.junit.composite.test.runners;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class AnotherTestRunner extends BlockJUnit4ClassRunner {
    public AnotherTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        throw new RuntimeException("AnotherTestRunner.getChildren() should not get called!");
    }

    @Override
    protected Description describeChild(FrameworkMethod frameworkMethod) {
        throw new RuntimeException("AnotherTestRunner.describeChild() should not get called!");
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        System.out.println("in AnotherTestRunner.runChild");

        super.runChild(method, notifier);
    }
}
