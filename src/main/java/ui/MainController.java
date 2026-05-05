package main.java.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import main.java.model.SyncFolder;
import main.java.service.ConfigService;
import main.java.service.LibrarySyncService;

import java.io.File;
import java.nio.file.Paths;



public class MainController {

    private final ConfigService configService = new ConfigService();
    private final LibrarySyncService syncService   = new LibrarySyncService();

    private ObservableList<SyncFolder> folders;
    private Label statusLabel;
    private Button syncButton;
    private Scene scene;

    private boolean darkMode = false;


    // ─── entry point ────────────────────────────────────────────────────

    public Parent build(Stage stage) {
        folders = FXCollections.observableArrayList(
                configService.getConfig().getSyncFolders()
        );

        VBox root = new VBox();
        root.getChildren().addAll(
                buildHeader(stage),
                buildBody(stage)
        );
        return root;
    }

    // ─── header ───────────────────────────────────────────────────────────

    private HBox buildHeader(Stage stage) {
        Label title = new Label("serato-sync");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Hyperlink version = new Hyperlink("v1.0");
        version.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");
        version.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI("https://github.com/wendellemosmoura")
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button themeBtn = new Button("Dark");
        themeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
        themeBtn.setOnAction(e -> {
            darkMode = !darkMode;
            Scene s = stage.getScene();
            if (darkMode) {
                s.getStylesheets().add(
                        getClass().getResource("/dark.css").toExternalForm()
                );
                themeBtn.setText("Light");
            } else {
                s.getStylesheets().clear();
                themeBtn.setText("Dark");
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, title, spacer, version, themeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-border-color: -fx-box-border; -fx-border-width: 0 0 1 0;");
        return header;
    }

    // ─── body ───────────────────────────────────────────────────────────────

    private VBox buildBody(Stage stage) {
        VBox body = new VBox(16);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(
                buildFolderSection(stage),
                buildSeratoPathSection(stage),
                new Separator(),
                buildSyncSection()
        );
        return body;
    }

    // ─── folders section ─────────────────────────────────────────────────────

    private VBox buildFolderSection(Stage stage) {
        Label sectionLabel = new Label("Folders to sync");
        sectionLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.6;");

        VBox section = new VBox(8, sectionLabel, buildInputRow(stage), buildFolderList());
        return section;
    }

    private HBox buildInputRow(Stage stage) {
        TextField input = new TextField();
        input.setPromptText("Folder path or use Browse...");
        input.setStyle("-fx-font-size: 13px;");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select music folder");
            File dir = chooser.showDialog(stage);
            if (dir != null) input.setText(dir.getAbsolutePath());
        });

        Button addBtn = new Button("Add");
        addBtn.setId("addButton");
        addBtn.setDefaultButton(true); // responde ao Enter
        addBtn.setOnAction(e -> addFolder(input));
        input.setOnAction(e -> addFolder(input)); // Enter no campo também adiciona

        HBox row = new HBox(8, input, browseBtn, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void addFolder(TextField input) {
        String path = input.getText().trim();
        if (path.isEmpty()) return;

        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            showStatus("Folder not found: " + path, false);
            return;
        }

        SyncFolder folder = new SyncFolder(Paths.get(path));
        if (!folders.contains(folder)) {
            folders.add(folder);
            configService.addFolder(folder);
        }
        input.clear();
        showStatus("Folder added.", true);
    }

    private ListView<SyncFolder> buildFolderList() {
        ListView<SyncFolder> list = new ListView<>(folders);
        list.setPrefHeight(180);
        list.setStyle("-fx-font-size: 13px;");
        list.setPlaceholder(new Label("No folders added yet."));

        list.setCellFactory(lv -> new ListCell<SyncFolder>() {
            private final Label  pathLabel = new Label();
            private final Button deleteBtn = new Button("×");
            private final HBox   row       = new HBox(8, pathLabel, deleteBtn);

            {
                HBox.setHgrow(pathLabel, Priority.ALWAYS);
                pathLabel.setMaxWidth(Double.MAX_VALUE);

                deleteBtn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #cc4444;" +
                                "-fx-font-size: 16px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 0 4 2 4;"
                );
                deleteBtn.setOnAction(e -> {
                    SyncFolder item = getItem();
                    if (item != null) {
                        folders.remove(item);
                        configService.removeFolder(item);
                        showStatus("Folder removed.", true);
                    }
                });

                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, 4, 2, 8));
            }

            @Override
            protected void updateItem(SyncFolder item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    pathLabel.setText(item.toString());
                    setGraphic(row);
                }
            }
        });

        return list;
    }

    // ─── Serato library section ──────────────────────────────────────────

    private VBox buildSeratoPathSection(Stage stage) {
        Label sectionLabel = new Label("Serato library location (_Serato_)");
        sectionLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.6;");

        TextField pathField = new TextField();
        pathField.setStyle("-fx-font-size: 13px;");
        pathField.setEditable(false);

        if (configService.getConfig().getSeratoLibraryPath() != null) {
            pathField.setText(configService.getConfig().getSeratoLibraryPath().toString());
        }
        HBox.setHgrow(pathField, Priority.ALWAYS);

        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select _Serato_ folder");
            File dir = chooser.showDialog(stage);
            if (dir != null) {
                pathField.setText(dir.getAbsolutePath());
                configService.setSeratoLibraryPath(Paths.get(dir.getAbsolutePath()));
            }
        });

        HBox row = new HBox(8, pathField, browseBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, sectionLabel, row);
    }

    // ─── sync section ──────────────────────────────────────────────

    private VBox buildSyncSection() {
        statusLabel = new Label(" ");
        statusLabel.setStyle("-fx-font-size: 12px;");
        statusLabel.setWrapText(true);

        syncButton = new Button("Sync library");
        syncButton.setMaxWidth(Double.MAX_VALUE);
        syncButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 0 10 0;");
        syncButton.setOnAction(e -> startSync());

        return new VBox(10, statusLabel, syncButton);
    }

    // ─── background sync ─────────────────────────────────────────

    private void startSync() {
        if (folders.isEmpty()) {
            showStatus("Add at least one folder before syncing.", false);
            return;
        }

        syncButton.setDisable(true);
        showStatus("Starting sync...", true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                syncService.sync(
                        configService.getConfig(),
                        (folderPath, index) ->
                                Platform.runLater(() ->
                                        showStatus("Processing: " + folderPath, true)
                                )
                );
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showStatus("Sync complete! Open Serato to see your crates.", true);
            syncButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showStatus("Erro: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()), false);
            syncButton.setDisable(false);
            ex.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true); // encerra com a janela sem bloquear o sistema
        thread.start();
    }

    // ─── status utility ─────────────────────────────────────────────────

    private void showStatus(String message, boolean isSuccess) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: " +
                        (isSuccess ? "#1D9E75" : "#D85A30") + ";"
        );
    }
}
