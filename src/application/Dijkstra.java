package application;

import java.util.*;

public class Dijkstra {

    private static class State {
        int v;
        double cost;
        State(int v, double cost) {
            this.v = v;
            this.cost = cost;
        }
    }

    // mode: 1=Distance, 2=Time
    public static DijkstraResult solve(Graph g, String srcName, String dstName, int mode) {
        Integer src = g.nodeId(srcName);
        Integer dst = g.nodeId(dstName);

        if (src == null) throw new IllegalArgumentException("Source not found: " + srcName);
        if (dst == null) throw new IllegalArgumentException("Destination not found: " + dstName);
        if (mode != 1 && mode != 2) throw new IllegalArgumentException("Mode must be 1 or 2");

        int n = g.size();
        double[] dist = new double[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);

        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingDouble(s -> s.cost));

        dist[src] = 0;
        pq.add(new State(src, 0));

        while (!pq.isEmpty()) {
            State cur = pq.poll();
            int u = cur.v;

            if (visited[u]) continue;
            visited[u] = true;

            if (u == dst) break;

            for (Edge e : g.neighbors(u)) {
                if (visited[e.to]) continue;

                double w = e.weight(mode);
                double newCost = dist[u] + w;

                if (newCost < dist[e.to]) {
                    dist[e.to] = newCost;
                    parent[e.to] = u;
                    pq.add(new State(e.to, newCost));
                }
            }
        }

        if (Double.isInfinite(dist[dst])) {
            return new DijkstraResult(List.of(), Double.POSITIVE_INFINITY);
        }

        // Reconstruct path
        LinkedList<String> path = new LinkedList<>();
        for (int v = dst; v != -1; v = parent[v]) {
            path.addFirst(g.nodeName(v));
        }

        return new DijkstraResult(path, dist[dst]);
    }
}
