package flarestar.junit.composite.runner.notifier;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

/**
 * TODO
 */
public class NullRunNotifier extends RunNotifier {
    private RunNotifier wrapped;

    public NullRunNotifier(RunNotifier wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void addListener(RunListener listener) {
        wrapped.addListener(listener);
    }

    @Override
    public void removeListener(RunListener listener) {
        wrapped.removeListener(listener);
    }

    @Override
    public void fireTestRunStarted(Description description) {
        // empty
    }

    @Override
    public void fireTestRunFinished(Result result) {
        // empty
    }

    @Override
    public void fireTestStarted(Description description) throws StoppedByUserException {
        // empty
    }

    @Override
    public void fireTestFailure(Failure failure) {
        // empty
    }

    @Override
    public void fireTestAssumptionFailed(Failure failure) {
        // empty
    }

    @Override
    public void fireTestIgnored(Description description) {
        // empty
    }

    @Override
    public void fireTestFinished(Description description) {
        // empty
    }

    @Override
    public void pleaseStop() {
        wrapped.pleaseStop();
    }

    @Override
    public void addFirstListener(RunListener listener) {
        wrapped.addFirstListener(listener);
    }
}
