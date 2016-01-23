package flarestar.junit.composite.runner.testobject;

import flarestar.junit.composite.runner.javassist.BaseClassExtender;
import javassist.*;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Creates a javassist proxy class from a test case class (the class users create when
 * writing tests). Used to make sure no matter how many runners create test instances, that
 * only one is used for each test.
 */
public class TestObjectProxyClassFactory extends BaseClassExtender {
    public Class<?> makeProxy(Class<?> testClass) {
        String proxyClassName = testClass.getName() + "Proxy";
        try {
            return Class.forName(proxyClassName);
        } catch (ClassNotFoundException e) {
            // ignore
        }

        ClassPool pool = ClassPool.getDefault();

        CtClass proxyCtClass;
        try {
            proxyCtClass = pool.get(testClass.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e); // should never happen
        }

        CtClass newTestCtClass = pool.makeClass(proxyClassName, proxyCtClass);
        copyAnnotations(pool, testClass, newTestCtClass);

        try {
            newTestCtClass.addConstructor(makeCachingConstructor(newTestCtClass));

            for (Method method : testClass.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                String superMethodBody = makeProxyMethodSuperBody(method);
                newTestCtClass.addMethod(CtNewMethod.make(superMethodBody, newTestCtClass));

                String proxyBody = makeProxyMethodBody(method);
                newTestCtClass.addMethod(CtNewMethod.make(proxyBody, newTestCtClass));
            }

            return newTestCtClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeProxyMethodSuperBody(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append("public ");
        builder.append(method.getReturnType().getName());
        builder.append(" ");
        builder.append(method.getName());
        builder.append("Super(");

        Class<?>[] params = method.getParameterTypes();
        appendMethodParameters(builder, params);

        builder.append(") {\n");

        builder.append("    ");
        if (method.getReturnType() != Void.class) {
            builder.append("return ");
        }
        builder.append("super.");
        builder.append(method.getName());
        builder.append("(");

        for (int i = 0; i != params.length; ++i) {
            if (i != 0) {
                builder.append(", ");
            }

            builder.append("arg");
            builder.append(i);
        }

        builder.append(");\n");

        builder.append("}\n");

        return builder.toString();
    }

    private CtConstructor makeCachingConstructor(CtClass declaringClass) throws CannotCompileException {
        String constructorCode =
            "public CustomRunner() {\n" +
            "    super();\n" +
            "    flarestar.junit.composite.runner.testobject.TestObjectInstanceContainer.setCurrentTestInstance(this);\n" +
            "}\n";
        return CtNewConstructor.make(constructorCode, declaringClass);
    }

    private String makeProxyMethodBody(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append("public ");
        builder.append(method.getReturnType().getName());
        builder.append(" ");
        builder.append(method.getName());
        builder.append("(");

        Class<?>[] params = method.getParameterTypes();
        appendMethodParameters(builder, params);

        builder.append(") {\n");

        builder.append("    java.lang.reflect.Method __method = getClass().getMethod(\"");
        builder.append(method.getName());
        builder.append("Super");

        if (params.length == 0) {
            builder.append("\", new Class[0]);\n");
        } else {
            builder.append("\", new Class[]{");
            for (int i = 0; i != params.length; ++i) {
                if (i != 0) {
                    builder.append(", ");
                }

                builder.append(params[i].getName());
                builder.append(".class");
            }
            builder.append("});\n");
        }

        builder.append("    ");
        if (method.getReturnType() != Void.class) {
            builder.append("return ");
        }
        builder.append("flarestar.junit.composite.runner.testobject.TestObjectInstanceContainer.invoke(");
        builder.append("__method, ");
        if (params.length == 0) {
            builder.append("new Object[0]);\n");
        } else {
            for (int i = 0; i != params.length; ++i) {
                if (i != 0) {
                    builder.append(", ");
                }

                builder.append("arg");
                builder.append(i);
            }
            builder.append(");\n");
        }

        builder.append("}\n");

        return builder.toString();
    }

    private void appendMethodParameters(StringBuilder builder, Class<?>[] params) {
        for (int i = 0; i != params.length; ++i) {
            if (i != 0) {
                builder.append(", ");
            }

            builder.append(params[i].getName());
            builder.append(" arg");
            builder.append(i);
        }
    }
}

