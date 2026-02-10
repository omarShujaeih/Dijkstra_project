package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainApp extends Application {

    private Graph graph = null;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dijkstra + PQ (Nodes + Path)");

        // ===== Left Panel Controls =====
        Button btnLoad = new Button("Load Graph File (.txt)");
        Label lblFile = new Label("No file loaded");

        TextField tfSource = new TextField();
        tfSource.setPromptText("Source");

        TextField tfDest = new TextField();
        tfDest.setPromptText("Destination");

        ToggleGroup tg = new ToggleGroup();
        RadioButton rbDist = new RadioButton("Distance");
        RadioButton rbTime = new RadioButton("Time");
        RadioButton rbBoth = new RadioButton("Both");
        rbDist.setToggleGroup(tg);
        rbTime.setToggleGroup(tg);
        rbBoth.setToggleGroup(tg);
        rbDist.setSelected(true);

        Button btnRun = new Button("Run Dijkstra");
        btnRun.setDisable(true);

        TextArea taOut = new TextArea();
        taOut.setEditable(false);
        taOut.setWrapText(true);
        taOut.setPrefRowCount(10);

        VBox left = new VBox(10,
                btnLoad, lblFile,
                new Label("From:"), tfSource,
                new Label("To:"), tfDest,
                new Separator(),
                new Label("Optimize:"),
                rbDist, rbTime, rbBoth,
                new Separator(),
                btnRun,
                new Label("Output:"),
                taOut
        );
        left.getStyleClass().add("left-panel");

        left.setPadding(new Insets(12));
        left.setPrefWidth(280);

        // ===== Right Panel Graph (Nodes + Path Only) =====
        GraphView graphView = new GraphView();
        graphView.setPrefSize(900, 600);

        // sync clicks -> textfields
        graphView.setSelectionListener((s, d) -> {
            tfSource.setText(s == null ? "" : s);
            tfDest.setText(d == null ? "" : d);
        });

        // ===== Load Handler =====
        btnLoad.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Graph TXT File");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File f = fc.showOpenDialog(stage);
            if (f == null) return;

            try {
                graph = GraphLoader.load(f);
                lblFile.setText("Loaded: " + f.getName() + " | Nodes: " + graph.size());
                btnRun.setDisable(false);

                taOut.setText("✅ File loaded.\nClick nodes to choose Source then Destination.\n");
                graphView.setGraph(graph);

            } catch (Exception ex) {
                graph = null;
                btnRun.setDisable(true);
                taOut.setText("❌ ERROR loading file:\n" + ex.getMessage());
            }
        });

        // ===== Run Handler =====
        btnRun.setOnAction(e -> {
            if (graph == null) return;

            String src = tfSource.getText().trim();
            String dst = tfDest.getText().trim();

            if (src.isEmpty() || dst.isEmpty()) {
                taOut.setText("⚠️ Please select/enter Source and Destination.");
                return;
            }

            try {
                graphView.setSelectedByName(src, dst);

                if (rbBoth.isSelected()) {
                    DijkstraResult rDist = Dijkstra.solve(graph, src, dst, 1);
                    DijkstraResult rTime = Dijkstra.solve(graph, src, dst, 2);

                    taOut.setText(
                            formatResult("Distance", rDist, 1) + "\n" +
                            formatResult("Time", rTime, 2)
                    );

                    // draw one path on map (Distance default)
                    graphView.setPathByNames(rDist.hasPath() ? rDist.path : null);

                } else {
                    int mode = rbDist.isSelected() ? 1 : 2;
                    String title = (mode == 1) ? "Distance" : "Time";

                    DijkstraResult r = Dijkstra.solve(graph, src, dst, mode);
                    taOut.setText(formatResult(title, r, mode));

                    graphView.setPathByNames(r.hasPath() ? r.path : null);
                }

            } catch (Exception ex) {
                taOut.setText("❌ ERROR:\n" + ex.getMessage());
            }
        });

        // ===== Root Layout =====
        BorderPane root = new BorderPane();
        root.setLeft(left);
        StackPane graphContainer = new StackPane(graphView);
        graphContainer.getStyleClass().add("graph-container");
        root.setCenter(graphContainer);
        BorderPane.setMargin(graphView, new Insets(12));

        Scene scene = new Scene(root, 1000, 720);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // ✅ Prints each segment weight + cumulative sum
    private String formatResult(String title, DijkstraResult r, int mode) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" Result ===\n");

        if (!r.hasPath()) {
            sb.append("No path found.\n");
            return sb.toString();
        }

        sb.append("Path: ").append(String.join(" -> ", r.path)).append("\n");
        sb.append("Segments:\n");

        double sum = 0.0;

        for (int i = 0; i < r.path.size() - 1; i++) {
            String aName = r.path.get(i);
            String bName = r.path.get(i + 1);

            Integer aId = graph.nodeId(aName);
            Integer bId = graph.nodeId(bName);

            if (aId == null || bId == null) {
                sb.append("  ").append(aName).append(" -> ").append(bName)
                  .append(" : (node not found)\n");
                continue;
            }

            double w = graph.edgeWeight(aId, bId, mode);
            sum += w;

            sb.append("  ")
              .append(aName).append(" -> ").append(bName)
              .append(" : ").append(String.format("%.3f", w))
              .append(" | cumulative=").append(String.format("%.3f", sum))
              .append("\n");
        }

        sb.append("Total ").append(title).append(": ")
          .append(String.format("%.3f", r.totalCost))
          .append("\n");

        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
