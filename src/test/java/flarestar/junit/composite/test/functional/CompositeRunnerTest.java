package flarestar.junit.composite.test.functional;

import flarestar.junit.composite.test.runners.ExampleTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(BlockJUnit4ClassRunner.class)
public class CompositeRunnerTest {
    @Test
    public void testSystem() throws Throwable {
        Computer computer = new Computer();
        JUnitCore jUnitCore = new JUnitCore();

        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(capture);
        TextListener listener = new TextListener(printStream);
        jUnitCore.addListener(listener);

        PrintStream systemOut = System.out;
        System.setOut(printStream);
        try {
            jUnitCore.run(computer, ExampleTest.class);
        } finally {
            System.setOut(systemOut);
        }

        String output = capture.toString("UTF-8");
        output = normalizeOutput(output);

        String expectedOutput = getResource("/CompositeTest.testSystem.expected.txt");
        Assert.assertEquals(expectedOutput, output);
    }

    private String normalizeOutput(String output) {
        output = output.replaceAll("Time:[^\n]+", "Time:");

        Matcher m = Pattern.compile("@([0-9A-Za-z]+)").matcher(output);
        if (m.find()) {
            String firstTestCaseAddress = m.group(1);
            output = output.replace(firstTestCaseAddress, "<address>");
        }

        return output;
    }

    private String getResource(String path) throws IOException, URISyntaxException {
        URL resourceUrl = getClass().getResource(path);
        if (resourceUrl == null) {
            throw new RuntimeException("Cannot find resource: " + path);
        }

        return (new Scanner(new File(resourceUrl.getFile()))).useDelimiter("\\Z").next() + "\n";
    }
}
