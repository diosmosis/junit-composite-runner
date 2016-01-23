package flarestar.junit.composite.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Dummy {
    String value() default "";
    int value2() default -1;
}
