package nodebox.handle;

import nodebox.graphics.*;

import java.awt.event.KeyEvent;


public abstract class AbstractHandle implements Handle {

    public static final int HANDLE_SIZE = 6;
    public static final int HALF_HANDLE_SIZE = HANDLE_SIZE / 2;
    public static final Color HANDLE_COLOR = new Color(0.41, 0.39, 0.68);

    public static final int SHIFT_DOWN = KeyEvent.SHIFT_DOWN_MASK;
    public static final int CTRL_DOWN = KeyEvent.CTRL_DOWN_MASK;
    public static final int ALT_DOWN = KeyEvent.ALT_DOWN_MASK;
    public static final int META_DOWN = KeyEvent.META_DOWN_MASK;

    private HandleDelegate delegate;
    private boolean visible = true;
    private boolean combinesEdits = false;
    // The view transform (document -> screen). Handles draw in screen space, so they project
    // the document coordinates they receive through this transform.
    protected double viewX = 0.0;
    protected double viewY = 0.0;
    protected double viewScale = 1.0;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setViewTransform(double viewX, double viewY, double viewScale) {
        this.viewX = viewX;
        this.viewY = viewY;
        this.viewScale = viewScale;
    }

    /**
     * Project a document point into screen space, where handles are drawn and hit-tested.
     * Public so handles written in scripting languages (e.g. Jython) can project too.
     */
    public Point toScreen(Point p) {
        return new Point(viewX + viewScale * p.x, viewY + viewScale * p.y);
    }

    public Point toScreen(double x, double y) {
        return new Point(viewX + viewScale * x, viewY + viewScale * y);
    }

    //// Stub implementations of event handling ////

    public boolean mouseClicked(Point pt) {
        return false;
    }

    public boolean mousePressed(Point pt) {
        return false;
    }

    public boolean mouseReleased(Point pt) {
        return false;
    }

    public boolean mouseEntered(Point pt) {
        return false;
    }

    public boolean mouseExited(Point pt) {
        return false;
    }

    public boolean mouseDragged(Point pt) {
        return false;
    }

    public boolean mouseMoved(Point pt) {
        return false;
    }

    public boolean keyTyped(int keyCode, int modifiers) {
        return false;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int keyCode, int modifiers) {
        return false;
    }

    //// Node events ////

    // TODO Can be removed?
    public void update() {
    }

    //// Node update methods ////

    public boolean hasInput(String portName) {
        if (delegate != null)
            return delegate.hasInput(portName);
        return false;
    }


    public boolean isConnected(String portName) {
        if (delegate != null)
            return delegate.isConnected(portName);
        return false;
    }

    public Object getValue(String portName) {
        if (delegate != null)
            return delegate.getValue(portName);
        return null;
    }

    /*public void setValue(String portName, Object value) {
        if (delegate != null && !isConnected(portName))
            delegate.setValue(portName, value);
    }*/

    public void silentSet(String portName, Object value) {
        if (delegate != null && !isConnected(portName))
            delegate.silentSet(portName, value);
    }

    public void startCombiningEdits(String command) {
        if (delegate != null && !combinesEdits) {
            delegate.startEdits(command);
            combinesEdits = true;
        }
    }

    public void stopCombiningEdits() {
        if (delegate != null) {
            combinesEdits = false;
            delegate.stopEditing();
        }
    }

    public void updateHandle() {
        if (delegate != null)
            delegate.updateHandle();
    }

    //// Handle delegate ////

    public HandleDelegate getHandleDelegate() {
        return delegate;
    }

    public void setHandleDelegate(HandleDelegate delegate) {
        this.delegate = delegate;
    }


    //// Utility methods ////

    protected void drawDot(GraphicsContext ctx, double x, double y) {
        ctx.rectmode(GraphicsContext.RectMode.CENTER);
        ctx.fill(HANDLE_COLOR);
        ctx.rect(x, y, HANDLE_SIZE, HANDLE_SIZE);
    }

    protected void drawDot(Path p, double x, double y) {
        p.rect(x, y, HANDLE_SIZE, HANDLE_SIZE);
    }

    /**
     * Create a screen-space rectangle that can be used to test if a point is inside of it.
     * (hit testing) The X and Y coordinates form the center of a rectangle the size of the handle.
     * <p/>
     * The coordinates are in screen space, so project the handle position with {@link #toScreen}
     * (or use {@link #hitsHandle}) before calling this.
     *
     * @param x the center x position of the rectangle, in screen space
     * @param y the center y position of the rectangle, in screen space
     * @return a rectangle the size of the handle.
     */
    protected Rect createHitRectangle(double x, double y) {
        return new Rect(x - HALF_HANDLE_SIZE, y - HALF_HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    /**
     * Test whether the cursor is within grab range of a handle drawn at the given document point.
     * Both the handle position and the cursor are projected to screen space, so the grab area is
     * a constant size on screen regardless of zoom.
     *
     * @param worldX     the handle's document x position
     * @param worldY     the handle's document y position
     * @param worldMouse the cursor position, in document space
     * @return true if the cursor is over the handle.
     */
    protected boolean hitsHandle(double worldX, double worldY, Point worldMouse) {
        Point sh = toScreen(worldX, worldY);
        Point sm = toScreen(worldMouse);
        return createHitRectangle(sh.x, sh.y).contains(sm);
    }

}
