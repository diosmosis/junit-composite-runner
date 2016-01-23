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
 *
 * ok, so how do we implement this? we need to make sure run() nests run calls for each runner
 *
 * normal runner workflow is this:
 *   run classBlock() (runs before/after class stuff)
 *   run runChildren() (nested in classBlock())
 *   run runChild() (which will deal w/ test rules & whatever else)
 *   run runLeaf() (w/ actual statement) although mine doesn't do this all the time...
 *
 * how would we compose bdd runner + robolectric?
 *  -> we'd run run() on the first one (BddRunner)
 *     -> this will run the classBlock() on the first one which should eventually call runChildren()
 *  -> at this point, we don't want to run the child tests, we want to run the next runner's classBlock()
 *     -> the next runner's classBlock will call runChildren()
 *  -> at this point we want to move on to runChildren().
 *     -> what if both runners have different child tests? eg, what if BDD creates some methods, while the other one has other methods,
 *        how do I run all of them? and do I run some w/ one runner & some w/ another?
 *
 *        can't do this.
 *     -> runChildren() will only use the first runner
 *  -> the first runner's runChildren() will loop through each child and invoke runChild()
 *  -> runChild() will invoke runLeaf() for each leaf
 *  -> runLeaf() will invoke the statement which will eventually run the FrameworkMethod.
 *     * we need to create a proxy FrameworkMethod that will chain to the next runner's runChild()
 *  -> at the end, the proxy FrameworkMethod will just invoke the composited ParentRunner's runLeaf()
 *
 * so our TODO is:
 * - how do we ensure only one object is created per instance?
 *
 * TODO: create issue on junit issue tracker about this
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
