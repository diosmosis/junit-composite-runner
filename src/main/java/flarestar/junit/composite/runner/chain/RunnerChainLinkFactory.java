package flarestar.junit.composite.runner.chain;

import flarestar.junit.composite.runner.CompositeRunner;
import javassist.*;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class RunnerChainLinkFactory {
    public ParentRunner<?> makeLink(Class<? extends ParentRunner<?>> runnerClass, final Class<?> testClass,
                                    final CompositeRunner compositeRunner, final ParentRunner<?> nextRunner) {
        ClassPool pool = ClassPool.getDefault();

        CtClass runnerCtClass;
        try {
            runnerCtClass = pool.get(runnerClass.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e); // should never happen
        }

        CtClass newRunnerCtClass = pool.makeClass(runnerClass.getName() + "ChainLink", runnerCtClass);

        try {
            newRunnerCtClass.addField(makeNextRunnerField(newRunnerCtClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

        try {
            newRunnerCtClass.addField(makeFirstRunnerField(newRunnerCtClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

        try {
            newRunnerCtClass.addConstructor(makeNewClassConstructor(newRunnerCtClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException(e); // should never happen
        }

        try {
            newRunnerCtClass.addMethod(makeChildrenInvokerMethod(newRunnerCtClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException(e); // should never happen
        }

        try {
            newRunnerCtClass.addMethod(makeRunChildMethod(newRunnerCtClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException(e); // should never happen
        }

        final Class createdJavaClass;
        try {
            createdJavaClass = newRunnerCtClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException(e); // should never happen
        }

        try {
            return (ParentRunner<?>) new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createdJavaClass.getConstructor(Class.class, CompositeRunner.class, ParentRunner.class)
                        .newInstance(testClass, compositeRunner, nextRunner);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private CtField makeNextRunnerField(CtClass declaringClass) throws CannotCompileException {
        return CtField.make("private org.junit.runners.ParentRunner nextRunner;", declaringClass);
    }

    private CtField makeFirstRunnerField(CtClass declaringClass) throws CannotCompileException {
        return CtField.make("private flarestar.junit.composite.runner.CompositeRunner compositeRunner;", declaringClass);
    }

    private CtMethod makeChildrenInvokerMethod(CtClass declaringClass) throws CannotCompileException {
        String childrenInvoker =
            "    protected org.junit.runners.model.Statement childrenInvoker(org.junit.runner.notification.RunNotifier notifier) {\n" +
            "        if (nextRunner == null) {\n" +
            "            return new flarestar.junit.composite.runner.statement.RunChildren(\n" +
            "                compositeRunner.getStructureProvidingRunner(), notifier);\n" +
            "        } else {\n" +
            "            return flarestar.junit.composite.runner.chain.RunnerChainLinkFactory.invokeClassBlock(nextRunner, notifier);\n" +
            "        }\n" +
            "    }";
        return CtNewMethod.make(childrenInvoker, declaringClass);
    }

    private CtConstructor makeNewClassConstructor(CtClass declaringClass) throws CannotCompileException {
        String constructor = "public CustomRunner(Class testClass, flarestar.junit.composite.runner.CompositeRunner compositeRunner, org.junit.runners.ParentRunner nextRunner) {\n" +
            "        super(testClass);\n" +
            "        this.compositeRunner = compositeRunner;\n" +
            "        this.nextRunner = nextRunner;\n" +
            "    }";
        return CtNewConstructor.make(constructor, declaringClass);
    }

    private CtMethod makeRunChildMethod(CtClass newRunnerCtClass) throws CannotCompileException {
        String runChild =
            "    protected void runChild(Object child, org.junit.runner.notification.RunNotifier notifier) {\n" +
            "        if (child.getClass() == org.junit.runners.model.FrameworkMethod.class && nextRunner != null) {\n" +
            "            child = new flarestar.junit.composite.runner.chain.FrameworkMethodChainLink((org.junit.runners.model.FrameworkMethod)child, notifier, nextRunner);\n" +
            "        }\n" +
            "\n" +
            "        super.runChild(child, notifier);\n" +
            "    }";
        return CtNewMethod.make(runChild, newRunnerCtClass);
    }

    // utility methods
    public static Statement invokeClassBlock(ParentRunner<?> nextRunner, RunNotifier notifier) {
        try {
            Method classBlockMethod = ParentRunner.class.getDeclaredMethod("classBlock", RunNotifier.class);
            classBlockMethod.setAccessible(true);
            return (Statement)classBlockMethod.invoke(nextRunner, notifier);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e); // should never happen
        }
    }
}
