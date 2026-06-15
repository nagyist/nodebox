package nodebox.handle;

import nodebox.graphics.GraphicsContext;
import nodebox.graphics.Path;
import nodebox.graphics.Point;
import nodebox.graphics.Rect;

public class TranslateHandle extends AbstractHandle {

    public static final int HANDLE_LENGTH = 100;

    // Half-thickness (in screen pixels) of the grab band around each axis line, so the whole
    // line can be dragged, not just its arrow tip.
    private static final int AXIS_PADDING = 6;

    private enum DragState {
        NONE, CENTER, HORIZONTAL, VERTICAL
    }

    private String translateName;
    private double px, py;
    private double ox, oy;
    private DragState dragState = DragState.NONE;

    public TranslateHandle() {
        this("translate");
    }

    public TranslateHandle(String translateName) {
        this.translateName = translateName;
        update();
    }

    @Override
    public void update() {
        setVisible(isConnected("shape"));
    }

    public void draw(GraphicsContext ctx) {
        // The gizmo's reach and arrow heads are UI chrome, drawn at a constant pixel size around
        // the projected document position.
        double handleLength = HANDLE_LENGTH;
        double tip = 3;
        double barb = 5;
        Point cp = toScreen((Point) getValue(translateName));
        double x = cp.x;
        double y = cp.y;
        ctx.rectmode(GraphicsContext.RectMode.CENTER);
        Path p = new Path();
        p.setFillColor(HANDLE_COLOR);
        ctx.stroke(HANDLE_COLOR);
        p.setStrokeColor(null);
        ctx.nofill();
        drawDot(ctx, x, y);

        if (dragState == DragState.NONE) {
            // Horizontal and vertical direction lines.
            ctx.line(x, y, x + handleLength, y);
            ctx.line(x, y, x, y + handleLength);

            // Vertical arrow
            p.moveto(x, y + handleLength + tip);
            p.lineto(x - barb, y + handleLength - tip);
            p.lineto(x + barb, y + handleLength - tip);

            // Horizontal arrow
            p.moveto(x + handleLength + tip, y);
            p.lineto(x + handleLength - tip, y - barb);
            p.lineto(x + handleLength - tip, y + barb);
        } else if (dragState == DragState.CENTER) {
            Point ps = toScreen(px, py);
            ctx.line(ps.x, ps.y, x, y);
            drawDot(ctx, x, y);
        } else if (dragState == DragState.HORIZONTAL) {
            double psx = toScreen(px, py).x;
            double x0, x1;
            ctx.line(psx - handleLength, y, x + handleLength, y);
            if (x + handleLength > psx - handleLength) {
                // arrow points right
                x0 = x + handleLength + tip;
                x1 = x + handleLength - tip;
            } else {
                // arrow points left
                x0 = x + handleLength - tip;
                x1 = x + handleLength + tip;
            }
            p.moveto(x0, y);
            p.lineto(x1, y - barb);
            p.lineto(x1, y + barb);
        } else if (dragState == DragState.VERTICAL) {
            double psy = toScreen(px, py).y;
            double y0, y1;
            ctx.line(x, psy - handleLength, x, y + handleLength);
            if (y + handleLength > psy - handleLength) {
                // arrow points down
                y0 = y + handleLength + tip;
                y1 = y + handleLength - tip;
            } else {
                // arrow points up
                y0 = y + handleLength - tip;
                y1 = y + handleLength + tip;
            }
            p.moveto(x, y0);
            p.lineto(x - barb, y1);
            p.lineto(x + barb, y1);
        }
        ctx.nostroke();
        ctx.draw(p);
    }

    @Override
    public boolean mousePressed(Point pt) {
        double handleLength = HANDLE_LENGTH;
        px = pt.getX();
        py = pt.getY();

        Point cp = (Point) getValue(translateName);
        ox = cp.x;
        oy = cp.y;

        Point sm = toScreen(pt);
        Point cs = toScreen(cp);
        // The center grabs for free (two-axis) movement. The horizontal and vertical regions are
        // padded bands covering the whole axis line up to and including the arrow tip, so you can
        // grab anywhere along a line to constrain movement to that axis.
        Rect centerRect = createHitRectangle(cs.x, cs.y);
        Rect horRect = new Rect(cs.x, cs.y - AXIS_PADDING, handleLength + AXIS_PADDING, 2 * AXIS_PADDING);
        Rect vertRect = new Rect(cs.x - AXIS_PADDING, cs.y, 2 * AXIS_PADDING, handleLength + AXIS_PADDING);

        if (centerRect.contains(sm))
            dragState = DragState.CENTER;
        else if (horRect.contains(sm))
            dragState = DragState.HORIZONTAL;
        else if (vertRect.contains(sm))
            dragState = DragState.VERTICAL;

        return (dragState != DragState.NONE);
    }

    @Override
    public boolean mouseDragged(Point pt) {
        if (dragState == DragState.NONE) return false;
        Point cp = (Point) getValue(translateName);
        double dx = pt.x - px;
        double dy = pt.y - py;
        if (dx == 0 && dy == 0) return false;
        startCombiningEdits("Set Value");
        if (dragState == DragState.CENTER) {
            silentSet(translateName, new Point(ox + dx, oy + dy));
        } else if (dragState == DragState.HORIZONTAL)
            silentSet(translateName, new Point(ox + dx, cp.y));
        else if (dragState == DragState.VERTICAL)
            silentSet(translateName, new Point(cp.x, oy + dy));
        return true;
    }

    @Override
    public boolean mouseReleased(Point pt) {
        if (dragState == DragState.NONE) return false;
        dragState = DragState.NONE;
        stopCombiningEdits();
        updateHandle();
        return true;
    }
}
