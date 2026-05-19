package main.java.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wendel Lemos Moura
 */

public class WplParser implements PlaylistParser {

    @Override
    public List<String> parse(File file) throws IOException {
        List<String> tracks = new ArrayList<String>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputSource inputSource = new InputSource(
                    new InputStreamReader(new FileInputStream(file), detectCharset(file))
            );

            Document doc = builder.parse(inputSource);
            NodeList mediaNodes = doc.getElementsByTagName("media");

            for (int i = 0; i < mediaNodes.getLength(); i++) {
                Element media = (Element) mediaNodes.item(i);
                String src = media.getAttribute("src");

                if (src != null && !src.trim().isEmpty()) {
                    tracks.add(src);
                }
            }

        } catch (Exception e) {
            throw new IOException("Failed to parse WPL file: " + file.getName(), e);
        }

        return tracks;
    }

    private Charset detectCharset(File file) throws IOException {
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

        return isValidUtf8(file) ? StandardCharsets.UTF_8 : Charset.defaultCharset();
    }

    private boolean isValidUtf8(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            byte[] buffer = new byte[4096];
            int read = fis.read(buffer);
            if (read <= 0) return true;

            int i = 0;
            while (i < read) {
                int b = buffer[i] & 0xFF;

                if (b < 0x80) {
                    i++;
                } else if (b >= 0xC2 && b <= 0xDF) {
                    if (i + 1 >= read) break;
                    if ((buffer[i + 1] & 0xC0) != 0x80) return false;
                    i += 2;
                } else if (b >= 0xE0 && b <= 0xEF) {
                    if (i + 2 >= read) break;
                    if ((buffer[i + 1] & 0xC0) != 0x80) return false;
                    if ((buffer[i + 2] & 0xC0) != 0x80) return false;
                    i += 3;
                } else if (b >= 0xF0 && b <= 0xF4) {
                    if (i + 3 >= read) break;
                    if ((buffer[i + 1] & 0xC0) != 0x80) return false;
                    if ((buffer[i + 2] & 0xC0) != 0x80) return false;
                    if ((buffer[i + 3] & 0xC0) != 0x80) return false;
                    i += 4;
                } else {
                    return false;
                }
            }
            return true;
        } finally {
            fis.close();
        }
    }
}
