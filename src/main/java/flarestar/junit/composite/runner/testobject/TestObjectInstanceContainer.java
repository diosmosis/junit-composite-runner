package flarestar.junit.composite.runner.testobject;

/**
 * TODO
 */
public class TestObjectInstanceContainer {
    public static Object currentTestInstance = null;

    public static void setCurrentInstance(Class<?> testObjectClass) {
        try {
            currentTestInstance = testObjectClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
