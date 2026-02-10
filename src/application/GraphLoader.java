package application;

import java.io.*;

public class GraphLoader {

    public static Graph load(File file) throws IOException {
        Graph g = new Graph();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                String a, b;
                double dist, time;

                if (parts.length == 4) {
                    // Node1 Node2 Distance Time
                    a = parts[0];
                    b = parts[1];
                    dist = Double.parseDouble(parts[2]);
                    time = Double.parseDouble(parts[3]);

                } else if (parts.length == 3) {
                    // Node1 Node2 Weight  (نستخدمها كـ distance و time)
                    a = parts[0];
                    b = parts[1];
                    dist = Double.parseDouble(parts[2]);
                    time = dist;

                } else {
                    throw new IOException(
                        "Invalid line " + lineNo + ": " + line +
                        "\nExpected: Node1 Node2 Distance [Time]"
                    );
                }

                g.addUndirected(a, b, dist, time);
            }
        }

        return g;
    }
}

