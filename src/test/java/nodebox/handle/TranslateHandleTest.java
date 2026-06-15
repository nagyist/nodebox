package nodebox.handle;

import nodebox.graphics.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TranslateHandleTest {

    private static final double DELTA = 0.0001;

    /**
     * Minimal delegate: holds the translate value and captures whatever the handle sets.
     */
    private static class StubDelegate implements HandleDelegate {
        Point value = new Point(0, 0);
        Point lastValue;

        public boolean hasInput(String portName) {
            return true;
        }

        public boolean isConnected(String portName) {
            return false;
        }

        public Object getValue(String portName) {
            return value;
        }

        public void setValue(String nodePath, String portName, Object v) {
        }

        public void silentSet(String portName, Object v) {
            value = (Point) v;
            lastValue = (Point) v;
        }

        public void startEdits(String command) {
        }

        public void stopEditing() {
        }

        public void updateHandle() {
        }
    }

    // At scale 1 with no offset, document and screen coordinates coincide.
    private Point pressDrag(double pressX, double pressY, double dx, double dy) {
        StubDelegate d = new StubDelegate();
        TranslateHandle h = new TranslateHandle();
        h.setHandleDelegate(d);
        h.setViewTransform(0, 0, 1);
        if (!h.mousePressed(new Point(pressX, pressY)))
            return null;
        h.mouseDragged(new Point(pressX + dx, pressY + dy));
        return d.lastValue;
    }

    @Test
    public void grabbingAlongTheHorizontalLineConstrainsToX() {
        // Press halfway along the horizontal arrow (not at the tip) and drag diagonally.
        Point result = pressDrag(50, 0, 10, 7);
        assertEquals(10, result.getX(), DELTA);
        assertEquals(0, result.getY(), DELTA); // y is constrained
    }

    @Test
    public void grabbingAlongTheVerticalLineConstrainsToY() {
        Point result = pressDrag(0, 50, 7, 10);
        assertEquals(0, result.getX(), DELTA); // x is constrained
        assertEquals(10, result.getY(), DELTA);
    }

    @Test
    public void grabbingTheCenterMovesBothAxes() {
        Point result = pressDrag(0, 0, 10, 10);
        assertEquals(10, result.getX(), DELTA);
        assertEquals(10, result.getY(), DELTA);
    }

    @Test
    public void grabbingTheArrowTipStillWorks() {
        Point result = pressDrag(100, 0, 10, 0);
        assertEquals(10, result.getX(), DELTA);
        assertEquals(0, result.getY(), DELTA);
    }

    @Test
    public void clickingOffTheAxesDoesNotStartADrag() {
        // A point well below the horizontal line and right of the vertical line hits nothing.
        Point result = pressDrag(50, 40, 10, 10);
        assertNull(result);
    }
}
