package flarestar.junit.composite.annotations;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Runners {
    Class<? extends ParentRunner<?>> value() default BlockJUnit4ClassRunner.class;
    Class<? extends ParentRunner<?>>[] others() default {};
}
