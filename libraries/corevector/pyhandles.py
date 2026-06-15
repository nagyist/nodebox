from nodebox.handle import CombinedHandle, PointHandle, FourPointHandle, TranslateHandle, RotateHandle, ScaleHandle, CircleScaleHandle, FreehandHandle
from nodebox.util.Geometry import coordinates, angle, distance

class LineHandle(CombinedHandle):
    def __init__(self):
        CombinedHandle.__init__(self)
        self.addHandle(PointHandle("point1"))
        self.addHandle(PointHandle("point2"))
        self.update()
        

class StarHandle(CombinedHandle):
    def __init__(self):
        CombinedHandle.__init__(self)
        self.addHandle(PointHandle())
        self.addHandle(CircleScaleHandle("inner", CircleScaleHandle.Mode.DIAMETER, "position"))
        self.addHandle(CircleScaleHandle("outer", CircleScaleHandle.Mode.DIAMETER, "position"))
        self.update()
        
        
class PolygonHandle(CombinedHandle):
    def __init__(self):
        CombinedHandle.__init__(self)
        self.addHandle(PointHandle())
        self.addHandle(CircleScaleHandle("radius", CircleScaleHandle.Mode.RADIUS, "position"))
        self.update()


class ReflectHandle(CombinedHandle):
    def __init__(self):
        CombinedHandle.__init__(self)
        self.addHandle(TranslateHandle("position"))
        self.addHandle(RotateHandle("angle", "position"))
        self.update()

    def update(self):
        CombinedHandle.update(self)
        self.visible = self.isConnected("shape")

    def draw(self, ctx):
        pos = self.getValue("position")
        x = pos.x
        y = pos.y
        a = self.getValue("angle")
        x1, y1 = coordinates(x, y, -1000, a)
        x2, y2 = coordinates(x, y, 1000, a)
        # Handles draw in screen space, so project the axis endpoints through the view transform.
        s1 = self.toScreen(x1, y1)
        s2 = self.toScreen(x2, y2)
        ctx.stroke(self.HANDLE_COLOR)
        ctx.line(s1.x, s1.y, s2.x, s2.y)
        CombinedHandle.draw(self, ctx)


class SnapHandle(PointHandle):

    def createHitRectangle(self, x, y):
        return Rect(-1000, -1000, 2000, 2000)

    def draw(self, ctx):
        pos = self.getValue("position")
        snap_x = pos.x
        snap_y = pos.y
        distance = self.getValue("distance")
        ctx.stroke(0.4, 0.4, 0.4, 0.5)
        ctx.strokewidth(1.0)
        for i in xrange(-100, 100):
            x = -snap_x + (i * distance)
            y = -snap_y + (i * distance)
            ctx.line(x, -1000, x, 1000)
            ctx.line(-1000, y, 1000, y)
