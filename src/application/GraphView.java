package application;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GraphView extends Pane {

    private final Canvas canvas = new Canvas(900, 600);
    private Graph graph;

    private double[] xs;
    private double[] ys;

    private int src = -1;
    private int dst = -1;

    private List<Integer> pathNodes = new ArrayList<>();

    private double nodeRadius = 2;   // نقاط صغيرة
    private double pickRadius = 10;  // مساحة كبسة أكبر من النقطة

    public interface SelectionListener {
        void onSelected(String sourceName, String destName);
    }
    private SelectionListener listener;

    public GraphView() {
        getChildren().add(canvas);

        widthProperty().addListener((obs, o, n) -> {
            canvas.setWidth(n.doubleValue());
            if (graph != null) computeCircularLayout();
            redraw();
        });
        heightProperty().addListener((obs, o, n) -> {
            canvas.setHeight(n.doubleValue());
            if (graph != null) computeCircularLayout();
            redraw();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClick);
        redraw();
    }

    public void setSelectionListener(SelectionListener l) {
        this.listener = l;
    }

    public void setGraph(Graph g) {
        this.graph = g;
        this.src = -1;
        this.dst = -1;
        this.pathNodes.clear();

        computeCircularLayout();
        redraw();
        notifySelection();
    }

    public void setSelectedByName(String source, String dest) {
        if (graph == null) return;
        Integer s = graph.nodeId(source);
        Integer d = graph.nodeId(dest);
        src = (s == null) ? -1 : s;
        dst = (d == null) ? -1 : d;
        pathNodes.clear();
        redraw();
        notifySelection();
    }

    public void setPathByNames(List<String> path) {
        pathNodes.clear();
        if (graph == null || path == null || path.isEmpty()) {
            redraw();
            return;
        }
        for (String name : path) {
            Integer id = graph.nodeId(name);
            if (id != null) pathNodes.add(id);
        }
        redraw();
    }

    private void notifySelection() {
        if (listener == null || graph == null) return;
        String s = (src >= 0) ? graph.nodeName(src) : "";
        String d = (dst >= 0) ? graph.nodeName(dst) : "";
        listener.onSelected(s, d);
    }

    private void computeCircularLayout() {
        int n = graph.size();
        xs = new double[n];
        ys = new double[n];

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double margin = 35;

        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);

        double cellW = (w - 2 * margin) / cols;
        double cellH = (h - 2 * margin) / rows;

        for (int i = 0; i < n; i++) {
            int r = i / cols;
            int c = i % cols;

            xs[i] = margin + c * cellW + cellW / 2.0;
            ys[i] = margin + r * cellH + cellH / 2.0;
        }
    }


    private void handleClick(MouseEvent e) {
        if (graph == null) return;

        int nearest = findNearestNode(e.getX(), e.getY());
        if (nearest == -1) return;

        if (src == -1 || (src != -1 && dst != -1)) {
            src = nearest;
            dst = -1;
            pathNodes.clear();
        } else {
            dst = nearest;
            pathNodes.clear();
        }

        redraw();
        notifySelection();
    }

    private int findNearestNode(double x, double y) {
        if (xs == null) return -1;

        double best = Double.POSITIVE_INFINITY;
        int bestIdx = -1;

        for (int i = 0; i < xs.length; i++) {
            double dx = xs[i] - x;
            double dy = ys[i] - y;
            double d2 = dx * dx + dy * dy;
            if (d2 < best) {
                best = d2;
                bestIdx = i;
            }
        }

        if (Math.sqrt(best) <= pickRadius) return bestIdx;
        return -1;
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);

        if (graph == null) {
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillText("Load a graph file to display.", 20, 20);
            return;
        }

        if (xs == null || xs.length != graph.size()) computeCircularLayout();

        // ===== Nodes (Gray) =====
        gc.setFill(javafx.scene.paint.Color.web("#9ca3af")); // رمادي للنقاط

        for (int i = 0; i < graph.size(); i++) {
            double x = xs[i];
            double y = ys[i];

            // خلّي src/dst ينرسموا لاحقًا بلونهم، هون بس نرسم العادي
            if (i == src || i == dst) continue;

            gc.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
        }

        // ===== Highlight Source / Destination =====
        double bigR = nodeRadius + 4;

        if (src >= 0) {
            gc.setFill(javafx.scene.paint.Color.web("#22c55e")); // أخضر
            gc.fillOval(xs[src] - bigR, ys[src] - bigR, bigR * 2, bigR * 2);
        }

        if (dst >= 0) {
            gc.setFill(javafx.scene.paint.Color.web("#ef4444")); // أحمر
            gc.fillOval(xs[dst] - bigR, ys[dst] - bigR, bigR * 2, bigR * 2);
        }

        // ===== Path (Blue) =====
        if (pathNodes != null && pathNodes.size() >= 2) {
            gc.setStroke(javafx.scene.paint.Color.web("#38bdf8"));
            gc.setLineWidth(3.5);

            for (int i = 0; i < pathNodes.size() - 1; i++) {
                int a = pathNodes.get(i);
                int b = pathNodes.get(i + 1);
                gc.strokeLine(xs[a], ys[a], xs[b], ys[b]);
            }

            gc.setLineWidth(1.0);
        }

        // ===== UI Hint =====
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("Click: Source then Destination. Run to draw path.", 12, 18);
    }
}
