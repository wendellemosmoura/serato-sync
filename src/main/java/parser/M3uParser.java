package main.java.parser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wendel Lemos Moura
 */

public class M3uParser implements PlaylistParser {

    @Override
    public List<String> parse(File file) throws IOException {
        List<String> tracks = new ArrayList<String>();
        Charset charset = detectCharset(file);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), charset)
        );

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                if (line.isEmpty() || line.startsWith("#")) continue;

                tracks.add(line);
            }
        } finally {
            reader.close();
        }

        return tracks;
    }

    private Charset detectCharset(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".m3u8")) {
            return StandardCharsets.UTF_8;
        }

        FileInputStream fis = new FileInputStream(file);
        try {
            byte[] bom = new byte[3];
            int read = fis.read(bom);
            if (read == 3
                    && bom[0] == (byte) 0xEF
                    && bom[1] == (byte) 0xBB
                    && bom[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            }
        } finally {
            fis.close();
        }

        return Charset.defaultCharset();
    }
}
