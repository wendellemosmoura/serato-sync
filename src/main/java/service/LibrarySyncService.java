package main.java.service;

import main.java.fs.FileScanner;
import main.java.io.CratePathResolver;
import main.java.io.CrateWriter;
import main.java.model.AppConfig;
import main.java.model.Crate;
import main.java.model.SyncFolder;
import main.java.model.SyncPlaylist;
import main.java.parser.PlaylistParser;
import main.java.parser.PlaylistParserFactory;
import main.java.util.SeratoPathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Wendel Lemos Moura
 */

public class LibrarySyncService {

    private final FileScanner scanner = new FileScanner();
    private final CratePathResolver resolver = new CratePathResolver();
    private final CrateWriter writer = new CrateWriter();

    /**
     * Synchronizes all configured folders with the Serato library.
     *
     * @param config     configuration loaded by ConfigRepository
     * @param onProgress Optional callback to update the UI: (crateName, total)
     */
    public void sync(AppConfig config, BiConsumer<String, Integer> onProgress) throws IOException {
        Path seratoPath = config.getSeratoLibraryPath();
        SeratoPathUtils.ensureSubcratesExists(seratoPath);

        List<SyncFolder> folders = config.getSyncFolders();
        List<SyncPlaylist> playlists = config.getSyncPlaylists();
        int total = folders.size() + playlists.size();
        int index = 0;

        for (SyncFolder syncFolder : folders) {
            syncOneFolder(syncFolder.getPath(), seratoPath);
            if (onProgress != null) onProgress.accept(syncFolder.toString(), ++index);
        }

        for (SyncPlaylist syncPlaylist : playlists) {
            syncPlaylist(syncPlaylist.getFile(), seratoPath);
            if (onProgress != null) onProgress.accept(syncPlaylist.getName(), ++index);
        }
    }

    private void syncOneFolder(Path rootFolder, Path seratoPath) throws IOException {
        Map<Path, List<Path>> folderMap = scanner.scan(rootFolder);

        for (Map.Entry<Path, List<Path>> entry : folderMap.entrySet()) {
            Path folder = entry.getKey();
            List<Path> tracks = entry.getValue();

            String crateName = resolver.resolveCrateName(rootFolder, folder);
            Crate crate = new Crate(crateName);

            for (Path track : tracks) {
                crate.addTrack(resolver.normalizeTrackPath(track));
            }

            Path crateFile = resolver.resolveCrateFile(seratoPath, crateName);
            writer.write(crate, crateFile.toFile());

            System.out.println("✓ " + crateFile.getFileName());
        }
    }

    public void syncPlaylist(File playlistFile, Path seratoLibraryPath) throws IOException {
        PlaylistParser parser = PlaylistParserFactory.getParser(playlistFile);
        List<String> tracks = parser.parse(playlistFile);

        if (tracks.isEmpty()) {
            throw new IOException("Playlist is empty: " + playlistFile.getName());
        }

        String fileName = playlistFile.getName();
        String crateName = fileName.substring(0, fileName.lastIndexOf('.'));

        Crate crate = new Crate(crateName);
        for (String track : tracks) {
            crate.addTrack(resolver.normalizeTrackPath(Paths.get(track)));
        }

        SeratoPathUtils.ensureSubcratesExists(seratoLibraryPath);
        Path crateFile = resolver.resolveCrateFile(seratoLibraryPath, crateName);
        writer.write(crate, crateFile.toFile());
    }
}