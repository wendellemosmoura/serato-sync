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
import main.java.model.SyncPlaylist;
import main.java.parser.PlaylistParserFactory;
import main.java.service.ConfigService;
import main.java.service.LibrarySyncService;
import main.java.util.OsUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author Wendel Lemos Moura
 */

public class MainController {

    private final ConfigService configService = new ConfigService();
    private final LibrarySyncService syncService = new LibrarySyncService();

    private ObservableList<SyncFolder> folders;
    private ObservableList<SyncPlaylist> playlists;
    private Label statusLabel;
    private Button syncButton;
    private boolean darkMode = false;

    // ─── entry point ────────────────────────────────────────────────────

    public Parent build(Stage stage) {
        folders = FXCollections.observableArrayList(
                configService.getConfig().getSyncFolders()
        );

        playlists = FXCollections.observableArrayList(
                configService.getConfig().getSyncPlaylists()
        );

        VBox root = new VBox();
        root.getChildren().addAll(
                buildHeader(stage),
                buildBody(stage)
        );
        return root;
    }

    public void applyInitialTheme(Scene scene) {
        if (configService.getConfig().isDarkMode()) {
            scene.getStylesheets().add(
                    getClass().getResource("/dark.css").toExternalForm()
            );
        }
    }

    // ─── header ───────────────────────────────────────────────────────────

    private HBox buildHeader(Stage stage) {
        Label title = new Label("serato-sync");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Hyperlink version = new Hyperlink(readAppVersion());
        version.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");
        version.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI("https://github.com/wendellemosmoura/serato-sync/releases")
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        darkMode = configService.getConfig().isDarkMode();
        Button themeBtn = new Button(darkMode ? "Light" : "Dark");
        themeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");

        themeBtn.setOnAction(e -> {
            darkMode = !darkMode;
            configService.setDarkMode(darkMode);
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

    private String readAppVersion() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(getClass().getResourceAsStream("/version.properties"));
            return "v" + props.getProperty("app.version", "?.?.?");
        } catch (Exception e) {
            return "v?.?.?";
        }
    }

    // ─── body ───────────────────────────────────────────────────────────────

    private VBox buildBody(Stage stage) {
        VBox body = new VBox(16);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(
                buildFolderSection(stage),
                buildPlaylistSection(stage),
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
        addBtn.setDefaultButton(true);
        addBtn.setOnAction(e -> addFolder(input));
        input.setOnAction(e -> addFolder(input));

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
            private final Label pathLabel = new Label();
            private final Button deleteBtn = new Button("×");
            private final HBox row = new HBox(8, pathLabel, deleteBtn);

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
                ContextMenu contextMenu = new ContextMenu();
                MenuItem revealItem = new MenuItem("Reveal in Explorer");
                if (OsUtils.isMac()) revealItem.setText("Reveal in Finder");
                revealItem.setOnAction(e -> {
                    SyncFolder item = getItem();
                    if (item != null) revealInFileManager(item.getPath().toFile());
                });

                contextMenu.getItems().add(revealItem);
                row.setOnContextMenuRequested(e ->
                        contextMenu.show(row, e.getScreenX(), e.getScreenY())
                );

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

    // ─── playlists section ─────────────────────────────────────────────────────

    private VBox buildPlaylistSection(Stage stage) {
        Label sectionLabel = new Label("Playlists to import");
        sectionLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.6;");

        return new VBox(8, sectionLabel, buildPlaylistInputRow(stage), buildPlaylistList());
    }

    private HBox buildPlaylistInputRow(Stage stage) {
        TextField input = new TextField();
        input.setPromptText(".m3u or .wpl file...");
        input.setStyle("-fx-font-size: 13px;");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select playlist file");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(
                            "Playlist files", "*.m3u", "*.m3u8", "*.wpl"
                    )
            );
            File file = chooser.showOpenDialog(stage);
            if (file != null) input.setText(file.getAbsolutePath());
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> addPlaylist(input));
        input.setOnAction(e -> addPlaylist(input));

        HBox row = new HBox(8, input, browseBtn, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void addPlaylist(TextField input) {
        String path = input.getText().trim();
        if (path.isEmpty()) return;

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            showStatus("File not found: " + path, false);
            return;
        }
        if (!PlaylistParserFactory.isSupported(file)) {
            showStatus("Unsupported format. Use .m3u, .m3u8 or .wpl", false);
            return;
        }

        SyncPlaylist playlist = new SyncPlaylist(file);
        if (!playlists.contains(playlist)) {
            playlists.add(playlist);
            configService.addPlaylist(playlist);
        }
        input.clear();
        showStatus("Playlist added.", true);
    }

    private ListView<SyncPlaylist> buildPlaylistList() {
        ListView<SyncPlaylist> list = new ListView<>(playlists);
        list.setPrefHeight(120);
        list.setStyle("-fx-font-size: 13px;");
        list.setPlaceholder(new Label("No playlists added yet."));

        list.setCellFactory(lv -> new ListCell<SyncPlaylist>() {
            private final Label  nameLabel = new Label();
            private final Button deleteBtn = new Button("×");
            private final HBox   row       = new HBox(8, nameLabel, deleteBtn);

            {
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                nameLabel.setMaxWidth(Double.MAX_VALUE);

                deleteBtn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #cc4444;" +
                                "-fx-font-size: 16px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 0 4 2 4;"
                );
                deleteBtn.setOnAction(e -> {
                    SyncPlaylist item = getItem();
                    if (item != null) {
                        playlists.remove(item);
                        configService.removePlaylist(item);
                        showStatus("Playlist removed.", true);
                    }
                });
                ContextMenu contextMenu = new ContextMenu();
                MenuItem revealItem = new MenuItem("Reveal in Explorer");
                if (OsUtils.isMac()) revealItem.setText("Reveal in Finder");

                revealItem.setOnAction(e -> {
                    SyncPlaylist item = getItem();
                    if (item != null) revealInFileManager(item.getFile());
                });

                contextMenu.getItems().add(revealItem);
                row.setOnContextMenuRequested(e ->
                        contextMenu.show(row, e.getScreenX(), e.getScreenY())
                );

                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, 4, 2, 8));
            }

            @Override
            protected void updateItem(SyncPlaylist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.getName()); // exibe só o nome do arquivo, não o path todo
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
            showStatus("Add at least one folder or playlist before syncing.", false);
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
            showStatus("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()), false);
            syncButton.setDisable(false);
            ex.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
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

    // ─── reveal in file manager utility ─────────────────────────────────────────────────

    private void revealInFileManager(File target) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (target.isDirectory()) {
                desktop.open(target);
            } else {
                desktop.open(target.getParentFile());
            }
        } catch (Exception ex) {
            showStatus("Could not open file manager.", false);
        }
    }
}