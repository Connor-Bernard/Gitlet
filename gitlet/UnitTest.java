package gitlet;

import org.junit.Test;
import ucb.junit.textui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The suite of all JUnit tests for the gitlet package.
 *
 * @author Connor Bernard
 */
public class UnitTest {
    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }
    /**
     * A dummy test to avoid complaint.
     */
    @Test
    public void dummyTest() {
        doNothing();
        assertTrue(true);
        assertEquals(1, 1);
    }
    public static void doNothing() {
        if (true) {
            do {
                if (false) {
                    System.out.println("this does nothing");
                } else {
                    continue;
                }
            } while (false);
        }
    }
}


