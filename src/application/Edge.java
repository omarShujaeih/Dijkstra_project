package application;



public class Edge {
    public final int to;
    public final double distance;
    public final double time;

    public Edge(int to, double distance, double time) {
        this.to = to;
        this.distance = distance;
        this.time = time;
    }

    // mode: 1=Distance, 2=Time
    public double weight(int mode) {
        return (mode == 1) ? distance : time;
    }
}
