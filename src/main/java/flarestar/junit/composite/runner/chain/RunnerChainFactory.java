package flarestar.junit.composite.runner.chain;

import flarestar.junit.composite.runner.CompositeRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 */
public class RunnerChainFactory {

    private final RunnerChainLinkFactory factory = new RunnerChainLinkFactory();

    public List<ParentRunner<?>> makeRunnerChain(Class<?> testClass, CompositeRunner compositeRunner)
            throws InitializationError {
        List<Class<? extends ParentRunner<?>>> runnerClasses = compositeRunner.getRunnerClasses();

        List<ParentRunner<?>> runners = new ArrayList<ParentRunner<?>>(runnerClasses.size());
        createRunnerChainLink(testClass, compositeRunner, runners, runnerClasses.iterator(), true);
        return runners;
    }

    private ParentRunner<?> createRunnerChainLink(Class<?> testClass, CompositeRunner compositeRunner,
                                                  List<ParentRunner<?>> runners,
                                                  Iterator<Class<? extends ParentRunner<?>>> iterator,
                                                  boolean isTestStructureProvider) throws InitializationError {
        Class<? extends ParentRunner<?>> thisRunnerClass = iterator.next();

        ParentRunner<?> nextRunner = null;
        if (iterator.hasNext()) {
            nextRunner = createRunnerChainLink(testClass, compositeRunner, runners, iterator, false);
        }

        ParentRunner<?> runner = factory.makeLink(thisRunnerClass, testClass, compositeRunner, nextRunner, isTestStructureProvider);
        runners.add(0, runner);
        return runner;
    }

    public static ParentRunner<?> makeChain(Class<?> testCaseClass, CompositeRunner runner) {
        RunnerChainFactory factory = new RunnerChainFactory();

        List<ParentRunner<?>> chain;
        try {
            chain = factory.makeRunnerChain(testCaseClass, runner);
        } catch (InitializationError initializationError) {
            throw new RuntimeException(initializationError);
        }

        return chain.get(0);
    }
}
