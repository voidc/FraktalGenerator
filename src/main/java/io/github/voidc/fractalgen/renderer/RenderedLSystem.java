package io.github.voidc.fractalgen.renderer;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class RenderedLSystem {
    private final double[] xPoints, yPoints;
    public final Rectangle2D boundingBox;

    public final static RenderedLSystem EMPTY =
            new RenderedLSystem(new double[0], new double[0], new Rectangle2D(0, 0, 1, 1));

    public RenderedLSystem(double[] xPoints, double[] yPoints, Rectangle2D boundingBox) {
        if (xPoints.length != yPoints.length)
            throw new IllegalArgumentException();

        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.boundingBox = boundingBox;
    }

    public void draw(GraphicsContext ctx, double percent) {
        int steps = (int) (xPoints.length * percent);
        ctx.applyEffect(null);
        ctx.strokePolyline(xPoints, yPoints, steps);
    }

    public int vertexCount() {
        return xPoints.length;
    }
}
