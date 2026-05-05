package main.java.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BinaryWriter implements AutoCloseable {
    private OutputStream out;

    public BinaryWriter(OutputStream out) {
        this.out = out;
    }

    public void writeString(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    public void writeInt(int value) throws IOException {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public void writeUTF16BE(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_16BE));
    }

    public void writeByte(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public void writeBytes(byte[] data) throws IOException {
        out.write(data);
    }
}
