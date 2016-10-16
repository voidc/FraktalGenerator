package io.github.voidc.fractalgen.renderer;

import io.github.voidc.fractalgen.LSystem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.*;
import java.util.concurrent.TimeoutException;

public abstract class LSystemRenderer {
    protected int maxDepth;
    private List<Point2D> vertices = new ArrayList<>();
    protected double angle;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private final ObservableList<RenderedLSystem> renderCache;
    private RenderService renderService;

    private static final int MAX_VERTEX_COUNT = 200000; //higher values might cause lag when drawing
    private static final int RENDER_TIMEOUT = 1000; //in milliseconds

    public LSystemRenderer() {
        renderCache = FXCollections.observableArrayList();
    }

    private RenderedLSystem render(LSystem lSystem) {
        reset();
        vertices.add(Point2D.ZERO);

        renderRule(lSystem, lSystem.getAxiom(), 0);

        int nPoints = vertices.size();
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];

        for(int i = 0; i< nPoints; i++) {
            Point2D vtx = vertices.get(i);
            xPoints[i] = vtx.getX();
            yPoints[i] = vtx.getY();
        }
        vertices.clear();

        Rectangle2D boundingBox = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
        return new RenderedLSystem(xPoints, yPoints, boundingBox);
    }

    /*
    public CompletableFuture<RenderedLSystem> renderAsync(LSystem lSystem) {
        if(currentAsync != null && !currentAsync.isDone())
            currentAsync.cancel(true);

        currentAsync = CompletableFuture.supplyAsync(() -> render(lSystem));
        return currentAsync;
    }

    public void renderAsyncCached(LSystem lSystem, Consumer<Integer> newDataAvailableListener) {
        renderCache.clear();
        ForkJoinPool.commonPool().execute(() -> {
            for (maxDepth = 0; true; maxDepth++) {
                long start = System.currentTimeMillis();

                renderCache.add(render(lSystem));
                System.out.println("Rendered depth " + maxDepth);
                Platform.runLater(() -> newDataAvailableListener.accept(renderCache.vertexCount()));

                long duration = System.currentTimeMillis() - start;
                if (duration > 1000)
                    break;
            }
        });
    }
    */

    public void renderToCache(LSystem lSystem) {
        if(renderService != null && renderService.isRunning()) {
            renderService.cancel();
        }
        renderCache.clear();

        if(lSystem.getAxiom().isEmpty()) {
            renderCache.add(RenderedLSystem.EMPTY);
            return;
        }

        maxDepth = 0;
        renderService = new RenderService(lSystem);
        renderService.setOnSucceeded(wse -> {
            RenderedLSystem rendered = (RenderedLSystem) wse.getSource().getValue();
            renderCache.add(rendered);
            System.out.println("Rendered depth " + maxDepth + " (v: " + rendered.vertexCount() + ")");
            maxDepth++;
            renderService.restart();
        });
        renderService.start();
    }

    public ObservableList<RenderedLSystem> getCache() {
        return renderCache;
    }

    private void reset() {
        angle = 0;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
    }

    protected abstract void renderRule(LSystem lSystem, String rule, int depth);

    protected void line() {
        Point2D vtx = vertices.get(vertices.size() - 1).add(Math.cos(angle), Math.sin(angle));

        if(vtx.getX() < minX)
            minX = vtx.getX();
        if(vtx.getX() > maxX)
            maxX = vtx.getX();
        if(vtx.getY() < minY)
            minY = vtx.getY();
        if(vtx.getY() > maxY)
            maxY = vtx.getY();

        vertices.add(vtx);
    }

    private class RenderService extends Service<RenderedLSystem> {
        private LSystem lSystem;

        public RenderService(LSystem lSystem) {
            this.lSystem = lSystem;
        }

        @Override
        protected Task<RenderedLSystem> createTask() {
            return new Task<RenderedLSystem>() {
                @Override
                protected RenderedLSystem call() throws Exception {
                    long start = System.currentTimeMillis();
                    RenderedLSystem rendered = render(lSystem);
                    long duration = System.currentTimeMillis() - start;
                    if (duration > RENDER_TIMEOUT
                            || rendered.vertexCount() > MAX_VERTEX_COUNT
                            || maxDepth > 100) {
                        throw new TimeoutException();
                    } else return rendered;
                }
            };
        }
    }
}
