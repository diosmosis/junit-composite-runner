package flarestar.junit.composite.runner;

import flarestar.junit.composite.annotations.Runners;
import flarestar.junit.composite.runner.chain.RunnerChainFactory;
import flarestar.junit.composite.runner.chain.RunnerChainLinkFactory;
import flarestar.junit.composite.runner.testobject.TestObjectInstanceContainer;
import flarestar.junit.composite.runner.testobject.TestObjectProxyClassFactory;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 */
public class CompositeRunner extends Runner {

    private final RunnerChainFactory chainFactory = new RunnerChainFactory();
    private final TestObjectProxyClassFactory testObjectClassFactory = new TestObjectProxyClassFactory();

    private final List<Class<? extends ParentRunner<?>>> runnerClasses;
    private final List<ParentRunner<?>> composedRunners;
    private Class<?> testClassUnprocessed;
    private Class<?> testClass;

    public CompositeRunner(Class<?> testClassUnprocessed) throws InitializationError {
        this.testClassUnprocessed = testClassUnprocessed;
        Runners annotation = testClassUnprocessed.getAnnotation(Runners.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                "The CompositeRunner runner must be used in conjunction with the @Runners annotation.");
        }

        this.testClass = testObjectClassFactory.makeProxy(testClassUnprocessed);

        runnerClasses = getComposedRunnerClasses(annotation);
        composedRunners = chainFactory.makeRunnerChain(testClass, this);
    }

    @Override
    public Description getDescription() {
        return getStructureProvidingRunner().getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        try {
            getStructureProvidingRunner().run(runNotifier);
        } finally {
            TestObjectInstanceContainer.currentTestInstance.pop();
        }
    }

    public ParentRunner<?> getStructureProvidingRunner() {
        return composedRunners.get(0);
    }

    private List<Class<? extends ParentRunner<?>>> getComposedRunnerClasses(Runners annotation) {
        List<Class<? extends ParentRunner<?>>> result = new ArrayList<Class<? extends ParentRunner<?>>>();
        result.add(annotation.value());
        Collections.addAll(result, annotation.others());
        return result;
    }

    public List<Class<? extends ParentRunner<?>>> getRunnerClasses() {
        return runnerClasses;
    }
}
