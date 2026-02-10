package application;

import java.util.*;

public class Graph {
    private final Map<String, Integer> id = new HashMap<>();
    private final List<String> name = new ArrayList<>();
    private final List<List<Edge>> adj = new ArrayList<>();

    public int getOrCreate(String node) {
        Integer existing = id.get(node);
        if (existing != null) return existing;

        int newId = name.size();
        id.put(node, newId);
        name.add(node);
        adj.add(new ArrayList<>());
        return newId;
    }

    public void addUndirected(String a, String b, double dist, double time) {
        int u = getOrCreate(a);
        int v = getOrCreate(b);

        adj.get(u).add(new Edge(v, dist, time));
        adj.get(v).add(new Edge(u, dist, time));
    }

    public int size() {
        return name.size();
    }

    public List<Edge> neighbors(int u) {
        return adj.get(u);
    }

    public String nodeName(int idx) {
        return name.get(idx);
    }

    public Integer nodeId(String node) {
        return id.get(node);
    }

    // mode: 1 = distance, 2 = time
    public double edgeWeight(int from, int to, int mode) {
        for (Edge e : adj.get(from)) {
            if (e.to == to) {
                return e.weight(mode);
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}
