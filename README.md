# junit-composite-runner

A special JUnit Runner class that composites other Runner classes so more than one can be used to run a test.

## Caveat

JUnit is not designed for Runner classes to be composited together. In fact, it's not possible out of the box.

This library makes it possible through reflection and bytecode manipulation. **Which means it won't work in every scenario.**

Composited Runners have to extend `ParentRunner` and have to follow the contract precisely. Deviations from established behavior could have strange effects.

## Usage

To combine multiple Runners, use the `RunWith(CompositeRunner.class)` annotation and the `Runners` notation like so:

```
@RunWith(CompositeRunner.class)
@Runners(value = BlockJUnit4ClassRunner.class, others = {MyTestRunner.class})
public class MyTest {
    // ...
}
```

The first test Runner class provided to `@Runners` is considered the **test structure provider**.
This Runner determines what tests are run (eg, by looking for `@Test` annotations).

The other Runners (specified in the `others =` annotation property) will only influence how the tests
are invoked.

This means that if you use `BlockJUnit4ClassRunner` as your test structure provider and composite it
with a custom runner that looks for test methods a different way (eg, w/ a `@MyTest` annotation), the
tests found by your custom test runner **will not be invoked**.

## How it works

Runner composition is accomplished by carefully chaining runner executions.

A JUnit Runner has three different responsibilities:
- It determines what tests need to be run
- It provides logic to execute all tests at once (surrounding the execution w/ logic, like `@BeforeClass`
  /`@AfterClass` methods). This is encapsulated in the `ParentRunner.classBlock()` method which uses
  the `ParentRunner.childrenInvoker()` method.
- And it provides logic to execute a single test after creating a test instance (surrounding the execution
  w/ logic, like `@Before`/`@After` methods). This is encapsulated in the `ParentRunner.runChild()` method.

The `CompositeRunner` uses Javassist to dynamically generate subclasses of each runner used.
These subclasses override the `ParentRunner.childrenInvoker()` and `ParentRunner.runChild()` methods so
that instead of performing the normal behavior, they invoke a method in the next test Runner in the chain.

When the overriden `ParentRunner.childrenInvoker()` method is called, the subclass will call
`ParentRunner.classBlock()` in the next runner. If there is no next runner, then it calls `runChildren()` on
the first runner in the chain. This means each runner's class level setup/teardown logic will be invoked
in turn.

Likewise, the overridden `ParentRunner.runChild()` method will call `ParentRunner.runChild()` on the
next runner.* If there is no next runner, then we just let `ParentRunner.runChild()` invoke the test method.
This means each runner's setup/teardown logic will be invoked in turn.

\* It's actually a bit more complicated than this. Since runChild() will invoke the test we can intercept
the execution at the right point and go to the next runner. So instead, we create a dummy `FrameworkMethod`
instance which invokes the next runner's runChild() method, instead of the actual test method.
