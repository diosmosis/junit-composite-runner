package flarestar.junit.composite.runner.testobject;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class TestObjectProxyClassFactory {
    public Class<?> makeProxy(Class<?> testClass) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(testClass);
        factory.setHandler(new MethodHandler() {
            public Object invoke(Object proxy, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                return proceed.invoke(TestObjectInstanceContainer.currentTestInstance, args);
            }
        });
        return factory.createClass();
    }
}
