package nodebox.handle;

import nodebox.graphics.GraphicsContext;
import nodebox.graphics.Point;
import nodebox.util.Geometry;

public class CircleScaleHandle extends AbstractHandle {

    public enum Mode {
        RADIUS, DIAMETER
    }

    private boolean dragging = false;
    private String radiusName;
    private String positionName = null;
    private Mode mode;
    private Point pt = null;

    public CircleScaleHandle() {
        this("radius", Mode.RADIUS);
    }

    public CircleScaleHandle(String radiusName) {
        this(radiusName, Mode.RADIUS);
    }

    public CircleScaleHandle(Mode mode) {
        this("radius", mode);
    }

    public CircleScaleHandle(String radiusName, Mode mode) {
        this(radiusName, mode, null);
    }

    public CircleScaleHandle(String radiusName, Mode mode, String positionName) {
        this.radiusName = radiusName;
        this.mode = mode;
        this.positionName = positionName;
    }

    private Point getCenter() {
        if (positionName != null)
            return (Point) getValue(positionName);
        else
            return Point.ZERO;
    }

    private double getRadius() {
        double val = (Double) getValue(radiusName);
        if (mode == Mode.DIAMETER)
            return Math.abs(val / 2);
        return Math.abs(val);
    }

    public void draw(GraphicsContext ctx) {
        Point center = toScreen(getCenter());
        // The circle is a real document measurement (the shape's radius), so it scales with the
        // view; the grab dot is screen-sized.
        double radius = getRadius() * viewScale;
        ctx.nofill();
        ctx.ellipsemode(GraphicsContext.EllipseMode.CENTER);
        ctx.stroke(HANDLE_COLOR);
        ctx.ellipse(center.x, center.y, radius * 2, radius * 2);
        if (pt != null) {
            Point sp = toScreen(pt);
            drawDot(ctx, sp.x, sp.y);
        }
    }

    @Override
    public boolean mousePressed(Point pt) {
        this.pt = null;
        // Grab the circle edge within a constant screen distance, so it stays grabbable at any zoom.
        double radius = getRadius() * viewScale;
        Point center = toScreen(getCenter());
        Point sm = toScreen(pt);
        float d = (float) Geometry.distance(center.x, center.y, sm.x, sm.y);
        dragging = (radius - 4 <= d && d <= radius + 4);
        return dragging;
    }

    @Override
    public boolean mouseDragged(Point pt) {
        if (!dragging) return false;
        Point center = getCenter();
        float newSize = (float) Geometry.distance(center.x, center.y, pt.x, pt.y);
        if (mode == Mode.DIAMETER)
            newSize *= 2;
        if (newSize == getRadius()) return false;
        silentSet(radiusName, newSize);
        this.pt = pt;
        return true;
    }

    @Override
    public boolean mouseReleased(Point pt) {
        if (!dragging) return false;
        dragging = false;
        this.pt = null;
        updateHandle();
        return true;
    }

    @Override
    public boolean mouseMoved(Point pt) {
        Point center = getCenter();
        double x = center.x;
        double y = center.y;
        double radius = getRadius();
        // Proximity to the circle edge is judged on screen (constant pixels); the resulting dot
        // sits on the circle in document space and is projected when drawn.
        Point sc = toScreen(center);
        Point sm = toScreen(pt);
        float d = (float) Geometry.distance(sc.x, sc.y, sm.x, sm.y);
        double screenRadius = radius * viewScale;
        if (screenRadius - 4 <= d && d <= screenRadius + 4) {
            float a = (float) Geometry.angle(x, y, pt.x, pt.y);
            double[] xy;
            xy = Geometry.coordinates(x, y, radius, a);
            this.pt = new Point((float) xy[0], (float) xy[1]);
        } else {
            this.pt = null;
        }
        updateHandle();
        return true;
    }
}
