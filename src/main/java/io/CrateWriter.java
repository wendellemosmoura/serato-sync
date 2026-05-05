package main.java.io;

import main.java.model.Crate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CrateWriter {

    public void write(Crate crate, File file) throws IOException {
        try (BinaryWriter out = new BinaryWriter(new FileOutputStream(file))) {
            writeHeader(out);
            writeColumns(out);
            writeTracks(out, crate);
        }
    }

    private void writeHeader(BinaryWriter out) throws IOException {
        String content = "1.0/Serato ScratchLive Crate";
        out.writeString("vrsn");
        out.writeInt(content.length() * 2); // 56 = 0x38
        out.writeUTF16BE(content);
    }

    private void writeColumns(BinaryWriter out) throws IOException {
        writeColumn(out, "song",    "250");
        writeColumn(out, "artist",  "250");
        writeColumn(out, "bpm",     "30");
        writeColumn(out, "key",     "30");
        writeColumn(out, "album",   "250");
        writeColumn(out, "length",  "250");
        writeColumn(out, "comment", "250");
    }

    private void writeColumn(BinaryWriter out, String name, String width) throws IOException {
        int innerSize = 8 + name.length() * 2
                + 8 + width.length() * 2;

        out.writeString("ovct");
        out.writeInt(innerSize);

        out.writeString("tvcn");
        out.writeInt(name.length() * 2);
        out.writeUTF16BE(name);

        out.writeString("tvcw");
        out.writeInt(width.length() * 2);
        out.writeUTF16BE(width);
    }

    private void writeTracks(BinaryWriter out, Crate crate) throws IOException {
        for (String track : crate.getTracks()) {
            String normalized = normalizePath(track);
            byte[] trackBytes = normalized.getBytes(StandardCharsets.UTF_16BE);

            out.writeString("otrk");
            out.writeInt(trackBytes.length + 8);

            out.writeString("ptrk");
            out.writeInt(trackBytes.length);
            out.writeBytes(trackBytes);
        }
    }

    private String normalizePath(String path) {
        path = path.replace("\\", "/");
        path = path.replaceAll("^[A-Za-z]:/", "");
        path = path.replaceAll("^/Volumes/[^/]+/", "");
        path = path.replaceAll("^/", ""); // barra inicial residual
        return path;
    }
}