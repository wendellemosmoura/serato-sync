package main.java.service;

import main.java.fs.FileScanner;
import main.java.io.CratePathResolver;
import main.java.io.CrateWriter;
import main.java.model.AppConfig;
import main.java.model.Crate;
import main.java.model.SyncFolder;
import main.java.util.SeratoPathUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Wendel Lemos Moura
 */

public class LibrarySyncService {

    private final FileScanner scanner  = new FileScanner();
    private final CratePathResolver resolver = new CratePathResolver();
    private final CrateWriter writer   = new CrateWriter();

    /**
     * Synchronizes all configured folders with the Serato library.
     *
     * @param config   configuration loaded by ConfigRepository
     * @param onProgress Optional callback to update the UI: (crateName, total)
     */
    public void sync(AppConfig config, BiConsumer<String, Integer> onProgress) throws IOException {
        Path seratoPath = config.getSeratoLibraryPath();
        SeratoPathUtils.ensureSubcratesExists(seratoPath);

        List<SyncFolder> folders = config.getSyncFolders();
        int total = folders.size();

        for (int i = 0; i < total; i++) {
            SyncFolder syncFolder = folders.get(i);
            syncOneFolder(syncFolder.getPath(), seratoPath);

            if (onProgress != null) {
                onProgress.accept(syncFolder.toString(), i + 1);
            }
        }
    }

    private void syncOneFolder(Path rootFolder, Path seratoPath) throws IOException {
        Map<Path, List<Path>> folderMap = scanner.scan(rootFolder);

        for (Map.Entry<Path, List<Path>> entry : folderMap.entrySet()) {
            Path   folder = entry.getKey();
            List<Path> tracks = entry.getValue();

            String crateName = resolver.resolveCrateName(rootFolder, folder);
            Crate crate     = new Crate(crateName);

            for (Path track : tracks) {
                crate.addTrack(resolver.normalizeTrackPath(track));
            }

            Path crateFile = resolver.resolveCrateFile(seratoPath, crateName);
            writer.write(crate, crateFile.toFile());

            System.out.println("✓ " + crateFile.getFileName());
        }
    }
}