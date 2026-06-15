package nodebox.handle;

import nodebox.graphics.GraphicsContext;
import nodebox.graphics.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractHandleTest {

    /**
     * A minimal handle that exposes the protected screen-space helpers for testing.
     */
    private static class TestHandle extends AbstractHandle {
        public void draw(GraphicsContext ctx) {
        }

        public Point screen(double x, double y) {
            return toScreen(x, y);
        }

        public boolean hits(double worldX, double worldY, Point worldMouse) {
            return hitsHandle(worldX, worldY, worldMouse);
        }
    }

    private static final double DELTA = 0.0001;

    @Test
    public void projectsDocumentPointToScreen() {
        TestHandle h = new TestHandle();
        h.setViewTransform(10, 20, 2);
        Point s = h.screen(5, 5);
        assertEquals(10 + 2 * 5, s.getX(), DELTA);
        assertEquals(20 + 2 * 5, s.getY(), DELTA);
    }

    @Test
    public void grabAreaIsConstantOnScreenWhenZoomedIn() {
        // At 2x zoom the 6px grab area only reaches 3 document units (= 3 screen px each side).
        TestHandle h = new TestHandle();
        h.setViewTransform(0, 0, 2);
        assertTrue("1 unit away is 2px on screen, inside the grab area", h.hits(0, 0, new Point(1, 0)));
        assertFalse("2 units away is 4px on screen, outside the grab area", h.hits(0, 0, new Point(2, 0)));
    }

    @Test
    public void grabAreaIsConstantOnScreenWhenZoomedOut() {
        // At 0.5x zoom the same 6px grab area reaches 6 document units, so a far world point hits.
        TestHandle h = new TestHandle();
        h.setViewTransform(0, 0, 0.5);
        assertTrue("5 units away is 2.5px on screen, inside the grab area", h.hits(0, 0, new Point(5, 0)));
        assertFalse("8 units away is 4px on screen, outside the grab area", h.hits(0, 0, new Point(8, 0)));
    }

    @Test
    public void grabAreaIsUnaffectedByViewOffset() {
        // A pan shifts both the handle and the cursor, so the hit result depends only on their
        // relative on-screen distance.
        TestHandle h = new TestHandle();
        h.setViewTransform(123, -45, 1);
        assertTrue(h.hits(10, 10, new Point(12, 10)));   // 2px away
        assertFalse(h.hits(10, 10, new Point(20, 10)));  // 10px away
    }
}
