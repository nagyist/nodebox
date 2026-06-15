package nodebox.handle;

import nodebox.graphics.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScaleHandleTest {

    private static final double DELTA = 0.0001;

    /**
     * Minimal delegate: reports a 100% scale and captures whatever the handle sets.
     */
    private static class StubDelegate implements HandleDelegate {
        Point lastValue;

        public boolean hasInput(String portName) {
            return true;
        }

        public boolean isConnected(String portName) {
            // The edited port is driven by the handle, not a connection, so silentSet forwards.
            return false;
        }

        public Object getValue(String portName) {
            return new Point(1, 1);
        }

        public void setValue(String nodePath, String portName, Object value) {
        }

        public void silentSet(String portName, Object value) {
            lastValue = (Point) value;
        }

        public void startEdits(String command) {
        }

        public void stopEditing() {
        }

        public void updateHandle() {
        }
    }

    private Point dragBottomRightCorner(double viewScale, double worldDx, double worldDy) {
        StubDelegate delegate = new StubDelegate();
        ScaleHandle h = new ScaleHandle();
        h.setHandleDelegate(delegate);
        h.setViewTransform(0, 0, viewScale);
        // The bottom-right corner of the (screen-constant) box sits at this world position.
        double corner = (ScaleHandle.HANDLE_WIDTH / viewScale) / 2;
        h.mousePressed(new Point(corner, corner));
        h.mouseDragged(new Point(corner + worldDx, corner + worldDy));
        return delegate.lastValue;
    }

    @Test
    public void dragResponseIsIndependentOfZoom() {
        // Drag the corner by the same *on-screen* distance (10px) at two zoom levels.
        // At 1x that is 10 world units; at 2x it is 5 world units.
        Point at1x = dragBottomRightCorner(1.0, 10, 0);
        Point at2x = dragBottomRightCorner(2.0, 5, 0);
        assertEquals(at1x.x, at2x.x, DELTA);
        assertEquals(at1x.y, at2x.y, DELTA);
    }

    @Test
    public void dragConvertsWorldDeltaToScreen() {
        // A 10-unit world drag of the bottom-right corner widens the 100px box to 120px,
        // i.e. a 1.2x horizontal scale, at default zoom.
        Point scaled = dragBottomRightCorner(1.0, 10, 0);
        assertEquals(1.2, scaled.x, DELTA);
        assertEquals(1.0, scaled.y, DELTA);
    }

    @Test
    public void sameWorldDragHasLargerEffectWhenZoomedIn() {
        // The same world-space drag should scale more when zoomed in, because the box is
        // smaller on the canvas: 10 world units at 2x equals a 20px on-screen drag -> 1.4x.
        Point scaled = dragBottomRightCorner(2.0, 10, 0);
        assertEquals(1.4, scaled.x, DELTA);
    }
}
