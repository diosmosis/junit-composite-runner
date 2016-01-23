package flarestar.junit.composite.runner.chain;

import flarestar.junit.composite.runner.CompositeRunner;
import javassist.*;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * TODO
 */
public class RunnerChainLinkFactory {
    public ParentRunner<?> makeLink(Class<? extends ParentRunner<?>> runnerClass, final Class<?> testClass,
                                    final CompositeRunner compositeRunner, final ParentRunner<?> nextRunner,
                                    boolean isTestStructureProvider) {
        ClassPool pool = ClassPool.getDefault();

        String newClassName = runnerClass.getName() + (isTestStructureProvider ? "TestStructureProvider" : "ChainLink");
        final Class<?> newRunnerCtClass = makeLinkClass(pool, newClassName, runnerClass, isTestStructureProvider);

        try {
            return (ParentRunner<?>) new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return newRunnerCtClass.getConstructor(Class.class, CompositeRunner.class, ParentRunner.class)
                        .newInstance(testClass, compositeRunner, nextRunner);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Class<?> makeLinkClass(ClassPool pool, String newClassName, Class<? extends ParentRunner<?>> runnerClass,
                                   boolean isTestStructureProvider) {
        // if the class already exists, don't try to create it again
        try {
            return Class.forName(newClassName);
        } catch (ClassNotFoundException e) {
            // ignore
        }

        CtClass runnerCtClass;
        try {
            runnerCtClass = pool.get(runnerClass.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e); // should never happen
        }

        CtClass newRunnerCtClass = pool.makeClass(newClassName, runnerCtClass);

        try {
            newRunnerCtClass.addField(makeNextRunnerField(newRunnerCtClass));
            newRunnerCtClass.addField(makeFirstRunnerField(newRunnerCtClass));
            newRunnerCtClass.addConstructor(makeNewClassConstructor(newRunnerCtClass));
            newRunnerCtClass.addMethod(makeChildrenInvokerMethod(newRunnerCtClass));
            newRunnerCtClass.addMethod(makeRunChildMethod(newRunnerCtClass));
            if (!isTestStructureProvider) {
                newRunnerCtClass.addMethod(makeGetChildrenMethod(newRunnerCtClass));
                newRunnerCtClass.addMethod(makeDescribeChildMethod(runnerClass, newRunnerCtClass));
            }
            return newRunnerCtClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException(e); // should never happen
        }
    }

    private CtMethod makeDescribeChildMethod(Class<? extends ParentRunner<?>> runnerClass, CtClass declaringClass) throws CannotCompileException {
        String parameterType = getDescribeChildParameterType(runnerClass).getName();

        String describeChild =
            "protected org.junit.runner.Description describeChild(" + parameterType + " child) {\n" +
            "    return flarestar.junit.composite.runner.chain.RunnerChainLinkFactory.invokeDescribeChild(compositeRunner.getStructureProvidingRunner(), child);\n" +
            "}";
        return CtNewMethod.make(describeChild, declaringClass);
    }

    private Class<?> getDescribeChildParameterType(Class<? extends ParentRunner<?>> runnerClass) {
        while (runnerClass.getSuperclass() != ParentRunner.class) {
            runnerClass = (Class<? extends ParentRunner<?>>)runnerClass.getSuperclass();
        }

        return (Class<?>) ((ParameterizedType)runnerClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private CtMethod makeGetChildrenMethod(CtClass declaringClass) throws CannotCompileException {
        String getChildren =
            "protected java.util.List getChildren() {\n" +
            "    return flarestar.junit.composite.runner.chain.RunnerChainLinkFactory.invokeGetChildren(compositeRunner.getStructureProvidingRunner());\n" +
            "}";
        return CtNewMethod.make(getChildren, declaringClass);
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
            "        if (nextRunner != null) {\n" +
            "            if (child.getClass() == org.junit.runners.model.FrameworkMethod.class) {\n" +
            "                child = new flarestar.junit.composite.runner.chain.FrameworkMethodChainLink((org.junit.runners.model.FrameworkMethod)child, notifier, nextRunner);\n" +
            "            }\n" +
            "            notifier = new flarestar.junit.composite.runner.notifier.NullRunNotifier(notifier);\n" +
            "        }\n" +
            "\n" +
            "        super.runChild(child, notifier);\n" +
            "    }";
        return CtNewMethod.make(runChild, newRunnerCtClass);
    }

    // utility methods
    public static Statement invokeClassBlock(final ParentRunner<?> nextRunner, final RunNotifier notifier) {
        try {
            return (Statement)new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    Method classBlockMethod = ParentRunner.class.getDeclaredMethod("classBlock", RunNotifier.class);
                    classBlockMethod.setAccessible(true);
                    return classBlockMethod.invoke(nextRunner, notifier);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static List invokeGetChildren(final ParentRunner<?> runner) {
        try {
            return (List)new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    Method getChildrenMethod = ParentRunner.class.getDeclaredMethod("getChildren");
                    getChildrenMethod.setAccessible(true);
                    return getChildrenMethod.invoke(runner);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Description invokeDescribeChild(final ParentRunner<?> runner, final Object child) {
        try {
            return (Description) new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    Method describeChildMethod = ParentRunner.class.getDeclaredMethod("describeChild", Object.class);
                    describeChildMethod.setAccessible(true);
                    return describeChildMethod.invoke(runner, child);
                }
            }.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
