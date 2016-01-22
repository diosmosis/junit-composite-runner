package flarestar.junit.composite.test.runners;

import flarestar.junit.composite.annotations.Runners;
import flarestar.junit.composite.runner.CompositeRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(CompositeRunner.class)
@Runners(value = BlockJUnit4ClassRunner.class, others = {TestRunner.class})
public class ExampleTest {
    private int value = 0;

    @Before
    public void thisBefore() {
        System.out.println("in thisBefore with " + this.toString() + " [value = " + value + "]");

        ++value;
    }

    @Before
    public void thatBefore() {
        System.out.println("in thatBefore with " + this.toString() + " [value = " + value + "]");

        ++value;
    }

    @After
    public void thisAfter() {
        System.out.println("in thisAfter with " + this.toString() + " [value = " + value + "]");
    }

    @Test
    public void testSomething() {
        System.out.println("in testSomething with " + this.toString() + " [value = " + value + "]");
    }

    @Test
    public void testSomethingElse() {
        System.out.println("in testSomethingElse with " + this.toString() + " [value = " + value + "]");
    }
}
