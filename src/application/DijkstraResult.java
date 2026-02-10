package application;

import java.util.List;

public class DijkstraResult {
    public final List<String> path;
    public final double totalCost;

    public DijkstraResult(List<String> path, double totalCost) {
        this.path = path;
        this.totalCost = totalCost;
    }

    public boolean hasPath() {
        return path != null && !path.isEmpty() && !Double.isInfinite(totalCost);
    }
}
